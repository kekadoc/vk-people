package com.kekadoc.projects.vkpeople

import android.app.Activity
import android.util.Log
import androidx.lifecycle.*
import com.kekadoc.projects.vkpeople.data.VKCurrentUser
import com.kekadoc.projects.vkpeople.data.VKUser
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.coroutines.*

class ViewModelActivity() : ViewModel() {

    companion object {
        private const val TAG: String = "ViewModelActivity-TAG"

        private const val LOADING_USER_TIMEOUT = 60_000L
    }

    private val _currentUser = MutableLiveData<VKCurrentUser?>()
    private val _showingUser = MutableLiveData<VKUser?>()

    val currentUser = _currentUser
    val showingUser = _showingUser

    val vkAuthCallbackHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "vkAuthCallbackHandler: $exception")
        throw exception
    }

    val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            _currentUser.postValue(null)
        }
    }

    val vkAuthCallback = object: VKAuthCallback {
        override fun onLogin(token: VKAccessToken) {
            requestCurrentUser()
        }
        override fun onLoginFailed(errorCode: Int) {
            _currentUser.postValue(null)
            Log.e(TAG, "onLoginFailed: $errorCode")
        }
    }

    fun logIn(activity: Activity) {
        VK.login(activity, arrayListOf(VKScope.WALL, VKScope.PHOTOS))
    }
    fun logOut() {
        VK.logout()
        _currentUser.value = null
    }

    fun requestCurrentUser() {
        viewModelScope.launch(Dispatchers.IO + vkAuthCallbackHandler) {
            _currentUser.postValue(VK.getLastSignedAccount())
        }
    }

    private var activeRequest: Job? = null

    fun requestRandomUser() {
        activeRequest?.cancel()
        activeRequest = viewModelScope.launch(Dispatchers.IO + vkAuthCallbackHandler) {
            val user = withTimeout(LOADING_USER_TIMEOUT) {
                VKUserProvider.requestRandomUser()
            }
            _showingUser.postValue(user)
            activeRequest = null
        }
    }
    fun requestUser(id: Int) {
        activeRequest?.cancel()
        activeRequest = viewModelScope.launch(Dispatchers.IO + vkAuthCallbackHandler) {
            val user = withTimeout(LOADING_USER_TIMEOUT) {
                VKUserProvider.loadUser(id)
            }
            _showingUser.postValue(user)
            activeRequest = null
        }
    }

}