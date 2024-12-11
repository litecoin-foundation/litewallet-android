package com.litewallet.di

import com.breadwallet.tools.manager.BRApiManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.litewallet.data.source.RemoteConfigSource

class Module(
    val remoteConfigSource: RemoteConfigSource = provideRemoteConfigSource(),
    val apiManager: BRApiManager = provideBRApiManager(remoteConfigSource)
)

private fun provideBRApiManager(remoteConfigSource: RemoteConfigSource): BRApiManager {
    return BRApiManager(remoteConfigSource)
}

private fun provideRemoteConfigSource(): RemoteConfigSource {
    return RemoteConfigSource.FirebaseImpl(Firebase.remoteConfig)
}
