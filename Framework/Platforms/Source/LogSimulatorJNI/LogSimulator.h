/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class LogSimulator */

#ifndef _Included_LogSimulator
#define _Included_LogSimulator
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     LogSimulator
 * Method:    stepForward
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_naoth_rc_LogSimulator_stepForward
  (JNIEnv *, jobject);

/*
 * Class:     LogSimulator
 * Method:    stepBack
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_naoth_rc_LogSimulator_stepBack
  (JNIEnv *, jobject);

/*
 * Class:     LogSimulator
 * Method:    jumpToBegin
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_naoth_rc_LogSimulator_jumpToBegin
  (JNIEnv *, jobject);

/*
 * Class:     LogSimulator
 * Method:    jumpToEnd
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_naoth_rc_LogSimulator_jumpToEnd
  (JNIEnv *, jobject);

/*
 * Class:     LogSimulator
 * Method:    jumpTo
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_de_naoth_rc_LogSimulator_jumpTo
  (JNIEnv *, jobject, jint);

/*
 * Class:     LogSimulator
 * Method:    openLogFile
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_de_naoth_rc_LogSimulator_openLogFile
  (JNIEnv *, jobject, jstring);

/*
 * Class:     LogSimulator
 * Method:    getCurrentFrame
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_naoth_rc_LogSimulator_getCurrentFrame
  (JNIEnv *, jobject);

/*
 * Class:     LogSimulator
 * Method:    getMinFrame
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_naoth_rc_LogSimulator_getMinFrame
  (JNIEnv *, jobject);

/*
 * Class:     LogSimulator
 * Method:    getMaxFrame
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_naoth_rc_LogSimulator_getMaxFrame
  (JNIEnv *, jobject);


JNIEXPORT jbyteArray Java_de_naoth_rc_LogSimulator_getRepresentation(JNIEnv * env, jobject thisObj, jstring name);

#ifdef __cplusplus
}
#endif
#endif
