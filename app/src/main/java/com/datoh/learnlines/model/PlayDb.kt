package com.datoh.learnlines.model

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.datoh.learnlines.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.IOException

@Entity(tableName = "play")
data class PlayItem(
    @PrimaryKey
    val name: String,
    val content: String = "",
)

@Entity(tableName = "play_info",
    foreignKeys = [ForeignKey(
    entity = PlayItem::class,
    parentColumns = arrayOf("name"),
    childColumns = arrayOf("name"),
    onDelete = ForeignKey.CASCADE)]
)
data class PlayInfoItem(
    @PrimaryKey
    val name: String,
    val character: String? = null,
    val currentLearnInfoActSceneIndex: Int? = null,
    val currentLearnInfoLineIndex: Int? = null,
)

@Dao
interface PlayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PlayItem)

    @Delete
    suspend fun delete(item: PlayItem)

    @Query("SELECT * from play WHERE name = :name")
    suspend fun getPlay(name: String): PlayItem?

    @Query("SELECT name from play ORDER BY name ASC")
    fun getAllPlaysName(): Flow<List<String>>
}

@Dao
interface PlayInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PlayInfoItem)

    @Delete
    suspend fun delete(item: PlayInfoItem)

    @Query("SELECT * from play_info WHERE name = :name")
    suspend fun getPlayInfo(name: String): PlayInfoItem?
}

@Database(entities = [PlayItem::class, PlayInfoItem::class], version = 1, exportSchema = false)
abstract class PlayDatabase : RoomDatabase() {
    abstract fun playDao(): PlayDao
    abstract fun playInfoDao(): PlayInfoDao

    companion object {
        @Volatile
        private var Instance: PlayDatabase? = null

        fun getDatabase(context: Context): PlayDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PlayDatabase::class.java, "play_database")
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            GlobalScope.launch {
                                lateinit var content: String
                                try {
                                    content =
                                        context.resources.openRawResource(R.raw.le_jeu_de_l_amour_et_du_hasard)
                                            .bufferedReader()
                                            .use { it.readText() }
                                } catch (ioException: IOException) {
                                    println(ioException)
                                }
                                val play = playParser(content)
                                Instance?.playDao()?.insert(PlayItem(play.name, content))
                            }
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }
    }
}