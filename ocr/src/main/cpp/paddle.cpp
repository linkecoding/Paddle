#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_cn_codekong_paddle_ocrs_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}