package com.kekadoc.projects.vkpeople

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kekadoc.projects.vkpeople.database.AppDatabase
import com.kekadoc.projects.vkpeople.database.SavedUser
import com.kekadoc.projects.vkpeople.database.SavedUsersDao
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var userDao: SavedUsersDao
    private lateinit var db: AppDatabase

    private lateinit var users: LiveData<List<SavedUser>>

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        userDao = db.savedUsersDao()
        users = userDao.getAll().apply {
            observeForever {
                printMessage("UsersLiveData", "Changed: $it")
            }
        }
    }
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    suspend fun insertAll() {
        val methodName = "insertAll(users)"
        val ids = intArrayOf(100, 101, 102)
        printMessage(methodName, "Start")
        val users = createUsers(ids)
        printMessage(methodName, "Created users: $users")
        userDao.insertAll(users)
        printMessage(methodName, "Users inserted in BD")

        val exists = userDao.existsAll(ids)

        assert(exists) {
            "Users not exists!"
        }
        userDao.deleteAll(ids)
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun insert() {
        val methodName = "insert(user)"
        val id = 200
        printMessage(methodName, "Start")
        val user = createUser(id)
        printMessage(methodName, "Created user: $user")
        userDao.insert(user)
        printMessage(methodName, "User inserted in BD")

        val exists = userDao.exists(id)

        assert(exists) {
            "User not exists!"
        }
        userDao.delete(id)
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun update() {
        val methodName = "update(user)"
        val id = 300
        printMessage(methodName, "Start")
        val user = createUser(id)
        printMessage(methodName, "Create user: $user")
        userDao.insert(user)
        printMessage(methodName, "User inserted in BD")
        user.data = "ChangedData"
        printMessage(methodName, "Instance User changed: $user")
        userDao.update(user)
        printMessage(methodName, "User in DB update")
        val userFromDp = userDao.get(user.id)
        printMessage(methodName, "Updated user from DP: $userFromDp")
        assert(user == userFromDp) {
            "LocalUser{$user} != UserFromDP{$userFromDp}"
        }
        userDao.delete(id)
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun updateAll() {
        val methodName = "updateAll(users)"
        val ids = intArrayOf(400, 401, 402)
        printMessage(methodName, "Start")
        val users = createUsers(ids)
        printMessage(methodName, "Created users: $users")
        userDao.insertAll(users)
        printMessage(methodName, "Users inserted in BD")
        users[0].data = "ChangeData 0"
        users[1].data = "ChangeData 1"
        users[2].data = "ChangeData 2"
        printMessage(methodName, "Users changed: $users")
        userDao.updateAll(users)
        printMessage(methodName, "Users in DB update")
        val userFromDp = userDao.getAll(ids)
        printMessage(methodName, "Updated users from DP: $userFromDp")
        assert(userFromDp == users.toList()) {
            "LocalUsers{$users} != UserFromDP{$userFromDp}"
        }
        userDao.deleteAll(ids)
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun deleteAll() {
        val methodName = "deleteAll()"
        val ids = intArrayOf(500, 501, 502)
        printMessage(methodName, "Start")
        val users = createUsers(ids)
        printMessage(methodName, "Created users: $users")
        userDao.insertAll(users)
        printMessage(methodName, "Users inserted in BD")
        userDao.deleteAll()
        printMessage(methodName, "All users removed")
        assert(userDao.size() == 0) {
            "DB is not empty"
        }
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun delete() {
        val methodName = "delete(user)"
        val id = 600
        printMessage(methodName, "Start")
        val user = createUser(id)
        printMessage(methodName, "Created user: $user")
        userDao.insert(user)
        assert(userDao.exists(id)) {
            "User not inserted"
        }
        printMessage(methodName, "User inserted in BD")
        userDao.delete(user)
        assert(!userDao.exists(id)) {
            "User not deleted"
        }
        printMessage(methodName, "User deleted")
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun deleteById() {
        val methodName = "delete(id)"
        val id = 700
        printMessage(methodName, "Start")
        val user = createUser(id)
        printMessage(methodName, "Created user: $user")
        userDao.insert(user)
        assert(userDao.exists(id)) {
            "User not inserted"
        }
        printMessage(methodName, "User inserted in BD")
        userDao.delete(id)
        assert(!userDao.exists(id)) {
            "User not deleted"
        }
        printMessage(methodName, "User deleted")
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun get() {
        val methodName = "get(id)"
        val id = 800
        printMessage(methodName, "Start")
        val user = createUser(id)
        printMessage(methodName, "Created user: $user")
        userDao.insert(user)
        printMessage(methodName, "User inserted in BD")
        val userFromDB = userDao.get(id)
        printMessage(methodName, "User from DB: $userFromDB")

        assert(user == userFromDB) {
            "LocalUser{$user} != UserFromDP{$userFromDB}"
        }
        userDao.delete(id)
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun getAllById() {
        val methodName = "getAll(ids)"
        val ids = intArrayOf(900, 901, 902)
        printMessage(methodName, "Start")
        val users = createUsers(ids)
        printMessage(methodName, "Created users: $users")
        userDao.insertAll(users)
        printMessage(methodName, "Users inserted in BD")
        val userFromDB = userDao.getAll(ids)
        printMessage(methodName, "Users from DB: $userFromDB")
        val listUsers = users.toList()
        assert(listUsers == userFromDB) {
            "Init users {$listUsers} != Users from BD {$userFromDB}"
        }
        userDao.deleteAll(ids)
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun getAll() {
        val methodName = "getAll()"
        val ids = intArrayOf(1000, 1001, 1002)
        printMessage(methodName, "Start")
        val users = createUsers(ids)
        printMessage(methodName, "Create users: $users")
        userDao.insertAll(users)
        printMessage(methodName, "Users inserted in BD")
        val userFromDB = userDao.getAll(ids)
        printMessage(methodName, "Users from DB: $userFromDB")
        val listUsers = users.toList()
        assert(listUsers == userFromDB) {
            "Init users [$listUsers] != Users from BD [$userFromDB]"
        }
        userDao.deleteAll(ids)
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun size() {
        val methodName = "size()"
        val ids = intArrayOf(1100, 1101, 1102)
        printMessage(methodName, "Start")
        val users = createUsers(ids)
        printMessage(methodName, "Create users: $users")
        val sizeBefore = userDao.size()
        printMessage(methodName, "Count users in DB before inserting: $sizeBefore")
        userDao.insertAll(users)
        printMessage(methodName, "Users inserted in BD")
        val sizeAfter = userDao.size()
        printMessage(methodName, "Count users in DB after inserting: $sizeAfter")

        assert(sizeBefore + users.size == sizeAfter) {
            "sizeBefore{$sizeBefore} + {${users.size}} != sizeAfter{$sizeAfter}"
        }
        userDao.deleteAll(ids)
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun exists() {
        val methodName = "exists(id)"
        val id = 1200
        printMessage(methodName, "Start")
        val user = createUser(id)
        printMessage(methodName, "Created user: $user")
        userDao.insert(user)
        printMessage(methodName, "User inserted in BD")

        val exist = userDao.exists(id)

        assert(exist) {
            "User not exist"
        }
        userDao.delete(id)
        printMessage(methodName, "Complete")
    }
    @Test
    suspend fun existsAll() {
        val methodName = "existsAll(ids)"
        val ids = intArrayOf(1300, 1301, 1302)
        printMessage(methodName, "Start")
        val users = createUsers(ids)
        printMessage(methodName, "Created users: $users")
        userDao.insertAll(users)
        printMessage(methodName, "Users inserted in BD")

        val exist = userDao.existsAll(ids)

        assert(exist) {
            "Users not exist"
        }
        userDao.deleteAll(ids)
        printMessage(methodName, "Complete")
    }

    private fun createUser(id: Int, data: String = id.toString()) = SavedUser(id, data)
    private fun createUsers(ids: IntArray): List<SavedUser> {
        return Array(ids.size) {
            createUser(ids[it])
        }.toList()
    }

    private fun printMessage(testName: String, msg: String) {
        println("${javaClass.simpleName}.${testName.padEnd(16)}: $msg")
    }

}