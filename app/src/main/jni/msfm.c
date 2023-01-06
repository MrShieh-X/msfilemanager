#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <errno.h>

JNIEXPORT jboolean JNICALL Java_com_mrshiehx_file_manager_utils_NativeUtils_isDirectory(
        JNIEnv *env,
        jobject thiz,
        jstring path
) {
    struct stat path_stat;
    const char *cPath = (*env)->GetStringUTFChars(env, path, NULL);
    (*env)->ReleaseStringUTFChars(env, path, cPath);
    if (stat(cPath, &path_stat) == -1) {
        return errno == ELOOP;
    }
    return S_ISDIR(path_stat.st_mode);
}
