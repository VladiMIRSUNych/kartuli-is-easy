package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY id ASC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE category = :category ORDER BY id ASC")
    fun getWordsByCategory(category: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE category = :category ORDER BY id ASC")
    suspend fun getWordsByCategoryOnce(category: String): List<WordEntity>

    @Query("SELECT * FROM words WHERE nextReviewTimestamp <= :currentTimestamp AND repetitions > 0 ORDER BY nextReviewTimestamp ASC")
    fun getWordsDueForReview(currentTimestamp: Long): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isCustom = 1 ORDER BY id DESC")
    fun getCustomWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE learned = 1 GROUP BY georgian ORDER BY georgian ASC")
    fun getLearnedWords(): Flow<List<WordEntity>>

    @Query("UPDATE words SET learned = 1 WHERE georgian = :georgian")
    suspend fun markWordLearned(georgian: String)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    @Query("SELECT * FROM words")
    suspend fun getAllWordsOnce(): List<WordEntity>

    @Query("SELECT * FROM words WHERE georgian = :georgian LIMIT 1")
    suspend fun getWordByGeorgian(georgian: String): WordEntity?

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)
}
