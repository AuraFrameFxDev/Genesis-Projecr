#include <jni.h>
#include <string>
#include <algorithm>
#include <android/log.h>

#define LOG_TAG "LanguageIdJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Initializes the native language identifier and logs the provided model path.
 */
JNIEXPORT jstring JNICALL
Java_com_example_app_language_LanguageIdentifier_nativeInitialize(
        JNIEnv *env,
        jobject /* this */,
        jstring modelPath) {
    const char *path = env->GetStringUTFChars(modelPath, nullptr);
    if (path == nullptr) {
        return env->NewStringUTF("");
    }

    LOGI("Initializing with model path: %s", path);

    env->ReleaseStringUTFChars(modelPath, path);
    return env->NewStringUTF("1.2.0");
}

/**
 * @brief Detects the language of the input text using heuristic keyword and character analysis.
 */
JNIEXPORT jstring JNICALL
Java_com_example_app_language_LanguageIdentifier_nativeDetectLanguage(
        JNIEnv *env,
        jobject /* this */,
        jlong handle,
        jstring text) {
    if (text == nullptr) {
        return env->NewStringUTF("und");
    }

    const char *nativeText = env->GetStringUTFChars(text, nullptr);
    if (nativeText == nullptr) {
        return env->NewStringUTF("und");
    }

    LOGI("Detecting language for text: %s", nativeText);

    std::string textStr(nativeText);
    std::string result = "en"; // Default to English

    // Convert to lowercase for case-insensitive matching
    std::transform(textStr.begin(), textStr.end(), textStr.begin(), ::tolower);

    // Language detection based on common words, articles, and patterns
    if (textStr.find(" el ") != std::string::npos ||
        textStr.find(" la ") != std::string::npos ||
        textStr.find(" de ") != std::string::npos ||
        textStr.find(" que ") != std::string::npos ||
        textStr.find(" es ") != std::string::npos ||
        textStr.find(" con ") != std::string::npos ||
        textStr.find(" y ") != std::string::npos ||
        textStr.find(" en ") != std::string::npos ||
        textStr.find(" un ") != std::string::npos ||
        textStr.find(" una ") != std::string::npos) {
        result = "es"; // Spanish
    } else if (textStr.find(" le ") != std::string::npos ||
               textStr.find(" et ") != std::string::npos ||
               textStr.find(" ce ") != std::string::npos ||
               textStr.find(" qui ") != std::string::npos ||
               textStr.find(" avec ") != std::string::npos ||
               textStr.find(" est ") != std::string::npos ||
               textStr.find(" dans ") != std::string::npos ||
               textStr.find(" pour ") != std::string::npos) {
        result = "fr"; // French
    } else if (textStr.find(" und ") != std::string::npos ||
               textStr.find(" der ") != std::string::npos ||
               textStr.find(" die ") != std::string::npos ||
               textStr.find(" das ") != std::string::npos ||
               textStr.find(" mit ") != std::string::npos ||
               textStr.find(" ist ") != std::string::npos ||
               textStr.find(" ein ") != std::string::npos ||
               textStr.find(" eine ") != std::string::npos ||
               textStr.find(" auf ") != std::string::npos ||
               textStr.find(" von ") != std::string::npos) {
        result = "de"; // German
    } else if (textStr.find(" il ") != std::string::npos ||
               textStr.find(" che ") != std::string::npos ||
               textStr.find(" per ") != std::string::npos ||
               textStr.find(" sono ") != std::string::npos ||
               textStr.find(" in ") != std::string::npos ||
               textStr.find(" non ") != std::string::npos) {
        result = "it"; // Italian
    } else if (textStr.find(" para ") != std::string::npos ||
               textStr.find(" com ") != std::string::npos ||
               textStr.find(" em ") != std::string::npos ||
               textStr.find(" um ") != std::string::npos ||
               textStr.find(" uma ") != std::string::npos) {
        result = "pt"; // Portuguese
    }

    // Additional character frequency analysis for better accuracy
    int accentCount = 0;
    for (char c: textStr) {
        if (c < 0 || c > 127) accentCount++; // Non-ASCII characters
    }

    // If significant portion contains non-ASCII characters and no language detected
    if (accentCount > static_cast<int>(textStr.length() * 0.1) && result == "en") {
        result = "mul"; // Multiple/unknown with accents
    }

    env->ReleaseStringUTFChars(text, nativeText);
    return env->NewStringUTF(result.c_str());
}

/**
 * @brief Placeholder function for releasing resources tied to a language identifier handle.
 */
JNIEXPORT void JNICALL
Java_com_example_app_language_LanguageIdentifier_nativeRelease(
        JNIEnv *env,
        jobject /* this */,
        jlong handle) {
    if (handle != 0) {
        LOGI("Language identifier resources cleaned up for handle: %lld", (long long) handle);
    }
}

/**
 * @brief Retrieves the current version of the native language identifier library.
 */
JNIEXPORT jstring JNICALL
Java_com_example_app_language_LanguageIdentifier_nativeGetVersion(
        JNIEnv *env,
        jclass /* clazz */) {
    return env->NewStringUTF("1.2.0");
}

#ifdef __cplusplus
}
#endif
