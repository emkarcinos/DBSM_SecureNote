package emkarcinos.dbsm_securenote.backend

import org.junit.Test
import org.junit.Assert.*

class SecurityTest {

    @Test
    fun hashingTest() {
        val passwordRaw = "password"
        val expectedHash = "B109F3BBBC244EB82441917ED06D618B9008DD09B3BEFD1B5E07394C706A8BB980B1D7785E5976EC049B46DF5F1326AF5A2EA6D103FD07C95385FFAB0CACBC86".toLowerCase()
        val resultHash = Security.generateHash(passwordRaw)
        assertEquals(expectedHash, resultHash)
    }

    @Test
    fun cipherDecipherTestWithPBKDF() {
        val secret = "test"
        val salt = Security.generateSalt()
        val message = "sdasda/ntest"
        val cipher = Security.encryptString(message, secret, salt)
        val decipher = Security.decryptToString(cipher, secret, salt)
        assertEquals(decipher, message)
    }
}