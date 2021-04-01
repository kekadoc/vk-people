package com.kekadoc.projects.vkpeople

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.kekadoc.projects.vkpeople.vkapi.data.VKUserPreview
import com.kekadoc.projects.vkpeople.database.DatabaseRepository
import com.kekadoc.projects.vkpeople.database.SavedUser
import com.kekadoc.projects.vkpeople.util.RequestCallback
import com.kekadoc.projects.vkpeople.vkapi.VKApi
import com.kekadoc.projects.vkpeople.vkapi.data.VKCurrentUser
import com.kekadoc.projects.vkpeople.vkapi.data.VKUser
import com.kekadoc.tools.exeption.Wtf
import kotlinx.coroutines.cancel

class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "ActivityViewModel-TAG"
    }

    private val databaseRepository = DatabaseRepository(application)
    private val vkRepository = VKApi.Repository(application)

    val vkAuthCallback = vkRepository

    private val savedUsersObserver: Observer<List<SavedUser>> = Observer<List<SavedUser>> { users ->
        vkRepository.requestSavedUsers(users.map { it.id })
    }

    val currentUser: LiveData<VKCurrentUser?> by lazy { vkRepository.currentUser }
    val loadedUser: LiveData<VKUser?> by lazy { vkRepository.showingUser }
    val savedUsers: LiveData<List<VKUserPreview>> = MutableLiveData<List<VKUserPreview>>(emptyList()).apply {
        val observer: Observer<List<VKUserPreview>> = Observer<List<VKUserPreview>> { users ->
            value = users
        }
        currentUser.observeForever {
            if (it == null) {
                postValue(emptyList())
                vkRepository.savedUsers.removeObserver(observer)
            } else {
                vkRepository.savedUsers.observeForever(observer)
            }
        }
    }

    init {
        databaseRepository.savedUsers.observeForever(savedUsersObserver)
    }

    fun containUserInSaved(id: Int, callback: RequestCallback<Boolean>? = null) = databaseRepository.containUserInSaved(id, callback)

    fun saveLoadedUser(callback: RequestCallback<Unit>? = null) {
        if (loadedUser.value == null) {
            callback!!.onFail(Wtf("Not found user!"))
            return
        }
        loadedUser.value?.let {
            databaseRepository.saveUser(it.id, callback)
        }
    }
    fun saveUser(id: Int, callback: RequestCallback<Unit>? = null) {
        databaseRepository.saveUser(id, callback)
    }
    fun deleteSavedUser(id: Int, callback: RequestCallback<Unit>? = null) {
        databaseRepository.deleteUser(id, callback)
    }

    fun logIn(activity: Activity) {
        vkRepository.logIn(activity)
    }
    fun logOut() {
        vkRepository.logOut()
    }

    fun requestRandomUser(callback: RequestCallback<Unit>? = null) {
        vkRepository.requestRandomUser(callback)
    }
    fun requestUser(id: Int, callback: RequestCallback<Unit>? = null) {
        vkRepository.requestUser(id, callback)
    }

    override fun onCleared() {
        super.onCleared()
        databaseRepository.savedUsers.removeObserver(savedUsersObserver)
        databaseRepository.cancel()
        vkRepository.cancel()
    }

}