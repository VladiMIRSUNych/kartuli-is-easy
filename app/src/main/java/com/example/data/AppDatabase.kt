package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [WordEntity::class, UserStatsEntity::class, StoryEntity::class, UnitEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun storyDao(): StoryDao
    abstract fun unitDao(): UnitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kartuli_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        if (database.wordDao().getCount() == 0) {
                            populateDatabase(database)
                        }
                    }
                }
            }

            private suspend fun populateDatabase(database: AppDatabase) {
                val wordDao = database.wordDao()
                val userStatsDao = database.userStatsDao()
                val storyDao = database.storyDao()

                // Insert initial everyday vocabulary
                wordDao.insertWords(InitialData.DEFAULT_WORDS)

                // Insert initial gamification stats
                userStatsDao.insertUserStats(
                    UserStatsEntity(
                        id = 1,
                        streakDays = 3,
                        lastActiveDate = "2026-10-17",
                        totalXp = 120,
                        hearts = 5,
                        maxHearts = 5,
                        gems = 150,
                        completedLessonsCount = 2,
                        dailyGoalTarget = 30,
                        dailyGoalProgress = 20
                    )
                )

                // Insert initial reading stories
                storyDao.insertStories(InitialData.DEFAULT_STORIES)

                // Insert initial learning units
                database.unitDao().insertUnits(InitialData.DEFAULT_UNITS)
            }
        }
    }
}
