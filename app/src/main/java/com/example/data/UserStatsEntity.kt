package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val id: Int = 1,
    val streakDays: Int = 1,
    val lastActiveDate: String = "",
    val totalXp: Int = 50,
    val hearts: Int = 5,
    val maxHearts: Int = 5,
    val gems: Int = 120,
    val completedLessonsCount: Int = 0,
    val dailyGoalTarget: Int = 30, // XP target
    val dailyGoalProgress: Int = 15,
    val completedCategories: String = "" // Comma-separated list of completed category IDs
) {
    fun isCategoryCompleted(categoryId: String): Boolean {
        return completedCategories.split(",").map { it.trim() }.contains(categoryId)
    }

    fun withCompletedCategory(categoryId: String): UserStatsEntity {
        val currentSet = completedCategories.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
        currentSet.add(categoryId)
        return copy(completedCategories = currentSet.joinToString(","))
    }
}
