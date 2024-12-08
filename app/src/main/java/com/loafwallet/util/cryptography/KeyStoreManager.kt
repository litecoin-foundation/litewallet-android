package com.loafwallet.util.cryptography

import android.content.Context
import android.security.keystore.UserNotAuthenticatedException
import com.breadwallet.tools.security.BRKeyStore
import com.breadwallet.tools.security.BRKeyStore.AliasObject
import com.breadwallet.tools.security.BRKeyStore.CANARY_ALIAS
import com.breadwallet.tools.security.BRKeyStore.PHRASE_ALIAS
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.security.KeyStore

class KeyStoreManager(
    private val context: Context,
    private val keyGenerator: KeyStoreKeyGenerator,
) {

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(BRKeyStore.ANDROID_KEY_STORE).apply { load(null) }
    }
    private val cipherBox: CipherBox by lazy { CipherBox.Impl() }
    private val cipherStorageFile: CipherStorage by lazy {
        CipherStorageFile(context, cipherBox)
    }
    private val cipherStoragePref: CipherStorage by lazy {
        CipherStoragePref(context, cipherBox)
    }
    private val mutex = Mutex()

    @Throws(UserNotAuthenticatedException::class)
    suspend fun getData(aliasObject: AliasObject): ByteArray? {
        mutex.withLock {
            Timber.d("timber: KeyStoreManager.getData called")
            try {
                var secretKey = keyStore.getKey(aliasObject.alias, null)

                /**
                 * previously we are storing the key on file,
                 * so we need to check it first and migrate to the new format
                 */
                if (cipherStorageFile.hasKey(aliasObject)) {
                    Timber.d("timber: KeyStoreManager.getData key file exists")
                    val decryptedData = cipherStorageFile.getKey(secretKey, aliasObject)

                    secretKey = keyGenerator.generateKey(
                        alias = aliasObject.alias,
                        isAuthRequired = (aliasObject.alias == PHRASE_ALIAS || aliasObject.alias == CANARY_ALIAS),
                        authTimeout = BRKeyStore.AUTH_DURATION_SEC
                    )

                    cipherStoragePref.saveKey(
                        secretKey = secretKey,
                        aliasObject = aliasObject,
                        data = decryptedData!!
                    )
                    Timber.d("timber: KeyStoreManager.getData key migrated")

                    cipherStorageFile.removeKey(aliasObject)

                    return decryptedData.also {
                        Timber.d("timber: KeyStoreManager.getData key migrated: $it")
                    }
                }


                /**
                 * the following are the new format logic that stored on the shared preferences
                 */

                if (cipherStoragePref.hasKey(aliasObject).not()) {
                    Timber.d("timber: KeyStoreManager.getData new key in prefs not exists | ${aliasObject.alias}, ${aliasObject.datafileName}, ${aliasObject.ivFileName}")
                    return null
                }

                val decryptedData = cipherStoragePref.getKey(secretKey, aliasObject)
                Timber.d("timber: KeyStoreManager.getData: decryptedData=${decryptedData}")
                return decryptedData.also {
                    Timber.d("timber: KeyStoreManager.getData key: $it")
                }
            } catch (e: UserNotAuthenticatedException) {
                //TODO: need auth?
                Timber.d("timber: KeyStoreManager.getData: ${e.message}")
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.e(e)
                return null
            }
        }
    }

    @Throws(UserNotAuthenticatedException::class)
    fun getDataBlocking(aliasObject: AliasObject) = runBlocking {
        getData(aliasObject)
    }

}