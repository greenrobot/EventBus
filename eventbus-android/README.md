# EventBus for Android

Despite its name this module is actually published as `org.greenrobot:eventbus` as an Android library (AAR).

It has a dependency on the Java-only artifact `org.greenrobot:eventbus-java` (JAR) previously available under the `eventbus` name.

Provides an `AndroidComponents` implementation to the Java library if it detects `AndroidComponentsImpl` on the classpath via reflection.
