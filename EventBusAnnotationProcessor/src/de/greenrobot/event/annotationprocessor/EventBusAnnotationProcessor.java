package de.greenrobot.event.annotationprocessor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

@SupportedAnnotationTypes("de.greenrobot.event.Subscribe")
public class EventBusAnnotationProcessor extends AbstractProcessor {
    public static final String CLASS_POSTFIX = "_EventBusInfo";
    public static final String JAVA_LANG_PREFIX = "java.lang.";
    public static final int JAVA_LANG_PREFIX_LENGTH = JAVA_LANG_PREFIX.length();

    /** Found subscriber methods for a class (without superclasses). */
    private final Map<TypeElement, List<ExecutableElement>> methodsByClass =
            new HashMap<TypeElement, List<ExecutableElement>>();
    private final Set<TypeElement> classesToSkip = new HashSet<TypeElement>();

    private boolean writerRoundDone;
    private int round;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Messager messager = processingEnv.getMessager();
        try {
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
            checkForSubscribersToSkip(messager);

            if (!methodsByClass.isEmpty()) {
                writeSources();
            } else {
                messager.printMessage(Diagnostic.Kind.WARNING, "No @Subscribe annotations found");
            }
            writerRoundDone = true;
        } catch (RuntimeException e) {
            // IntelliJ does not handle exceptions nicely, so log and print a message
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error in EventBusAnnotationProcessor: " + e);
        }
        return true;
    }

    private void collectSubscribers(Set<? extends TypeElement> annotations, RoundEnvironment env, Messager messager) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = env.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                if (element instanceof ExecutableElement) {
                    ExecutableElement method = (ExecutableElement) element;
                    if (checkHasNoErrors(method, messager)) {
                        Element classElement = method.getEnclosingElement();
                        List<ExecutableElement> methods = methodsByClass.get(classElement);
                        if (methods == null) {
                            methods = new ArrayList<ExecutableElement>();
                            methodsByClass.put((TypeElement) classElement, methods);
                        }
                        methods.add(method);
                    }
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "@Subscribe is only valid for methods", element);
                }
            }
        }
    }

    private boolean checkHasNoErrors(ExecutableElement element, Messager messager) {
        if (element.getModifiers().contains(Modifier.STATIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must not be static", element);
            return false;
        }

        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must be public", element);
            return false;
        }

        List<? extends VariableElement> parameters = ((ExecutableElement) element).getParameters();
        if (parameters.size() != 1) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Subscriber method must have exactly 1 parameter", element);
            return false;
        }
        return true;
    }

    private void checkForSubscribersToSkip(Messager messager) {
        for (Map.Entry<TypeElement, List<ExecutableElement>> entry : methodsByClass.entrySet()) {
            TypeElement skipCandidate = entry.getKey();
            TypeElement subscriberClass = skipCandidate;
            while (subscriberClass != null) {
                if (!subscriberClass.getModifiers().contains(Modifier.PUBLIC)) {
                    boolean added = classesToSkip.add(skipCandidate);
                    if (added) {
                        String msg;
                        if (subscriberClass.equals(skipCandidate)) {
                            msg = "Falling back to reflection because class is not public";
                        } else {
                            msg = "Falling back to reflection because " + skipCandidate +
                                    " has a non-public super class";
                        }
                        messager.printMessage(Diagnostic.Kind.NOTE, msg, subscriberClass);
                    }
                    break;
                }
                List<ExecutableElement> methods = methodsByClass.get(subscriberClass);
                if (methods != null) {
                    for (ExecutableElement method : methods) {
                        VariableElement param = method.getParameters().get(0);
                        DeclaredType paramType = (DeclaredType) param.asType();
                        Set<Modifier> eventClassModifiers = paramType.asElement().getModifiers();
                        if (!eventClassModifiers.contains(Modifier.PUBLIC)) {
                            boolean added = classesToSkip.add(skipCandidate);
                            if (added) {
                                String msg;
                                if (subscriberClass.equals(skipCandidate)) {
                                    msg = "Falling back to reflection because event type is not public";
                                } else {
                                    msg = "Falling back to reflection because " + skipCandidate +
                                            " has a super class using a non-public event type";
                                }
                                messager.printMessage(Diagnostic.Kind.NOTE, msg, subscriberClass);
                            }
                            break;
                        }
                    }
                }
                subscriberClass = getSuperclass(subscriberClass);
            }
        }
    }

    private TypeElement getSuperclass(TypeElement type) {
        if (type.getSuperclass().getKind() == TypeKind.DECLARED) {
            TypeElement superclass = (TypeElement) processingEnv.getTypeUtils().asElement(type.getSuperclass());
            String name = superclass.getQualifiedName().toString();
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                // Skip system classes, this just degrades performance
                return null;
            } else {
                return superclass;
            }
        } else {
            return null;
        }
    }

    private void writeSources() {
        List<Map.Entry<TypeElement, List<ExecutableElement>>> entries =
                new ArrayList<Map.Entry<TypeElement, List<ExecutableElement>>>(methodsByClass.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<TypeElement, List<ExecutableElement>> entry = entries.get(i);
            TypeElement subscriberClass = entry.getKey();
            if (classesToSkip.contains(subscriberClass)) {
                continue;
            }

            BufferedWriter writer = null;
            try {
                PackageElement packageElement = getPackageElement(subscriberClass);
                String myPackage = packageElement.getQualifiedName().toString();
                String subscriberClassName = getClassString(subscriberClass, myPackage);
                String infoClassName = getInfoClass(subscriberClass, myPackage);

                JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(myPackage + '.' + infoClassName);
                writer = new BufferedWriter(sourceFile.openWriter());
                writer.write("package " + myPackage + ";\n\n");
                writer.write("import de.greenrobot.event.SubscriberInfo;\n");
                writer.write("import de.greenrobot.event.SubscriberMethod;\n");
                writer.write("import de.greenrobot.event.ThreadMode;\n\n");
                writer.write("/** This class is generated by EventBus, do not edit. */\n");
                writer.write("public class " + infoClassName + " extends SubscriberInfo {\n");
                writer.write("    public " + infoClassName + "() {\n");
                TypeElement nextEntry = nextEntry(entries, entry, i);
                String next = getNextValue(myPackage, nextEntry);
                String infoSuperClass = getSuperclassInfoClass(subscriberClass, myPackage);
                writeLine(writer, 2, "super(" + subscriberClassName + ".class,", infoSuperClass + ",", next + ");");
                writer.write("    }\n\n");
                writer.write("    protected SubscriberMethod[] createSubscriberMethods() {\n");
                writer.write("        return new SubscriberMethod[] {\n");
                Set<String> methodSignatures = new HashSet<String>();
                writeMethods(writer, entry.getValue(), methodSignatures, myPackage);
                writer.write("        };\n");
                writer.write("    }\n}\n");
            } catch (IOException e) {
                throw new RuntimeException("Could not write source for " + subscriberClass.getQualifiedName(), e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        //Silent
                    }
                }
            }
        }
    }

    private String getSuperclassInfoClass(TypeElement subscriberClass, String myPackage) {
        DeclaredType superclassType = (DeclaredType) subscriberClass.getSuperclass();
        if (superclassType != null) {
            TypeElement superclass = (TypeElement) superclassType.asElement();
            if (methodsByClass.containsKey(superclass) && !classesToSkip.contains(superclass)) {
                return getInfoClass(superclass, myPackage) + ".class";
            }
        }
        return "null";
    }

    private String getInfoClass(TypeElement subscriberClass, String myPackage) {
        String subscriberClassName = getClassString(subscriberClass, myPackage);
        return subscriberClassName.replace('.', '_') + CLASS_POSTFIX;
    }

    private String getNextValue(String myPackage, TypeElement nextEntry) throws IOException {
        String nextValue;
        if (nextEntry != null) {
            PackageElement nextPackageElement = getPackageElement(nextEntry);
            String nextPackage = nextPackageElement.getQualifiedName().toString();
            String nextInfoClassName = getInfoClass(nextEntry, nextPackage);
            if (!myPackage.equals(nextPackage)) {
                nextInfoClassName = nextPackage + "." + nextInfoClassName;
            }
            nextValue = nextInfoClassName + ".class";
        } else {
            nextValue = "null";
        }
        return nextValue;
    }

    private String getClassString(TypeElement typeElement, String myPackage) {
        String className = typeElement.getQualifiedName().toString();
        int lastPeriod = className.lastIndexOf('.');
        if (!myPackage.isEmpty() && className.startsWith(myPackage) && lastPeriod == myPackage.length()) {
            // TODO detect nested types also

            className = className.substring(myPackage.length() + 1);
        } else if (className.startsWith(JAVA_LANG_PREFIX) && lastPeriod == JAVA_LANG_PREFIX_LENGTH - 1) {
            className = className.substring(JAVA_LANG_PREFIX_LENGTH);
        }
        return className;
    }

    private PackageElement getPackageElement(TypeElement subscriberClass) {
        Element candidate = subscriberClass.getEnclosingElement();
        while (!(candidate instanceof PackageElement)) {
            candidate = candidate.getEnclosingElement();
        }
        return (PackageElement) candidate;
    }

    private TypeElement nextEntry(List<Map.Entry<TypeElement, List<ExecutableElement>>> entries,
                                  Map.Entry<TypeElement, List<ExecutableElement>> current, int currentIdx) {
        for (int i = currentIdx + 1; ; i++) {
            if (i == entries.size()) {
                i = 0;
            }
            if (i == currentIdx) {
                return null;
            } else {
                Map.Entry<TypeElement, List<ExecutableElement>> candidate = entries.get(i);
                if (!classesToSkip.contains(candidate.getKey())) {
                    return candidate.getKey();
                }
            }
        }
    }

    private void writeMethods(BufferedWriter writer, List<ExecutableElement> methods, Set<String> methodSignatures,
                              String myPackage) throws IOException {
        for (ExecutableElement method : methods) {

            List<? extends VariableElement> parameters = method.getParameters();
            TypeMirror paramType = parameters.get(0).asType();
            TypeElement paramElement = (TypeElement) processingEnv.getTypeUtils().asElement(paramType);
            String eventClass = getClassString(paramElement, myPackage) + ".class";

            String methodSignature = method + ">" + paramElement.getQualifiedName();
            if (!methodSignatures.add(methodSignature)) {
                continue;
            }

            String methodName = method.getSimpleName().toString();

            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            List<String> parts = new ArrayList<String>();
            parts.add("createSubscriberMethod(\"" + methodName + "\",");
            String lineEnd = "),";
            if (subscribe.priority() == 0 && !subscribe.sticky()) {
                if (subscribe.threadMode() == ThreadMode.POSTING) {
                    parts.add(eventClass + lineEnd);
                } else {
                    parts.add(eventClass + ",");
                    parts.add("ThreadMode." + subscribe.threadMode().name() + lineEnd);
                }
            } else {
                parts.add(eventClass + ",");
                parts.add("ThreadMode." + subscribe.threadMode().name() + ",");
                parts.add(subscribe.priority() + ",");
                parts.add(subscribe.sticky() + lineEnd);
            }
            writeLine(writer, 3, parts.toArray(new String[parts.size()]));

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Indexed @Subscribe at " +
                    method.getEnclosingElement().getSimpleName() + "." + methodName +
                    "(" + paramElement.getSimpleName() + ")");

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
