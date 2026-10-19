package de.dasbabypixel.heating.gui

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

class SessionManager {
    private val secureRandom = SecureRandom()
    private val signingKey: SecretKey =
        KeyGenerator.getInstance("HmacSHA256").also { it.init(256, secureRandom) }.generateKey()
    private val signatureLengthByte = 32

    fun createSession(): Uuid = Uuid.random()

    fun sign(sessionId: Uuid): String {
        val sessionIdBytes = sessionId.toByteArray()
        val signature = Mac.getInstance("HmacSHA256").also { it.init(signingKey) }.doFinal(sessionIdBytes)
        val combined = ByteArray(sessionIdBytes.size + signature.size)
        System.arraycopy(sessionIdBytes, 0, combined, 0, sessionIdBytes.size)
        System.arraycopy(signature, 0, combined, sessionIdBytes.size, signature.size)
        return Base64.encode(combined)
    }

    fun verifyAndGet(token: String): Uuid? {
        return try {
            val decoded = Base64.decode(token)
            if (decoded.size <= signatureLengthByte) return null
            val sessionIdSize = decoded.size - signatureLengthByte
            val sessionIdBytes = decoded.copyOfRange(0, sessionIdSize)
            val signature = decoded.copyOfRange(sessionIdSize, decoded.size)
            val computedSignature = Mac.getInstance("HmacSHA256").also { it.init(signingKey) }.doFinal(sessionIdBytes)
            if (!MessageDigest.isEqual(signature, computedSignature)) {
                return null
            }
            Uuid.fromByteArray(sessionIdBytes)
        } catch (_: Exception) {
            null
        }
    }
}