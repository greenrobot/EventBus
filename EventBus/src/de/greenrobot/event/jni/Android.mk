LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)
 
LOCAL_MODULE    := method
LOCAL_SRC_FILES := Method.c
LOCAL_LDLIBS := -llog -ldl
include $(BUILD_SHARED_LIBRARY)


