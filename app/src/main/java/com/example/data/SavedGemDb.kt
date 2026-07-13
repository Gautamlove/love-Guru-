package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_gems")
data class SavedGem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "tricky", "gussa", "reply"
    val title: String, // The user's input/question/message
    val analysis: String, // Core analysis / mood analysis / battle plan
    val option1Label: String,
    val option1Text: String,
    val option2Label: String,
    val option2Text: String,
    val option3Label: String,
    val option3Text: String,
    val option4Label: String? = null,
    val option4Text: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SavedGemDao {
    @Query("SELECT * FROM saved_gems ORDER BY timestamp DESC")
    fun getAllGems(): Flow<List<SavedGem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGem(gem: SavedGem)

    @Query("DELETE FROM saved_gems WHERE id = :id")
    suspend fun deleteGemById(id: Int)
}

@Database(entities = [SavedGem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedGemDao(): SavedGemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "love_guru_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class SavedGemRepository(private val savedGemDao: SavedGemDao) {
    val allGems: Flow<List<SavedGem>> = savedGemDao.getAllGems()

    suspend fun insert(gem: SavedGem) {
        savedGemDao.insertGem(gem)
    }

    suspend fun delete(id: Int) {
        savedGemDao.deleteGemById(id)
    }
}
