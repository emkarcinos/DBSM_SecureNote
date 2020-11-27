package emkarcinos.dbsm_securenote.backend

import junit.framework.Assert.assertEquals
import org.junit.*
import java.io.File

class FileManagerTest {
    companion object{
        private val folder = File("./testfiles")

        @BeforeClass @JvmStatic
        fun init(){
            folder.mkdir()
            FileManager.init(folder)
        }

        @AfterClass @JvmStatic
        fun after(){
            deleteDirectory(folder)
        }

        private fun deleteDirectory(directoryToBeDeleted: File): Boolean {
            val allContents = directoryToBeDeleted.listFiles()
            if (allContents != null) {
                for (file in allContents) {
                    deleteDirectory(file)
                }
            }
            return directoryToBeDeleted.delete()
        }
    }

    @Test
    fun writeBytesReadBytesIdentityTest(){
        val placeholderData = "test".toByteArray()
        //FileManager.saveBytes("test", placeholderData)
        //val readData = FileManager.readRawBytes("test")
        //assertArrayEquals(placeholderData, readData)
    }

    @Test
    fun userSaveLoadTest(){
        val username = "user"
        val user = User(username, "pass")
        FileManager.saveUserData(user)
        val loadedUser = FileManager.grabUser(username)
        assertEquals(user.passwordHash, loadedUser?.passwordHash)
        assertEquals(user.salt, loadedUser?.salt)
    }
}