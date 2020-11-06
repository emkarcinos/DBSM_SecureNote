package emkarcinos.dbsm_securenote.backend

import org.junit.Before
import org.junit.Test
import java.io.File
import org.junit.Assert.*

class FileManagerTest {

    @Before
    fun init(){
        FileManager.directory = File("./")
    }

    @Test
    fun writeBytesReadBytesIdentityTest(){
        val placeholderData = "test".toByteArray()
        FileManager.saveBytes("test", placeholderData)
        val readData = FileManager.readRawBytes("test")
        assertArrayEquals(placeholderData, readData)
    }
}