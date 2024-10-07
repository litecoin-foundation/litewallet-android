
#include "jni.h"
#include "BRInt.h"
#include "BRPeerManager.h"

#ifndef BREADWALLET_PEERMANAGER_H
#define BREADWALLET_PEERMANAGER_H

#ifdef __cplusplus
extern "C" {
#endif

extern BRPeerManager *_peerManager;

JNIEXPORT void JNICALL
Java_com_breadwallet_wallet_BRPeerManager_create(JNIEnv *env, jobject thiz,
                                                 int earliestKeyTime,
                                                 int blocksCount, int peersCount,
                                                 double fpRate);

JNIEXPORT void JNICALL Java_com_breadwallet_wallet_BRPeerManager_connect(JNIEnv *env, jobject thiz);

JNIEXPORT void JNICALL Java_com_breadwallet_wallet_BRPeerManager_rescan(JNIEnv *env, jobject thiz);

JNIEXPORT void JNICALL
Java_com_breadwallet_wallet_BRPeerManager_putBlock(JNIEnv *env, jobject thiz,
                                                   jbyteArray block, int blockHeight);

JNIEXPORT void JNICALL
Java_com_breadwallet_wallet_BRPeerManager_createBlockArrayWithCount(JNIEnv *env,
                                                                    jobject thiz,
                                                                    jint blockCount);

JNIEXPORT void JNICALL
Java_com_breadwallet_wallet_BRPeerManager_putPeer(JNIEnv *env, jobject thiz,
                                                  jbyteArray peerAddress,
                                                  jbyteArray peerPort,
                                                  jbyteArray peerTimeStamp);

JNIEXPORT void JNICALL
Java_com_breadwallet_wallet_BRPeerManager_createPeerArrayWithCount(JNIEnv *env,
                                                                   jobject thiz,
                                                                   jint peerCount);

JNIEXPORT jboolean JNICALL Java_com_breadwallet_wallet_BRPeerManager_isCreated(JNIEnv *env,
                                                                               jobject obj);

JNIEXPORT jdouble JNICALL
Java_com_breadwallet_wallet_BRPeerManager_syncProgress(JNIEnv *env, jclass thiz,
                                                       int startHeight);

JNIEXPORT jint JNICALL Java_com_breadwallet_wallet_BRPeerManager_getCurrentBlockHeight(JNIEnv *env,
                                                                                       jclass thiz);

JNIEXPORT jint JNICALL Java_com_breadwallet_wallet_BRPeerManager_getEstimatedBlockHeight(
        JNIEnv *env, jclass thiz);

JNIEXPORT jlong JNICALL Java_com_breadwallet_wallet_BRPeerManager_getLastBlockTimestamp(
        JNIEnv *env, jobject thiz);

JNIEXPORT void JNICALL Java_com_breadwallet_wallet_BRPeerManager_peerManagerFreeEverything(
        JNIEnv *env, jobject thiz);

JNIEXPORT void JNICALL Java_com_breadwallet_presenter_activities_IntroActivity_testCore(JNIEnv *env,
                                                                                        jobject instance);

JNIEXPORT jboolean JNICALL Java_com_breadwallet_wallet_BRPeerManager_isConnected(JNIEnv *env,
                                                                                 jobject obj);

JNIEXPORT jint JNICALL Java_com_breadwallet_wallet_BRPeerManager_getRelayCount(JNIEnv *env,
                                                                               jclass thiz,
                                                                               jbyteArray txHash);

JNIEXPORT jboolean JNICALL Java_com_breadwallet_wallet_BRPeerManager_setFixedPeer(
        JNIEnv *env, jobject thiz, jstring node, jint port);

JNIEXPORT jstring JNICALL Java_com_breadwallet_wallet_BRPeerManager_getCurrentPeerName(
        JNIEnv *env, jobject thiz);

#ifdef __cplusplus
}
#endif

#endif //BREADWALLET_PEERMANAGER_H
