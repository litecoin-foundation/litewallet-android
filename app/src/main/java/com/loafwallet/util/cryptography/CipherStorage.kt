@file:OptIn(ExperimentalEncodingApi::class)

package com.loafwallet.util.cryptography

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import com.breadwallet.tools.security.BRKeyStore
import com.breadwallet.tools.security.BRKeyStore.AliasObject
import java.io.File
import java.security.Key
import kotlin.io.encoding.ExperimentalEncodingApi

interface CipherStorage {
    fun saveKey(secretKey: Key, aliasObject: AliasObject, data: ByteArray)
    fun getKey(secretKey: Key, aliasObject: AliasObject): ByteArray?
    fun hasKey(aliasObject: AliasObject): Boolean
    fun removeKey(aliasObject: AliasObject)
    fun removeAll()
}

class CipherStoragePref(
    private val context: Context,
    private val cipherBox: CipherBox,
) : CipherStorage {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(BRKeyStore.KEY_STORE_PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun saveKey(secretKey: Key, aliasObject: AliasObject, data: ByteArray) {
        val cipherEncrypt = cipherBox.initEncryptCipher(
            cipherAlgorithm = BRKeyStore.NEW_CIPHER_ALGORITHM,
            secretKey = secretKey
        )

        val encryptedData = cipherBox.encryptData(
            cipher = cipherEncrypt,
            data = data
        )

        saveEncryptedData(aliasObject.ivFileName, cipherEncrypt.iv)
        saveEncryptedData(aliasObject.alias, encryptedData)
    }

    override fun getKey(secretKey: Key, aliasObject: AliasObject): ByteArray? {
        val encryptedData = getEncryptedData(aliasObject.alias) ?: return null
        val iv = getEncryptedData(aliasObject.ivFileName) ?: return null

        val cipherDecrypt = cipherBox.initDecryptCipher(
            cipherAlgorithm = BRKeyStore.NEW_CIPHER_ALGORITHM,
            secretKey = secretKey,
            iv = iv
        )
        val decryptedData = cipherBox.decryptData(
            cipher = cipherDecrypt,
            encryptedData = encryptedData
        )

        return decryptedData
    }

    override fun hasKey(aliasObject: AliasObject): Boolean {
        return sharedPreferences.contains(aliasObject.alias) && sharedPreferences.contains(
            aliasObject.ivFileName
        )
    }

    override fun removeKey(aliasObject: AliasObject) {
        return sharedPreferences.edit {
            remove(aliasObject.ivFileName)
            remove(aliasObject.alias)
        }
    }

    override fun removeAll() {
        sharedPreferences.edit { clear() }
    }

    private fun getEncryptedData(alias: String): ByteArray? {
        return sharedPreferences.getString(alias, null)?.let {
            Base64.decode(it, Base64.DEFAULT)
        }
    }

    private fun saveEncryptedData(alias: String, data: ByteArray) {
        sharedPreferences.edit {
            putString(
                alias,
                Base64.encodeToString(data, Base64.DEFAULT)
            )
        }
    }
}

class CipherStorageFile(
    private val context: Context,
    private val cipherBox: CipherBox,
) : CipherStorage {

    override fun saveKey(secretKey: Key, aliasObject: AliasObject, data: ByteArray) {
        //no-op
    }

    override fun getKey(secretKey: Key, aliasObject: AliasObject): ByteArray? {
        if (hasKey(aliasObject).not()) {
            return null
        }

        val aliasFile = getFile(aliasObject.datafileName)
        val aliasIvFile = getFile(aliasObject.ivFileName)

        val cipherDecrypt = cipherBox.initDecryptCipher(
            cipherAlgorithm = BRKeyStore.CIPHER_ALGORITHM,
            secretKey = secretKey,
            iv = aliasIvFile.readBytes()
        )

        val decryptedData = cipherBox.decryptData(
            cipher = cipherDecrypt,
            encryptedData = aliasFile.readBytes()
        )

        return decryptedData

    }

    override fun hasKey(aliasObject: AliasObject): Boolean {
        return getFile(aliasObject.datafileName).exists() && getFile(aliasObject.ivFileName).exists()
    }

    override fun removeKey(aliasObject: AliasObject) {
        val aliasFile = getFile(aliasObject.datafileName)
        val aliasIvFile = getFile(aliasObject.ivFileName)

        if (aliasFile.exists()) {
            aliasFile.delete()
        }

        if (aliasIvFile.exists()) {
            aliasIvFile.delete()
        }
    }

    override fun removeAll() {
        //no-op
    }

    private fun getFile(fileName: String): File = File(getFilePath(fileName))

    private fun getFilePath(fileName: String): String {
        val filesDir = context.filesDir.absolutePath
        return "${filesDir}${File.separator}${fileName}"
    }

}