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
    fun noteSaveLoadTest(){
        val user = User("user", "pass")
        val note = Note("text", user)
        user.note = note
        FileManager.saveNote(note)
        val loadedNote = FileManager.readNote(user)
        assertEquals(note.noteText, loadedNote)
    }

    @Test
    fun userSaveLoadTest(){
        val user = UserManager.createNewUser("password")!!
        FileManager.saveUserData(user)
        val loadedUser = FileManager.grabUser()
        assertEquals(user.passwordHash, loadedUser?.passwordHash)
        assertEquals(user.salt, loadedUser?.salt)
    }
}