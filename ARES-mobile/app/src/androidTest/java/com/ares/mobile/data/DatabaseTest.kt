package com.ares.mobile.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var memoryDao: MemoryDao
    private lateinit var conversationDao: ConversationDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        memoryDao = db.memoryDao()
        conversationDao = db.conversationDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndRetrieveMemory() = runTest {
        val entity = MemoryEntity(key = "nombre", value = "Carlos", updatedAt = System.currentTimeMillis())
        memoryDao.upsert(entity)

        val result = memoryDao.observeMemories().first()

        assertEquals(1, result.size)
        assertEquals("Carlos", result.first().value)
    }

    @Test
    fun insertAndRetrieveMessages() = runTest {
        val msg = MessageEntity(role = "User", content = "Hola", timestamp = System.currentTimeMillis())
        conversationDao.insert(msg)

        val result = conversationDao.observeMessages().first()

        assertEquals(1, result.size)
        assertEquals("Hola", result.first().content)
    }

    @Test
    fun deleteMemoryByKey() = runTest {
        memoryDao.upsert(MemoryEntity(key = "ciudad", value = "Madrid", updatedAt = 0))
        memoryDao.deleteByKey("ciudad")

        assertTrue(memoryDao.observeMemories().first().isEmpty())
    }
}