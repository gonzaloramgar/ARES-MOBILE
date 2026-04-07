package com.ares.mobile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.ares.mobile.agent.AgentLoop
import com.ares.mobile.agent.ConversationHistory
import com.ares.mobile.agent.ITool
import com.ares.mobile.agent.ToolRegistry
import com.ares.mobile.ai.GemmaClient
import com.ares.mobile.ai.ModelManager
import com.ares.mobile.ai.ModelRouter
import com.ares.mobile.data.AppDatabase
import com.ares.mobile.tools.AlarmTool
import com.ares.mobile.tools.CameraTool
import com.ares.mobile.tools.ClipboardTool
import com.ares.mobile.tools.LocationTool
import com.ares.mobile.tools.VoiceTool
import kotlinx.serialization.json.Json

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        isLenient = true
    }

    val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { appContext.preferencesDataStoreFile("ares_settings") },
    )

    val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "ares-mobile.db",
    ).build()

    val conversationDao = database.conversationDao()
    val memoryDao = database.memoryDao()
    val scheduledTaskDao = database.scheduledTaskDao()

    private val tools: List<ITool> = listOf(
        ClipboardTool(appContext),
        CameraTool(appContext),
        LocationTool(appContext),
        VoiceTool(appContext),
        AlarmTool(appContext, scheduledTaskDao),
    )

    val toolRegistry = ToolRegistry(tools)
    val conversationHistory = ConversationHistory(conversationDao)
    val modelRouter = ModelRouter(appContext)
    val modelManager = ModelManager(appContext, dataStore, modelRouter)
    val gemmaClient = GemmaClient(appContext, modelManager)
    val agentLoop = AgentLoop(conversationHistory, toolRegistry, gemmaClient, modelManager)
}
