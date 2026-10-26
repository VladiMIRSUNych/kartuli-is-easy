package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val orderIndex: Int = 0,
    val isCustom: Boolean = false
)
