package com.duasmartapp.di

import android.app.Application
import androidx.lifecycle.coroutineScope
import com.duasmartapp.model.dao.UserDao
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newCoroutineContext
import kotlinx.coroutines.runBlocking

class DuaApp : Application() {
    private val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + Dispatchers.Main.immediate
    )

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(this)
        // Seed default admin/teacher accounts and school settings on first launch.
        scope.launch {
            DatabaseSeeder.seed(db)
        }
    }
}
