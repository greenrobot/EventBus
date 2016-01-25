package org.greenrobot.eventbus.annotationprocessor;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
import javax.annotation.processing.SupportedOptions;
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

@SupportedAnnotationTypes("org.greenrobot.eventbus.Subscribe")
@SupportedOptions("eventBusIndex")
public class EventBusAnnotationProcessor extends AbstractProcessor {
    public static final String INFO_CLASS_POSTFIX = "_EventBusInfo";
    public static final String OPTION_EVENT_BUS_INDEX = "eventBusIndex";

    /** Found subscriber methods for a class (without superclasses). */
    private final Map<TypeElement, List<ExecutableElement>> methodsByClass = new HashMap<>();
    private final Set<TypeElement> classesToSkip = new HashSet<>();

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
                // Nor now, we just use a single index and skip individual files: createInfoFiles();

                String index = processingEnv.getOptions().get(OPTION_EVENT_BUS_INDEX);
                if (index != null) {
                    createInfoIndexFile(index);
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "No option " + OPTION_EVENT_BUS_INDEX +
                            " passed to annotation processor.");
                }
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
                            methods = new ArrayList<>();
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

    // Currently unused in favor of single index files
    private void createInfoFiles() {
        for (Map.Entry<TypeElement, List<ExecutableElement>> entry : methodsByClass.entrySet()) {
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
                writer.write("import org.greenrobot.eventbus.meta.AbstractSubscriberInfo;\n");
                writer.write("import org.greenrobot.eventbus.SubscriberMethod;\n");
                writer.write("import org.greenrobot.eventbus.ThreadMode;\n\n");
                writer.write("/** This class is generated by EventBus, do not edit. */\n");
                writer.write("public class " + infoClassName + " extends AbstractSubscriberInfo {\n");
                writer.write("    public " + infoClassName + "() {\n");
                String infoSuperClass = getSuperclassInfoClass(subscriberClass, myPackage);
                writeLine(writer, 2, "super(" + subscriberClassName + ".class,", infoSuperClass + ",", "/* TODO */ true);");
                writer.write("    }\n\n");
                writer.write("    public SubscriberMethod[] getSubscriberMethods() {\n");
                writer.write("        return new SubscriberMethod[] {\n");
                writeCreateSubscriberMethods(writer, entry.getValue(), "createSubscriberMethod", myPackage);
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
        return subscriberClassName.replace('.', '_') + INFO_CLASS_POSTFIX;
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
        PackageElement packageElement = getPackageElement(typeElement);
        String packageString = packageElement.getQualifiedName().toString();
        String className = typeElement.getQualifiedName().toString();
        if (packageString != null && !packageString.isEmpty()) {
            if (packageString.equals(myPackage)) {
                className = cutPackage(myPackage, className);
            } else if (packageString.equals("java.lang")) {
                className = typeElement.getSimpleName().toString();
            }
        }
        return className;
    }

    private String cutPackage(String paket, String className) {
        if (className.startsWith(paket + '.')) {
            // Don't use TypeElement.getSimpleName, it doesn't work for us with inner classes
            return className.substring(paket.length() + 1);
        } else {
            // Paranoia
            throw new IllegalStateException("Mismatching " + paket + " vs. " + className);
        }
    }

    private PackageElement getPackageElement(TypeElement subscriberClass) {
        Element candidate = subscriberClass.getEnclosingElement();
        while (!(candidate instanceof PackageElement)) {
            candidate = candidate.getEnclosingElement();
        }
        return (PackageElement) candidate;
    }

    private void writeCreateSubscriberMethods(BufferedWriter writer, List<ExecutableElement> methods,
                                              String callPrefix, String myPackage) throws IOException {
        for (ExecutableElement method : methods) {
            List<? extends VariableElement> parameters = method.getParameters();
            TypeMirror paramType = parameters.get(0).asType();
            TypeElement paramElement = (TypeElement) processingEnv.getTypeUtils().asElement(paramType);
            String methodName = method.getSimpleName().toString();
            String eventClass = getClassString(paramElement, myPackage) + ".class";

            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            List<String> parts = new ArrayList<>();
            parts.add(callPrefix + "(\"" + methodName + "\",");
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

    private void createInfoIndexFile(String index) {
        BufferedWriter writer = null;
        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(index);
            int period = index.lastIndexOf('.');
            String myPackage = period > 0 ? index.substring(0, period) : null;
            String clazz = index.substring(period + 1);
            writer = new BufferedWriter(sourceFile.openWriter());
            if (myPackage != null) {
                writer.write("package " + myPackage + ";\n\n");
            }
            writer.write("import org.greenrobot.eventbus.meta.SimpleSubscriberInfo;\n");
            writer.write("import org.greenrobot.eventbus.meta.SubscriberMethodInfo;\n");
            writer.write("import org.greenrobot.eventbus.meta.SubscriberInfo;\n");
            writer.write("import org.greenrobot.eventbus.meta.SubscriberInfoIndex;\n\n");
            writer.write("import org.greenrobot.eventbus.ThreadMode;\n\n");
            writer.write("import java.util.HashMap;\n");
            writer.write("import java.util.Map;\n\n");
            writer.write("/** This class is generated by EventBus, do not edit. */\n");
            writer.write("public class " + clazz + " implements SubscriberInfoIndex {\n");
            writer.write("    private static final Map<Class<?>, SubscriberInfo> SUBSCRIBER_INDEX;\n\n");
            writer.write("    static {\n");
            writer.write("        SUBSCRIBER_INDEX = new HashMap<Class<?>, SubscriberInfo>();\n\n");
            writeIndexLines(writer, myPackage);
            writer.write("    }\n\n");
            writer.write("    private static void putIndex(SubscriberInfo info) {\n");
            writer.write("        SUBSCRIBER_INDEX.put(info.getSubscriberClass(), info);\n");
            writer.write("    }\n\n");
            writer.write("    @Override\n");
            writer.write("    public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {\n");
            writer.write("        SubscriberInfo info = SUBSCRIBER_INDEX.get(subscriberClass);\n");
            writer.write("        if (info != null) {\n");
            writer.write("            return info;\n");
            writer.write("        } else {\n");
            writer.write("            return null;\n");
            writer.write("        }\n");
            writer.write("    }\n");
            writer.write("}\n");
        } catch (IOException e) {
            throw new RuntimeException("Could not write source for " + index, e);
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

    private void writeIndexLines(BufferedWriter writer, String myPackage) throws IOException {
        for (Map.Entry<TypeElement, List<ExecutableElement>> entry : methodsByClass.entrySet()) {
            TypeElement subscriberTypeElement = entry.getKey();
            if (classesToSkip.contains(subscriberTypeElement)) {
                continue;
            }

            String subscriberClass = getClassString(subscriberTypeElement, myPackage);
            if (isVisible(myPackage, subscriberTypeElement)) {
                writeLine(writer, 2,
                        "putIndex(new SimpleSubscriberInfo(" + subscriberClass + ".class,",
                        "true,", "new SubscriberMethodInfo[] {");
                List<ExecutableElement> methods = methodsByClass.get(subscriberTypeElement);
                writeCreateSubscriberMethods(writer, methods, "new SubscriberMethodInfo", myPackage);
                writer.write("        }));\n\n");
            } else {
                writer.write("        // Subscriber not visible to index: " + subscriberClass + "\n");
            }
        }
    }

    private boolean isVisible(String myPackage, TypeElement typeElement) {
        Set<Modifier> modifiers = typeElement.getModifiers();
        boolean visible;
        if (modifiers.contains(Modifier.PUBLIC)) {
            visible = true;
        } else if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED)) {
            visible = false;
        } else {
            String subscriberPackage = getPackageElement(typeElement).getQualifiedName().toString();
            if (myPackage == null) {
                visible = subscriberPackage.length() == 0;
            } else {
                visible = myPackage.equals(subscriberPackage);
            }
        }
        return visible;
    }

    private void writeLine(BufferedWriter writer, int indentLevel, String... parts) throws IOException {
        writeLine(writer, indentLevel, 2, parts);
    }

    private void writeLine(BufferedWriter writer, int indentLevel, int indentLevelIncrease, String... parts)
            throws IOException {
        writeIndent(writer, indentLevel);
        int len = indentLevel * 4;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i != 0) {
                if (len + part.length() > 118) {
                    writer.write("\n");
                    if (indentLevel < 12) {
                        indentLevel += indentLevelIncrease;
                    }
                    writeIndent(writer, indentLevel);
                    len = indentLevel * 4;
                } else {
                    writer.write(" ");
                }
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
