//
// Created by Colin PRO on 2017/12/12.
//

#include <jni.h>
#include <time.h>
#include <inttypes.h>
#include <android/log.h>

#include "polarrRender.h"

#define LOG_TAG "POLARR_JNI"
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

PolarrRender *polarrRender;

unsigned char *as_unsigned_char_array(JNIEnv *env, jbyteArray array) {
    int len = env->GetArrayLength(array);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));

    return buf;
}

jbyteArray as_byte_array(JNIEnv *env, unsigned char *buf, int len) {
    jbyteArray array = env->NewByteArray(len);
    env->SetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));
    return array;
}

long long currentTimeInMilliseconds() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return ((tv.tv_sec * 1000) + (tv.tv_usec / 1000));
}

extern "C"
JNIEXPORT void JNICALL
Java_co_polarr_render_PolarrRenderJni_init(JNIEnv *env, jclass type,
                                           jint width, jint height, jint stride, jint scanline,
                                           jboolean needEgl) {
    polarrRender = new PolarrRender;

    polarrRender->init(needEgl);
    polarrRender->setYUVsize(width, height, stride, scanline);

}

extern "C"
JNIEXPORT void JNICALL
Java_co_polarr_render_PolarrRenderJni_release(JNIEnv *env, jclass type) {
    delete polarrRender;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_co_polarr_render_PolarrRenderJni_updateYUVData(JNIEnv *env, jclass type,
                                                    jbyteArray yuvArr) {
    int len = env->GetArrayLength(yuvArr);
    unsigned char *yuvBytes = as_unsigned_char_array(env, yuvArr);

    long during;
    long long startTime;

    startTime = currentTimeInMilliseconds();

    polarrRender->initFilter(F_COMMON_6);

    during = currentTimeInMilliseconds() - startTime;

    ALOGD("Init filter & size:%"
                  PRId64
                  "ms", during);

    startTime = currentTimeInMilliseconds();

    polarrRender->applyFilter(yuvBytes);
    during = currentTimeInMilliseconds() - startTime;
    ALOGD("APPLY FILTER:%"
                  PRId64
                  "ms", during);

    return as_byte_array(env, yuvBytes, len);
}
