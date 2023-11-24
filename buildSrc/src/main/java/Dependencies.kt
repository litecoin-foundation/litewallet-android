object Versions {
    const val kotlin = "1.3.71"
    const val gradlePlugin = "3.6.2"
    const val androidxAppCompat = "1.6.1"
    const val androidxCore = "1.12.0"
    const val androidx = "1.1.5"
    const val androidxExtJunit = "1.1.5"
    const val androidxJunit = "4.13.2"
    const val espressoCore = "3.5.1"
    const val androidxConstraintLayout = "2.1.4"
    const val jetbrainsCoRoutines = "1.7.3"
    const val jetbrainsKotlinReflect = "1.9.20"
    const val commonsIOCommit = "20030203.000550"
    const val jettyWebapp = "11.0.18"
    const val jettyWebsocket = "9.2.19.v20160908"
    const val jettyContinuation = "9.2.19.v20160908"
    const val slf4jAPI = "2.0.9"
    const val androidxLegacy = "1.0.0"
    const val recyclerview ="1.3.2"
    const val constraintlayout = "2.1.4"
    const val gridlayout = "1.0.0"
    const val cardView = "1.0.0"
    const val preferenceKTX = "1.2.1"
    const val browser = "1.6.0"
    const val material = "1.10.0"
    const val lifecycleExtensions = "2.2.0"
    const val lifecycleRuntimeKtx = "2.6.2"
    const val zxingCore = "3.5.2"
    const val jbsdiff = "1.0"
    const val okhttp = "4.3.1"
    const val firebaseBom = "32.5.0"
    const val firebaseAnalytics = ""
    const val firebaseCrashlytics = ""
    const val firebaseCrashlyticsNDK = ""
    const val timber = "5.0.1"
    const val resolution = "5.0.0"
    const val progressbutton = "2.1.0"
    const val dagger = "2.48.1"
    const val daggerCompiler = "2.48.1"
    const val playCore = "1.10.3"
    const val corektx = "1.8.1"
    const val ktlint = "0.44.0"
    const val lifecycleViewmodel = "2.6.2"

    //class path
    const val kotlinVersion = "1.6.21"
    const val androidTools = "8.1.0"
    const val kotlinGradlePlugin = "$kotlinVersion"
    const val googleServices = "4.3.14"
    const val firebaseCrashlyticsGradle = "2.9.9"
}
object Deps {

    // Test
    const val androidTestImplementationExtension =  "androidx.test.ext:${Versions.androidxExtJunit}"
    const val androidTestImplementationJunit = "junit:junit:${Versions.androidxJunit}"
    const val androidTestImplementationEspresso = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"

    // jetbrains
    const val jetbrainsKotlinCoRoutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.jetbrainsCoRoutines}"
    const val jetbrainsKotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.jetbrainsKotlinReflect}"

    // commons
        const val commonsio = "commons-io:commons-io:${Versions.commonsIOCommit}"
        const val jettyWebapp = "org.eclipse.jetty:jetty-webapp:${Versions.jettyWebapp}"
        const val websocketJettyWebapp = "org.eclipse.jetty.websocket:jetty-webapp:${Versions.jettyWebsocket}"
        const val webJettyContinuation = "org.eclipse.jetty:jetty-continuation:${Versions.jettyContinuation}"
        const val slf4j = "org.slf4j:slf4j-api:${Versions.slf4jAPI}"

    // androidx
        const val androidxCore = "androidx.core:core-ktx:${Versions.androidxCore}"
        const val androidX = "androidx.core:core-ktx:${Versions.androidx}"
        const val androidxAppCompat = "androidx.appcompat:appcompat:${Versions.androidxAppCompat}"
        const val androidxConstraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.androidxConstraintLayout}"
        const val androidxLegacy = "androidx.legacy:legacy-support-v13:${Versions.androidxLegacy}"
        const val recyclerview = "androidx.recyclerview:recyclerview:${Versions.recyclerview}"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}"
        const val gridlayout = "androidx.gridlayout:gridlayout:${Versions.gridlayout}"
        const val androidxCardview = "androidx.cardview:cardview:${Versions.cardView}"

    // preferences
        const val  preferenceKTX = "androidx.preference:preference-ktx:${Versions.preferenceKTX}"

    // ChromeCustomTabs
        const val  browserTabs = "androidx.browser:browser:${Versions.browser}"

    // material design
        const val  androidMaterial = "com.google.android.material:material:${Versions.material}"
        const val  lifecycleExten = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycleExtensions}"
        const val  runtimeKTX = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleRuntimeKtx}"
        const val  zxingCore = "com.google.zxing:core:${Versions.zxingCore}"
        const val  sigpipeJbsdiff = "io.sigpipe:jbsdiff:${Versions.jbsdiff}"
        const val  okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"

        // Firebase
        const val  firebaseBOM = "com.google.firebase:firebase-bom:${Versions.firebaseBom}"
        const val  firebaseAnalytics = "com.google.firebase:firebase-analytics:${Versions.firebaseAnalytics}"
        const val  firebaseCrashlytics = "com.google.firebase:firebase-analytics:${Versions.firebaseCrashlytics}"
        const val  firebaseCrashlyticsndk = "com.google.firebase:firebase-analytics:${Versions.firebaseCrashlyticsNDK}"

        // Timber
        const val  jakewhartonTimber = "com.jakewharton.timber:timber:${Versions.timber}"

        // Unstoppable domain
        const val  unstoppableDomains = "com.unstoppabledomains:resolution:${Versions.resolution}"

        // Progress Button
        const val  progress = "com.github.razir.progressbutton:progressbutton:${Versions.progressbutton}"

        // Dagger
        const val  daggerTest = "com.google.dagger:dagger:${Versions.dagger}"

       //was kapt
        const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.daggerCompiler}"

        // Play Core
        const val playCore = "com.google.android.play:core:${Versions.playCore}"
        const val playCoreKtx = "com.google.android.play:core-ktx:${Versions.corektx}"

        // Ktlint
        const val ktlinter = "com.pinterest:ktlint:${Versions.ktlint}"

        // kotlin
        const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel:${Versions.lifecycleViewmodel}"
        const val lifecycleViewModelKTX = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycleViewmodel}"

        // classpath
        const val androidTools = "com.android.tools.build:gradle:${Versions.androidTools}"
        const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
        const val googleServices = "com.android.tools.build:gradle:${Versions.googleServices}"
        const val firebaseCrashlyticsGradle = "com.android.tools.build:gradle:${Versions.firebaseCrashlyticsGradle}"
}
object ConfigVersions {
        const val compileSdk = 33
        const val minSDK = 30
        const val targetSDK = 33
        const val code = 736
        const val name = "v2.9.0"
        const val multiDexEnabled = true
}