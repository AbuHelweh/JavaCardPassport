/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class jnitestfingerprint_FPrintController */

#ifndef _Included_jnitestfingerprint_FPrintController
#define _Included_jnitestfingerprint_FPrintController
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     jnitestfingerprint_FPrintController
 * Method:    sayHello
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_jnitestfingerprint_FPrintController_sayHello
  (JNIEnv *, jobject);

/*
 * Class:     jnitestfingerprint_FPrintController
 * Method:    init
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jnitestfingerprint_FPrintController_init
  (JNIEnv *, jobject);

/*
 * Class:     jnitestfingerprint_FPrintController
 * Method:    verifyPrint
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jnitestfingerprint_FPrintController_verifyPrint
  (JNIEnv *, jobject);

/*
 * Class:     jnitestfingerprint_FPrintController
 * Method:    scanImage
 * Signature: ()[C
 */
JNIEXPORT jcharArray JNICALL Java_jnitestfingerprint_FPrintController_scanImage
  (JNIEnv *, jobject, jint *, jint *);

/*
 * Class:     jnitestfingerprint_FPrintController
 * Method:    verifyImage
 * Signature: ([CII)Z
 */
JNIEXPORT jboolean JNICALL Java_jnitestfingerprint_FPrintController_verifyImage
  (JNIEnv *, jobject, jcharArray, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
