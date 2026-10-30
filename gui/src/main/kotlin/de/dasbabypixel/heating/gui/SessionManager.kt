package de.dasbabypixel.heating.gui

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.time.Instant
import kotlin.uuid.Uuid

class SessionManager(applicationSecretKey: ApplicationSecretKey) {
    private val signingKey: ByteArray = applicationSecretKey.derive256Data(
        "jwt-session-signing-key", SessionManager::class.java.name.toByteArray()
    )
    private val algorithm: Algorithm = Algorithm.HMAC256(signingKey)
    private val verifier: JWTVerifier =
        JWT.require(algorithm).withClaimPresence("username").withClaimPresence("user-id").build()
    private val signatureLengthByte = 32

    fun createSession(): Uuid = Uuid.random()

    fun sign(principal: UserPrincipal): String {
        return JWT.create()
            .withIssuedAt(Instant.now())
            .withClaim("username", principal.username)
            .withClaim("user-id", principal.userId.toHexString())
            .sign(algorithm)
    }

    fun verifyAndGet(token: String): UserPrincipal? {
        try {
            val decoded = verifier.verify(token)
            val username = decoded.getClaim("username").asString()
            val userId = decoded.getClaim("user-id").asString().let { Uuid.parseHex(it) }
            return UserPrincipal(username, userId)
        } catch (e: JWTVerificationException) {
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // fun sign(sessionId: Uuid): String {
    //     val sessionIdBytes = sessionId.toByteArray()
    //     val signature = createMac().doFinal(sessionIdBytes)
    //     val combined = ByteArray(sessionIdBytes.size + signature.size)
    //     System.arraycopy(sessionIdBytes, 0, combined, 0, sessionIdBytes.size)
    //     System.arraycopy(signature, 0, combined, sessionIdBytes.size, signature.size)
    //     return Base64.encode(combined)
    // }
    //
    // fun verifyAndGet(token: String): Uuid? {
    //     return try {
    //         val decoded = Base64.decode(token)
    //         if (decoded.size <= signatureLengthByte) return null
    //         val sessionIdSize = decoded.size - signatureLengthByte
    //         val sessionIdBytes = decoded.copyOfRange(0, sessionIdSize)
    //         val signature = decoded.copyOfRange(sessionIdSize, decoded.size)
    //         val computedSignature = createMac().doFinal(sessionIdBytes)
    //         if (!MessageDigest.isEqual(signature, computedSignature)) {
    //             return null
    //         }
    //         Uuid.fromByteArray(sessionIdBytes)
    //     } catch (_: Exception) {
    //         null
    //     }
    // }
    //
    // private fun createMac(): Mac = Mac.getInstance("HmacSHA256").also { it.init(signingKey) }
}