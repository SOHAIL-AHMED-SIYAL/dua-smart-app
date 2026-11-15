package com.duasmartapp.di

import androidx.room.RoomDatabase
import com.duasmartapp.model.AppSetting
import com.duasmartapp.model.AppUser
import com.duasmartapp.model.dao.SettingDao
import com.duasmartapp.model.dao.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {
    fun seed(db: RoomDatabase) = withContext(Dispatchers.IO) {
        val userDao: UserDao = db.userDao()
        val settingDao: SettingDao = db.settingDao()

        val existingUsers = userDao.getAll().first()
        if (existingUsers.isEmpty()) {
            userDao.insert(AppUser(username = "admin", password = "admin123", role = "ADMIN", isEnabled = true))
            userDao.insert(AppUser(username = "teacher", password = "teacher123", role = "TEACHER", isEnabled = true))
        }

        val existingSettings = settingDao.get("school_name")
        if (existingSettings == null) {
            settingDao.put(AppSetting("app_name", "DUA SMART APP"))
            settingDao.put(AppSetting("school_name", "Dua Public School Chak Siyal"))
            settingDao.put(AppSetting("school_classes", "KG through Class 8"))
            settingDao.put(AppSetting("currency", "PKR"))
            settingDao.put(AppSetting("currency_symbol", "Rs."))
        }
    }
}
