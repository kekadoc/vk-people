package com.kekadoc.projects.vkpeople.database

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import com.kekadoc.projects.vkpeople.util.AbstractRepository
import com.kekadoc.projects.vkpeople.util.RequestCallback

@Entity(tableName = "SavedUsers")
data class SavedUser(@PrimaryKey val id: Int, @ColumnInfo(name = "data") var data: String? = null)

@Dao
interface BaseDao<T> {

    fun getAll(): LiveData<List<T>>

    fun getAll(ids: IntArray): List<T>

    suspend fun get(id: Int): T

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<T>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: T)

    @Update
    suspend fun update(data: T)
    @Update
    suspend fun updateAll(data: List<T>)

    suspend fun deleteAll()
    suspend fun deleteAll(ids: IntArray)

    @Delete
    suspend fun delete(data: T)

    suspend fun delete(id: Int)

    suspend fun size(): Int

    suspend fun exists(id: Int): Boolean

    suspend fun existsAll(ids: IntArray): Boolean
}

@Dao
interface SavedUsersDao : BaseDao<SavedUser> {

    @Query("SELECT * FROM SavedUsers")
    override fun getAll(): LiveData<List<SavedUser>>

    @Query("SELECT * FROM SavedUsers WHERE id=:id")
    override suspend fun get(id: Int): SavedUser

    @Query("SELECT * FROM SavedUsers WHERE id IN (:ids)")
    override fun getAll(ids: IntArray): List<SavedUser>


    @Query("DELETE FROM SavedUsers")
    override suspend fun deleteAll()

    @Query("DELETE FROM SavedUsers WHERE id IN (:ids)")
    override suspend fun deleteAll(ids: IntArray)

    @Query("DELETE FROM SavedUsers WHERE id = :id")
    override suspend fun delete(id: Int)

    @Query("SELECT COUNT(*) FROM SavedUsers")
    override suspend fun size(): Int

    @Query("SELECT EXISTS(SELECT * FROM SavedUsers WHERE id = :id)")
    override suspend fun exists(id: Int): Boolean

    @Query("SELECT EXISTS(SELECT * FROM SavedUsers WHERE id IN (:ids))")
    override suspend fun existsAll(ids: IntArray): Boolean

}

@Database(entities = [SavedUser::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savedUsersDao(): SavedUsersDao

    companion object {

        private const val LIKED_USERS_DATABASE_NAME = "liked_users_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        LIKED_USERS_DATABASE_NAME
                ).build()

                INSTANCE = instance
                instance
            }
        }

    }

}

class DatabaseRepository(application: Application) : AbstractRepository() {

    companion object {
        private const val TAG: String = "DatabaseRepository-TAG"
    }

    private val database = AppDatabase.getDatabase(application)
    private val savedUsersDao = database.savedUsersDao()

    fun containUserInSaved(id: Int, callback: RequestCallback<Boolean>? = null) {
        execute(callback) {
            savedUsersDao.exists(id)
        }
        return
    }

    /**
     * All saved users in database
     */
    val savedUsers by lazy { savedUsersDao.getAll() }

    /**
     * Save user with specified id
     * @param id User Id
     * @param callback Callback
     */
    fun saveUser(id: Int, callback: RequestCallback<Unit>? = null) {
        execute(callback) {
            Log.e(TAG, "saveUser: $id")
            savedUsersDao.insert(SavedUser(id)) }
    }
    /**
     * Delete user with specified id
     * @param id User Id
     * @param callback Callback
     */
    fun deleteUser(id: Int, callback: RequestCallback<Unit>? = null) {
        execute(callback) { savedUsersDao.delete(id) }
    }

}