package com.loafwallet.util.cryptography

import com.breadwallet.tools.security.BRKeyStore
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec


interface CipherBox {
    fun initEncryptCipher(
        cipherAlgorithm: String,
        secretKey: Key
    ): Cipher

    fun encryptData(cipher: Cipher, data: ByteArray): ByteArray

    fun initDecryptCipher(
        cipherAlgorithm: String,
        secretKey: Key,
        iv: ByteArray
    ): Cipher

    fun decryptData(cipher: Cipher, encryptedData: ByteArray): ByteArray

    class Impl : CipherBox {

        override fun initEncryptCipher(
            cipherAlgorithm: String,
            secretKey: Key,
        ): Cipher {
            val cipher = Cipher.getInstance(cipherAlgorithm)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            return cipher
        }

        override fun encryptData(
            cipher: Cipher,
            data: ByteArray
        ): ByteArray {
            val encryptedData = cipher.doFinal(data)

            return encryptedData
        }

        override fun initDecryptCipher(
            cipherAlgorithm: String,
            secretKey: Key,
            iv: ByteArray
        ): Cipher {
            val cipher = Cipher.getInstance(cipherAlgorithm)
            val spec = if (cipherAlgorithm == BRKeyStore.NEW_CIPHER_ALGORITHM)
                GCMParameterSpec(128, iv)
            else IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            return cipher
        }

        override fun decryptData(
            cipher: Cipher,
            encryptedData: ByteArray
        ): ByteArray {
            return cipher.doFinal(encryptedData)
        }

    }
}