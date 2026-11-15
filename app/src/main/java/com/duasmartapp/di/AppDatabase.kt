package com.duasmartapp.di

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.duasmartapp.model.AttendanceRecord
import com.duasmartapp.model.AppSetting
import com.duasmartapp.model.AppUser
import com.duasmartapp.model.FeeRecord
import com.duasmartapp.model.Holiday
import com.duasmartapp.model.Student
import com.duasmartapp.model.dao.AttendanceDao
import com.duasmartapp.model.dao.FeeDao
import com.duasmartapp.model.dao.HolidayDao
import com.duasmartapp.model.dao.SettingDao
import com.duasmartapp.model.dao.StudentDao
import com.duasmartapp.model.dao.UserDao

@Database(
    entities = [
        Student::class,
        AppUser::class,
        AttendanceRecord::class,
        Holiday::class,
        FeeRecord::class,
        AppSetting::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun userDao(): UserDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun holidayDao(): HolidayDao
    abstract fun feeDao(): FeeDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dua_smart_app.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
