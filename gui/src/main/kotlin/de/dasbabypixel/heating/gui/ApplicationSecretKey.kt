package de.dasbabypixel.heating.gui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.nio.file.attribute.AclEntry
import java.nio.file.attribute.AclEntryPermission
import java.nio.file.attribute.AclEntryType
import java.nio.file.attribute.AclFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.security.SecureRandom
import javax.crypto.KDF
import javax.crypto.spec.HKDFParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.fileAttributesViewOrNull
import kotlin.io.path.getPosixFilePermissions
import kotlin.io.path.readText
import kotlin.io.path.setPosixFilePermissions
import kotlin.io.path.writeText
import kotlin.system.exitProcess

/**
 * Secret key for the entire application. Same key is (usually) used across application runs.
 *
 * The key may be changed manually, in such cases all client sessions will be invalidated.
 */
class ApplicationSecretKey(private val secretKey: ByteArray) {
    fun derive256Data(
        label: String,
        salt: ByteArray
    ): ByteArray {
        return KDF.getInstance("HKDF-SHA256").run {
            deriveData(
                HKDFParameterSpec.ofExtract().addIKM(secretKey).addSalt(salt).thenExpand(label.toByteArray(), 32)
            )
        }
    }

    companion object {
        private val posixPerms = setOf(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
        private val logger: Logger = LoggerFactory.getLogger(ApplicationSecretKey::class.java)
        fun getOrCreateFromFile(path: Path): ApplicationSecretKey {
            if (!path.exists()) { // We generate a 512 byte key. Pretty large but should then we can use it for basically anything without ever having to worry about it being compromised.
                val randomBytes = ByteArray(512)
                SecureRandom.getInstanceStrong().nextBytes(randomBytes)
                val encoded = Base64.encode(randomBytes)
                path.parent?.createDirectories()
                path.createFile()
                restrictFile(path)
                path.writeText(encoded)
            }

            if (!fileRestrictedCorrectly(path)) {
                logger.error("Secret key file has wrong permissions. Too broad.")
                exitProcess(1)
            }

            val encoded = path.readText()
            val bytes = Base64.decode(encoded)
            if (bytes.size != 512) {
                logger.error("Bad content in secret key file")
                exitProcess(1)
            }

            return ApplicationSecretKey(bytes)
        }

        private fun restrictFile(path: Path) {
            var permissionsSet = false

            try {
                path.setPosixFilePermissions(posixPerms)
                permissionsSet = true
            } catch (_: UnsupportedOperationException) {
            }

            if (!permissionsSet) {
                try {
                    path.fileAttributesViewOrNull<AclFileAttributeView>()?.let { aclView ->
                        val owner = aclView.owner
                        val ownerEntry =
                            AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(owner).setPermissions(
                                AclEntryPermission.READ_DATA,
                                AclEntryPermission.WRITE_DATA,
                                AclEntryPermission.APPEND_DATA,
                                AclEntryPermission.READ_ACL,
                                AclEntryPermission.READ_ATTRIBUTES,
                                AclEntryPermission.READ_NAMED_ATTRS,
                                AclEntryPermission.WRITE_ACL,
                                AclEntryPermission.DELETE,
                                AclEntryPermission.WRITE_NAMED_ATTRS,
                                AclEntryPermission.WRITE_ATTRIBUTES,
                                AclEntryPermission.SYNCHRONIZE
                            ).build()
                        aclView.acl = listOf(ownerEntry)
                        permissionsSet = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (!permissionsSet) {
                logger.error("Failed to restrict permissions for secret file")
                exitProcess(1)
            }
        }

        private fun fileRestrictedCorrectly(path: Path): Boolean {
            try {
                val perms = path.getPosixFilePermissions()
                if (perms == posixPerms) return true
            } catch (_: UnsupportedOperationException) {
            }

            try {
                path.fileAttributesViewOrNull<AclFileAttributeView>()?.let { aclView ->
                    val owner = aclView.owner
                    val acl = aclView.acl

                    for (entry in acl) {
                        if (entry.type() == AclEntryType.ALLOW) {
                            val principal = entry.principal()
                            val grantsReadOrWrite = entry.permissions().any {
                                it == AclEntryPermission.READ_DATA || it == AclEntryPermission.WRITE_DATA || it == AclEntryPermission.APPEND_DATA
                            }
                            if (grantsReadOrWrite && principal != owner) {
                                return false
                            }
                        }
                    }
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }
    }
}