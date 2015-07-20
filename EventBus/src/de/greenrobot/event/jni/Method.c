#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <sys/statfs.h>
#include <sys/types.h>
#include<time.h>
/* Header for class de_greenrobot_event_util_MethodUtil */
#define  LOG_TAG    "SCANER"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
/*
 * Class:     de_greenrobot_event_util_MethodUtil
 * Method:    listMethods
 * Signature: (Ljava/lang/String;I)V
 */JNIEXPORT jobjectArray JNICALL Java_de_greenrobot_event_util_MethodUtil_listMethods(JNIEnv *env, jobject obj, jobject pthis) {
	jclass class = (*env)->GetObjectClass(env, pthis);
	jmethodID listMethod = (*env)->GetMethodID(env, class, "getEventSubscriberDescList", "()[Lde/greenrobot/event/EventSubscriberDesc;");
	jobjectArray methodInfos = (*env)->CallObjectMethod(env, pthis, listMethod);
	jint size = (*env)->GetArrayLength(env, methodInfos);
	jobjectArray result;
	result = (*env)->NewObjectArray(env, size, (*env)->FindClass(env, "java/lang/reflect/Method"), 0);
//	LOGE("size:%d", size);
	jint index = 0;
	for (index = 0; index < size; index++) {
//		LOGE("index:%d ", index);
		jobject methodInfo = (*env)->GetObjectArrayElement(env, methodInfos, index);
		jclass methodInfoClass = (*env)->GetObjectClass(env, methodInfo);

		jfieldID methodNameFiled = (*env)->GetFieldID(env, methodInfoClass, "methodName", "Ljava/lang/String;");
		jstring methodName = (jstring) (*env)->GetObjectField(env, methodInfo, methodNameFiled);
		const char *methodNativeStr = (*env)->GetStringUTFChars(env, methodName, 0);
//		LOGE("index:%d methodName:%s", index, methodNativeStr);

//		jfieldID argsFiled = (*env)->GetFieldID(env, methodInfoClass, "args", "Ljava/lang/String;");
//		jstring argsName = (jstring) (*env)->GetObjectField(env, methodInfo, argsFiled);
//
//		const char *argnativeString = (*env)->GetStringUTFChars(env, argsName, 0);
//		LOGE("index:%d argsName:%s", index, argnativeString);

		jmethodID eventClazzMethod = (*env)->GetMethodID(env, methodInfoClass, "getEventClazz", "()Ljava/lang/Class;");
		if (eventClazzMethod == NULL) {
//			LOGE("eventClazzMethod:%s", "null");
			break;
		}
		jclass eventClazz = (jclass) (*env)->CallObjectMethod(env, methodInfo, eventClazzMethod);
		if (eventClazz == NULL) {
//			LOGE("className:%s", "null");
			break;
		}
		jclass cls = (*env)->FindClass(env, "java/lang/Class");
		if (cls == NULL) {
//			LOGE("cls:%s", "null");
			break;
		}
		jmethodID mid_getName = (*env)->GetMethodID(env, cls, "getName", "()Ljava/lang/String;");
		if (mid_getName == NULL) {
//			LOGE("mid_getName:%s", "null");
			break;
		}
		jstring className = (*env)->CallObjectMethod(env, eventClazz, mid_getName);
		int namesize=(*env)->GetStringLength(env, className);
//		int namesize=(*env)->GetStringLength(env, argsName);
		const char *classNameStr = (*env)->GetStringUTFChars(env, className, 0);
		char argsChars[namesize+5];
		sprintf(argsChars, "(L%s;)V", classNameStr);
		str_replace(argsChars, ".", "/");
//		sprintf(argsChars, "(%s)V", argnativeString);
//		LOGE("index:%d str1:%s", index, argsChars);
		jmethodID eventMethod = (*env)->GetMethodID(env, class, methodNativeStr, argsChars);
		(*env)->ReleaseStringUTFChars(env, methodName, methodNativeStr);
		(*env)->ReleaseStringUTFChars(env, className, classNameStr);
//		(*env)->ReleaseStringUTFChars(env, argsName, argnativeString);
		if (eventMethod == NULL) {
//			LOGE("index:%d eventMethod:%d", index, -1);
			break;
		} else {
			jobject methodObj = (*env)->ToReflectedMethod(env, class, eventMethod, JNI_FALSE);
			(*env)->SetObjectArrayElement(env, result, index, methodObj);
		}
	}
	return result;
}
 int str_replace(char* str,char* str_src, char* str_des){
     char *ptr=NULL;
     char buff[256];
     char buff2[256];
     int i = 0;

     if(str != NULL){
         strcpy(buff2, str);
     }else{
         printf("str_replace err!/n");
         return -1;
     }
     memset(buff, 0x00, sizeof(buff));
     while((ptr = strstr( buff2, str_src)) !=0){
         if(ptr-buff2 != 0) memcpy(&buff[i], buff2, ptr - buff2);
         memcpy(&buff[i + ptr - buff2], str_des, strlen(str_des));
         i += ptr - buff2 + strlen(str_des);
         strcpy(buff2, ptr + strlen(str_src));
     }
     strcat(buff,buff2);
     strcpy(str,buff);
     return 0;
 }

