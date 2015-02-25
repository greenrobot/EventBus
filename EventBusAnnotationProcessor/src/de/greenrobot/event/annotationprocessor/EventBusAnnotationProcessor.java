package de.greenrobot.event.annotationprocessor;

import de.greenrobot.event.Subscribe;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("de.greenrobot.event.Subscribe")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class EventBusAnnotationProcessor extends AbstractProcessor {
    private final Map<Element, List<Element>> methodsByClass = new HashMap<Element, List<Element>>();
    private boolean writerRoundDone;
    private int round;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Messager messager = processingEnv.getMessager();
        round++;
        messager.printMessage(Diagnostic.Kind.NOTE, "Processing round " + round + ", new annotations: " +
                !annotations.isEmpty() + ", processingOver: " + env.processingOver());
        if (env.processingOver()) {
            if (!annotations.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Unexpected processing state: annotations still available after processing over");
                return false;
            }
        }
        if (annotations.isEmpty()) {
            return false;
        }

        if (writerRoundDone) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Unexpected processing state: annotations still available after writing.");
        }
        collectSubscribers(annotations, env, messager);

        if (!methodsByClass.isEmpty()) {
            writeSources();
        } else {
            messager.printMessage(Diagnostic.Kind.WARNING, "No @Subscribe annotations found");
        }
        writerRoundDone = true;

        return true;
    }

    private boolean checkElement(Element element, Messager messager) {
        if (element.getModifiers().contains(Modifier.STATIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must not be static", element);
            return false;
        }

        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must be public", element);
            return false;
        }

        Set<Modifier> subscriberClassModifiers = element.getEnclosingElement().getModifiers();
        if (!subscriberClassModifiers.contains(Modifier.PUBLIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber class must be public",
                    element.getEnclosingElement());
            return false;
        }

        List<? extends VariableElement> parameters = ((ExecutableElement) element).getParameters();
        if (parameters.size() != 1) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must have exactly 1 parameter", element);
            return false;
        }

        VariableElement param = parameters.get(0);
        DeclaredType paramType = (DeclaredType) param.asType();
        Set<Modifier> eventClassModifiers = paramType.asElement().getModifiers();
        if (!eventClassModifiers.contains(Modifier.PUBLIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Event type must be public: " + paramType, param);
            return false;
        }
        return true;
    }

    private void collectSubscribers(Set<? extends TypeElement> annotations, RoundEnvironment env, Messager messager) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = env.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                if (checkElement(element, messager)) {
                    Element classElement = element.getEnclosingElement();
                    List<Element> methods = methodsByClass.get(classElement);
                    if (methods == null) {
                        methods = new ArrayList<Element>();
                        methodsByClass.put(classElement, methods);
                    }
                    methods.add(element);
                }
            }
        }
    }

    private void writeSources() {
        String pack = "de.greenrobot.event";
        String className = "MyGeneratedSubscriberIndex";
        BufferedWriter writer = null;
        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(pack + '.' + className);
            writer = new BufferedWriter(sourceFile.openWriter());
            writer.write("package de.greenrobot.event;\n\n");
            //            writer.write("import de.greenrobot.event.SubscriberIndexEntry;\n");
            //            writer.write("import de.greenrobot.event.ThreadMode;\n\n");
            writer.write("/** This class is generated by EventBus, do not edit. */\n");
            writer.write("class " + className + " extends SubscriberIndex {\n");
            writer.write("    SubscriberMethod[] createSubscribersFor(Class<?> subscriberClass) {\n");

            boolean first = true;
            for (Map.Entry<Element, List<Element>> entry : methodsByClass.entrySet()) {
                String ifPrefix;
                if (first) {
                    ifPrefix = "";
                    first = false;
                } else {
                    ifPrefix = "} else ";
                }
                TypeElement subscriberClass = (TypeElement) entry.getKey();
                writeLine(writer, 2, ifPrefix + "if(subscriberClass ==", subscriberClass.asType() + ".class) {");
                writer.write("            return new SubscriberMethod[] {\n");

                Set<String> methodSignatures = new HashSet<String>();
                writeIndexEntries(writer, null, entry.getValue(), methodSignatures);
                while (subscriberClass.getSuperclass().getKind() == TypeKind.DECLARED) {
                    subscriberClass = (TypeElement) processingEnv.getTypeUtils().asElement(subscriberClass.getSuperclass());
                    List<Element> superClassMethods = methodsByClass.get(subscriberClass);
                    if (superClassMethods != null) {
                        writeIndexEntries(writer, subscriberClass, superClassMethods, methodSignatures);
                    }
                }
                writer.write("            };\n");
            }
            if (!methodsByClass.isEmpty()) {
                writer.write("        }\n");
            }
            writer.write("        return null;\n");
            writer.write("    };\n}\n");
        } catch (IOException e) {
            throw new RuntimeException("Could not write source for " + className, e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //Silent
            }
        }
    }

    private void writeIndexEntries(BufferedWriter writer, TypeElement subscriberClass, List<Element> elements, Set<String> methodSignatures) throws IOException {
        for (Element element : elements) {

            List<? extends VariableElement> parameters = ((ExecutableElement) element).getParameters();
            VariableElement param = parameters.get(0);
            DeclaredType paramType = (DeclaredType) param.asType();

            String methodSignature = element+">"+paramType;
            if(!methodSignatures.add(methodSignature)) {
                continue;
            }

            String methodName = element.getSimpleName().toString();
            String subscriberClassString = subscriberClass == null ? "subscriberClass" :
                    subscriberClass.asType().toString() + ".class";

            Subscribe subscribe = element.getAnnotation(Subscribe.class);
            writeLine(writer, 4, "createSubscriberMethod(" + subscriberClassString + ",",
                    "\"" + methodName + "\",",
                    paramType.toString() + ".class,",
                    "ThreadMode." + subscribe.threadMode().name() + "),");

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Indexed @Subscribe at " +
                    element.getEnclosingElement().getSimpleName() + "." + methodName +
                    "(" + paramType.asElement().getSimpleName() + ")");
        }
    }

    private void writeLine(BufferedWriter writer, int indentLevel, String... parts) throws IOException {
        writeIndent(writer, indentLevel);
        int len = indentLevel * 4;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (len + part.length() > 118) {
                writer.write("\n");
                if (indentLevel < 12) {
                    indentLevel += 2;
                }
                writeIndent(writer, indentLevel);
                len = indentLevel * 4;
            } else if (i != 0) {
                writer.write(" ");
            }
            writer.write(part);
            len += part.length();
        }
        writer.write("\n");
    }

    private void writeIndent(BufferedWriter writer, int indentLevel) throws IOException {
        for (int i = 0; i < indentLevel; i++) {
            writer.write("    ");
        }
    }
}
