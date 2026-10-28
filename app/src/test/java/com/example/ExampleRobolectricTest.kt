package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.ui.model.LanguageMode
import com.example.ui.model.ZyvexStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies ZYVEX AI name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ZYVEX AI", appName)
    }

    @Test
    fun `database stores and retrieves user and assistant telemetry`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val dao = db.chatMessageDao()

        dao.clearAllMessages()

        val userMsg = ChatMessageEntity(
            text = "Initiate diagnostic scan",
            isUser = true,
            timestamp = System.currentTimeMillis(),
            language = LanguageMode.ENGLISH.code
        )
        val insertedId = dao.insertMessage(userMsg)
        assertTrue(insertedId > 0)

        val assistantMsg = ChatMessageEntity(
            text = "All systems operational, Sir.",
            isUser = false,
            timestamp = System.currentTimeMillis() + 100,
            language = LanguageMode.ENGLISH.code
        )
        dao.insertMessage(assistantMsg)

        val recent = dao.getRecentMessages(10)
        assertEquals(2, recent.size)
        assertEquals("All systems operational, Sir.", recent[0].text)

        dao.clearAllMessages()
        val emptyList = dao.getRecentMessages(10)
        assertEquals(0, emptyList.size)
    }

    @Test
    fun `language modes support multilingual definitions`() {
        assertEquals("auto", LanguageMode.AUTO.code)
        assertEquals("english", LanguageMode.ENGLISH.code)
        assertEquals("urdu", LanguageMode.URDU.code)
        assertEquals("roman_urdu", LanguageMode.ROMAN_URDU.code)
    }
}
