package org.greenrobot.eventbus.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionStackTraceUtils {

    public static String getStackTraceAsString(Throwable ex) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
