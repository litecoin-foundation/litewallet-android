package com.litewallet.di

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.litewallet.data.source.RemoteConfigSource

class Module(
    val remoteConfigSource: RemoteConfigSource = provideRemoteConfigSource()
)

fun provideRemoteConfigSource(): RemoteConfigSource {
    return RemoteConfigSource.FirebaseImpl(Firebase.remoteConfig)
}
