#include "jni.h"

#ifndef BREADWALLET_CORE_H
#define BREADWALLET_CORE_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jbyteArray JNICALL
Java_com_breadwallet_tools_security_BitcoinUrlHandler_parsePaymentRequest(JNIEnv *env, jobject obj, jbyteArray payment);

JNIEXPORT jbyteArray JNICALL
Java_com_breadwallet_tools_security_BitcoinUrlHandler_getCertificatesFromPaymentRequest(JNIEnv *env, jobject obj,
                                                                                     jbyteArray payment, jint index);

JNIEXPORT jstring JNICALL
Java_com_breadwallet_tools_security_BitcoinUrlHandler_parsePaymentACK(JNIEnv *env, jobject obj, jbyteArray paymentACK);

#ifdef __cplusplus
}
#endif

#endif //BREADWALLET_CORE_H
