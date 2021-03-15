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

class ActivityViewModel : ViewModel() {

    companion object {
        private const val TAG: String = "ActivityViewModel-TAG"

        private const val LOADING_USER_TIMEOUT = 60_000L
    }

    private val _loadingProcess = MutableLiveData(false)
    val loadingProcess: LiveData<Boolean> = _loadingProcess

    private val _currentUser = MutableLiveData<VKCurrentUser?>()
    private val _showingUser = MutableLiveData<VKUser?>()

    val currentUser: LiveData<VKCurrentUser?> = _currentUser
    val showingUser: LiveData<VKUser?> = _showingUser

    private val vkAuthCallbackHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "vkAuthCallbackHandler: $exception")
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

    private var activeRequestCurrentUser: Job? = null
    private var activeRequest: Job? = null

    fun logIn(activity: Activity) {
        VK.login(activity, VKApi.getVKScopes())
    }
    fun logOut() {
        VK.logout()
        _currentUser.value = null
        _showingUser.value = null
    }

    fun requestCurrentUser() {
        activeRequestCurrentUser = runRequest {
            _currentUser.postValue(VK.getLastSignedAccount())
        }
    }
    fun requestRandomUser() {
        requestAnotherUser { VKUserProvider.requestRandomUser() }
    }
    fun requestUser(id: Int) {
        requestAnotherUser { VKUserProvider.loadUser(id) }
    }

    private fun requestAnotherUser(block: suspend CoroutineScope.() -> VKUser?) {
        _loadingProcess.value = true
        activeRequest?.cancel()
        activeRequest = runRequest {
            val user = withTimeout(LOADING_USER_TIMEOUT, block)
            _showingUser.postValue(user)
            activeRequest = null
            _loadingProcess.postValue(false)
        }
    }

    private fun runRequest(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(Dispatchers.IO + vkAuthCallbackHandler, block = block)
    }

}