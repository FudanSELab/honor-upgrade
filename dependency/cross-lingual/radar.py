import os

CallKeywords = [
    "RegisterNatives",
    "registerNativeMethods",
    "jniRegisterNativeMethods",
    "RegisterMethodsOrDie",
    # https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/jni/LayoutlibLoader.cpp;drc=c93d02ac1e1aca3ca3956015cdd89e3f48acd7e1;l=174?hl=zh-cn
    "AndroidRuntime::registerNativeMethods",
    "RegisterNativeMethods",
    "jniSQLite3RegisterNativeMethods", # vendor/honor/system/base/frameworks/base/core/jni/hihonor/redb/sqlite_common.h
    "JniRegisterNativeMethods" # vendor/honor/system/base/frameworks/base/packages/HwWifiNetworkService/WiFiEnhance/jni/DcJni.cpp
]

keywords = [
    'jniRegisterNativeMethods',
    'RegisterMethodsOrDie',
    'registerNativeMethods',
    'RegisterNatives',
    'RegisterNativeMethods',
    'JNINativeMethod',
    'jni',
    'JNI'
    'JNI_OnLoad'
    'GetMethodID',
    'CallVoidMethod',
    'CallStaticMethod',
    'CallObjectMethod',
    'env->Call'
]

black_list_path = [
    "prebuilts/jdk/",
    "art/test/"
]

black_list_file = [
    "prebuilts/runtime/mainline/platform/sdk/include/libnativehelper/include_jni/jni.h",
    "external/oj-libjdwp/src/share/javavm/export/jni.h",
    "art/compiler/jni/jni_compiler_test.cc",
    "art/runtime/jni/check_jni.cc",
    "art/runtime/jni/jni_internal_test.cc",
    "libnativehelper/include_jni/jni.h"
]

def radar(directory: str, keywords: list[str] = keywords,
          save_to: str = 'matched_files.txt', overwrite: bool = False) -> list[str]:
    matched_files = []
    if os.path.exists(save_to) and not overwrite:
        with open(save_to, 'r') as f:
            matched_files = f.readlines()
            matched_files = [file.strip() for file in matched_files]
        return matched_files
    for root, dirs, files in os.walk(directory):
        # print(f"Scanning {root}")
        for file in files:
            if not file.endswith('.cpp') and not file.endswith('.cc') and not file.endswith('.h'):
                continue
            is_black = False
            for black in black_list_path:
                if black in root:
                    is_black = True
                    break
            if is_black:
                continue
            file_path = os.path.join(root, file)
            if os.path.islink(file_path):
                continue
            is_black = False
            for black in black_list_file:
                if black in file_path:
                    is_black = True
                    break
            if is_black:
                continue
            try:
                with open(file_path, 'r') as f:
                    content = f.read()
                for kw in keywords:
                    if kw in content:
                        result = file_path.replace(directory, '', 1)
                        if result.startswith('/'):
                            result = result[1:]
                        matched_files.append(result)
                        break
            except UnicodeDecodeError:
                continue
    with open(save_to, 'w') as f:
        for file in matched_files:
            f.write(file + '\n')
    return matched_files
