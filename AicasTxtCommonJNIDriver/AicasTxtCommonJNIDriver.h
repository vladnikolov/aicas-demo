/* DO NOT EDIT THIS FILE - it is machine generated */

#include <jni.h>
/* Header for class com_aicas_fischertechnik_driver_AicasTxtCommonJNIDriver */

#ifndef _Included_com_aicas_fischertechnik_AicasTxtCommonJNIDriver
#define _Included_com_aicas_fischertechnik_AicasTxtCommonJNIDriver
#ifdef __cplusplus
extern "C" {
#endif

/* Class:     com.aicas.fischertechnik.AicasTxtCommonJNIDriver
 * Method:    initTxt
 * Signature: ()I */
#ifdef __cplusplus
extern "C"
#endif
JNIEXPORT jint JNICALL Java_com_aicas_fischertechnik_AicasTxtCommonJNIDriver_initTxt(
		JNIEnv *env, jobject t);

/* Class:     com.aicas.fischertechnik.AicasTxtCommonJNIDriver
 * Method:    uninitTxt
 * Signature: ()I */
#ifdef __cplusplus
extern "C"
#endif
JNIEXPORT jint JNICALL Java_com_aicas_fischertechnik_AicasTxtCommonJNIDriver_uninitTxt(
		JNIEnv *env, jobject t);

/* Class:     com.aicas.fischertechnik.AicasTxtCommonJNIDriver
 * Method:    rotateMotor
 * Signature: (IIII)I */
#ifdef __cplusplus
extern "C"
#endif
JNIEXPORT jint JNICALL Java_com_aicas_fischertechnik_AicasTxtCommonJNIDriver_rotateMotor(
		JNIEnv *env, jobject t, jint id, jint direction, jint speed,
		jint distance);

/* Class:     com.aicas.fischertechnik.AicasTxtCommonJNIDriver
 * Method:    stopMotor
 * Signature: (I)I */
#ifdef __cplusplus
extern "C"
#endif
JNIEXPORT void JNICALL Java_com_aicas_fischertechnik_AicasTxtCommonJNIDriver_stopMotor(
		JNIEnv *env, jobject t, jint id);

/* Class:     com.aicas.fischertechnik.AicasTxtCommonJNIDriver
 * Method:    readInput
 * Signature: (I)I */
#ifdef __cplusplus
extern "C"
#endif
JNIEXPORT jint JNICALL Java_com_aicas_fischertechnik_AicasTxtCommonJNIDriver_readInput(
		JNIEnv *env, jobject t, jint id);

/* Class:     com.aicas.fischertechnik.AicasTxtCommonJNIDriver
 * Method:    writeOutput
 * Signature: (II)Z */
#ifdef __cplusplus
extern "C"
#endif
JNIEXPORT jboolean JNICALL Java_com_aicas_fischertechnik_AicasTxtCommonJNIDriver_writeOutput(
		JNIEnv *env, jobject t, jint id, jint value);

/* Class:     com.aicas.fischertechnik.AicasTxtCommonJNIDriver
 * Method:    readImpulseSamplerCounter
 * Signature: (I)I */
#ifdef __cplusplus
extern "C"
#endif
JNIEXPORT jint JNICALL Java_com_aicas_fischertechnik_AicasTxtCommonJNIDriver_readImpulseSamplerCounter(
		JNIEnv *env, jobject t);

/* Class:     com.aicas.fischertechnik.AicasTxtCommonJNIDriver
 * Method:    resetImpulseSamplerCounter
 * Signature: (I)I */
#ifdef __cplusplus
extern "C"
#endif
JNIEXPORT void JNICALL Java_com_aicas_fischertechnik_AicasTxtCommonJNIDriver_resetImpulseSamplerCounter(
		JNIEnv *env, jobject t);

#ifdef __cplusplus
}
#endif
#endif
