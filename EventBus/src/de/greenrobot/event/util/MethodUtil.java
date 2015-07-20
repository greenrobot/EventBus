package de.greenrobot.event.util;

import java.lang.reflect.Method;
/**
 * Created by James on 2015/2/2.<br><br>
 */
public class MethodUtil {
	static{
		try {
			System.loadLibrary("method");
		} catch (Exception e) {
            e.printStackTrace();
		}
	}
	public static native Method[] listMethods(Object obj);
	
	public static String getCClassString(Class<?> classObj){
		String className = classObj.getName();
		className = className.replace(".", "/");
		return "L"+className+";";
	}
}
