LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
        $(call all-logtags-files-under, src)

LOCAL_MODULE := settings-logtags

include $(BUILD_STATIC_JAVA_LIBRARY)

# Build the Settings APK
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := Settings
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_MODULE_TAGS := optional
LOCAL_USE_AAPT2 := true

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    $(call all-java-files-under,../../../external/koush/Widgets/Widgets/src) \
    $(call all-java-files-under,../../../external/koush/Superuser/Superuser/src)

LOCAL_STATIC_ANDROID_LIBRARIES := \
    android-support-v4 \
    android-support-v13 \
    android-support-v7-appcompat \
    android-support-v7-cardview \
    android-support-v7-preference \
    android-support-v7-recyclerview \
    android-support-v14-preference

LOCAL_JAVA_LIBRARIES := \
    bouncycastle \
    core-oj \
    telephony-common \
    ims-common

LOCAL_STATIC_JAVA_LIBRARIES := \
    jsr305 \
    settings-logtags

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res \
    external/koush/Widgets/Widgets/res \
    external/koush/Superuser/Superuser/res

LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true

LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages com.koushikdutta.superuser:com.koushikdutta.widgets

ifneq ($(INCREMENTAL_BUILDS),)
    LOCAL_PROGUARD_ENABLED := disabled
    LOCAL_JACK_ENABLED := incremental
    LOCAL_JACK_FLAGS := --multi-dex native
endif

include frameworks/opt/setupwizard/library/common-gingerbread.mk
include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
ifeq (,$(ONE_SHOT_MAKEFILE))
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
