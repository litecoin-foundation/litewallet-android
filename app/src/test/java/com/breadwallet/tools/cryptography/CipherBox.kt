package com.breadwallet.tools.cryptography

import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

interface CipherBox {
    fun initEncryptCipher(
        cipherAlgorithm: String,
        key: Key
    ): Cipher

    fun encryptData(cipher: Cipher, data: String): EncryptedOutput

    fun initDecryptCipher(
        cipherAlgorithm: String,
        key: Key,
        encryptedOutput: EncryptedOutput
    ): Cipher

    fun decryptData(cipher: Cipher, encryptedOutput: EncryptedOutput): ByteArray

    class Impl : CipherBox {

        override fun initEncryptCipher(
            cipherAlgorithm: String,
            key: Key,
        ): Cipher {
            val cipher = Cipher.getInstance(cipherAlgorithm)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return cipher
        }

        override fun encryptData(
            cipher: Cipher,
            data: String
        ): EncryptedOutput {
            val encryptedData = cipher.doFinal(data.encodeToByteArray())

            return EncryptedOutput(
                iv = cipher.iv,
                encryptedData = encryptedData,
            )
        }

        override fun initDecryptCipher(
            cipherAlgorithm: String,
            key: Key,
            encryptedOutput: EncryptedOutput
        ): Cipher {
            val cipher = Cipher.getInstance(cipherAlgorithm)
            val spec = GCMParameterSpec(128, encryptedOutput.iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            return cipher
        }

        override fun decryptData(
            cipher: Cipher,
            encryptedOutput: EncryptedOutput
        ): ByteArray {
            return cipher.doFinal(encryptedOutput.encryptedData)
        }

    }
}

class EncryptedOutput(
    val iv: ByteArray,
    val encryptedData: ByteArray
)