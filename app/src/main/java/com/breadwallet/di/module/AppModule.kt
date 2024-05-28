package com.breadwallet.di.module

import com.breadwallet.BreadApp
import com.breadwallet.BreadApp_kt
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

/** Litewallet
 * Created by Mohamed Barry on 6/30/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */
@Module
@DisableInstallInCheck
class AppModule(val app: BreadApp) {

    @Provides
    @Singleton
    fun provideApplication() = app
}
