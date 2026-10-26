package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey
    val id: String,
    val titleGeorgian: String,
    val titleRussian: String,
    val category: String, // e.g. "Ресторан", "Рынок", "Улица"
    val contentGeorgian: String,
    val contentRussian: String,
    val question: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val correctIndex: Int,
    val isCompleted: Boolean = false
)
