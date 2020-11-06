package emkarcinos.dbsm_securenote.backend

import org.junit.Before
import org.junit.Test
import java.io.File
import org.junit.Assert.*

class NoteTest {

    @Before
    fun init(){
        FileManager.directory = File("./")
    }

    @Test
    fun noteSavingLoadingTest() {
        val secret = "pass"
        val user = User("maciek", Security.generateHash(secret))

        val note = Note(user, secret)

        note.saveNote("test\nnote", secret)

        val loadedNote = Note(user, secret)

        assertEquals(note.noteText, loadedNote.noteText)
    }
}