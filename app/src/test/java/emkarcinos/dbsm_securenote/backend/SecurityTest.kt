package emkarcinos.dbsm_securenote.backend

import org.junit.Test
import org.junit.Assert.*

class SecurityTest {

    @Test
    fun hashingTest() {
        val passwordRaw = "password"
        val expectedHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
        val resultHash = Security.generateHash(passwordRaw)
        assertEquals(expectedHash, resultHash)
    }
}