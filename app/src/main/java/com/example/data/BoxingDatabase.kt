package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [PresetWorkout::class, WorkoutHistory::class], version = 1, exportSchema = false)
abstract class BoxingDatabase : RoomDatabase() {
    abstract fun boxingDao(): BoxingDao

    companion object {
        @Volatile
        private var INSTANCE: BoxingDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BoxingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BoxingDatabase::class.java,
                    "boxing_database"
                )
                .addCallback(BoxingDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class BoxingDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.boxingDao()
                    // Prepopulate standard presets
                    dao.insertPreset(
                        PresetWorkout(
                            name = "Amateur Match",
                            rounds = 3,
                            workSeconds = 120,
                            restSeconds = 60,
                            prepSeconds = 10,
                            warningSeconds = 10,
                            isCustom = false
                        )
                    )
                    dao.insertPreset(
                        PresetWorkout(
                            name = "Professional Match",
                            rounds = 12,
                            workSeconds = 180,
                            restSeconds = 60,
                            prepSeconds = 10,
                            warningSeconds = 10,
                            isCustom = false
                        )
                    )
                    dao.insertPreset(
                        PresetWorkout(
                            name = "HIIT / Tabata",
                            rounds = 8,
                            workSeconds = 20,
                            restSeconds = 10,
                            prepSeconds = 5,
                            warningSeconds = 5,
                            isCustom = false
                        )
                    )
                    dao.insertPreset(
                        PresetWorkout(
                            name = "Speed Sparring",
                            rounds = 6,
                            workSeconds = 120,
                            restSeconds = 30,
                            prepSeconds = 10,
                            warningSeconds = 10,
                            isCustom = false
                        )
                    )
                }
            }
        }
    }
}
