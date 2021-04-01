package com.kekadoc.projects.vkpeople.vkapi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kekadoc.projects.vkpeople.util.AbstractRepository
import com.kekadoc.projects.vkpeople.vkapi.data.VKCurrentUser
import com.kekadoc.projects.vkpeople.vkapi.data.VKRawUser
import com.kekadoc.projects.vkpeople.vkapi.data.VKUser
import com.kekadoc.projects.vkpeople.vkapi.data.VKUserPreview
import com.kekadoc.projects.vkpeople.util.RequestCallback
import com.kekadoc.projects.vkpeople.vkapi.request.VKCurrentUserRequest
import com.kekadoc.projects.vkpeople.vkapi.request.VKUserRequest
import com.kekadoc.projects.vkpeople.vkapi.request.VKRawUsersRequest
import com.kekadoc.projects.vkpeople.vkapi.request.VKUsersListRequest
import com.vk.api.sdk.*
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun <T> vkApiCallback(onSuccess: ((result: T) -> Unit)? = null,
                      onFail: ((error: Exception) -> Unit)? = null): VKApiCallback<T> {
    return object : VKApiCallback<T> {
        override fun fail(error: Exception) {
            onFail?.invoke(error)
        }
        override fun success(result: T) {
            onSuccess?.invoke(result)
        }
    }
}


internal fun Activity.startVKUserProfile(id: Int) {
    startVKActivity("${VKApi.VK_BASE_URL}/id${id}")
}

internal fun Activity.startVKActivity(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

    val resInfo = packageManager.queryIntentActivities(intent, 0);
    if (resInfo.isEmpty()) return

    resInfo.forEach {
        if (it.activityInfo != null) {
            if (VKApi.VK_APP_PACKAGE_ID == it.activityInfo.packageName) {
                intent.setPackage(it.activityInfo.packageName)
                return@forEach
            }
        }
    }

    startActivity(intent)
}

object VKApi {
    private const val TAG: String = "VKApi-TAG"

    const val VK_BASE_URL = "https://vk.com"
    const val VK_APP_PACKAGE_ID = "com.vkontakte.android"

    class Repository(context: Context) : AbstractRepository(), VKAuthCallback, VKTokenExpiredHandler {

        companion object {
            private const val LOADING_USER_TIMEOUT = 60_000L
        }

        init {
            VK.setConfig(Config.createVKConfig(context))
            VK.addTokenExpiredHandler(object: VKTokenExpiredHandler {
                override fun onTokenExpired() {
                    (currentUser as MutableLiveData).postValue(null)
                }
            })
        }

        val savedUsers: LiveData<List<VKUserPreview>> = MutableLiveData(emptyList())
        val currentUser: LiveData<VKCurrentUser?> = object : MutableLiveData<VKCurrentUser?>() {
            override fun onActive() {
                super.onActive()
                requestCurrentUser()
            }
        }
        val showingUser: LiveData<VKUser?> =  MutableLiveData<VKUser?>(null)

        private var activeRequestCurrentUser: Job? = null
        private var activeRequest: Job? = null
        private var activeRequestSavedUsers: Job? = null

        fun logIn(activity: Activity) {
            VK.login(activity, Config.getVKScopes())
        }
        fun logOut() {
            VK.logout()
            (currentUser as MutableLiveData).value = null
            (showingUser as MutableLiveData).value = null
        }

        fun requestSavedUsers(ids: List<Int>, callback: RequestCallback<Unit>? = null) {
            activeRequestSavedUsers?.cancel()
            activeRequestSavedUsers = execute(callback) {
                val users = suspendCoroutine<List<VKUserPreview>> { continuation ->
                    Thread(Runnable {
                        try {
                            continuation.resume(VK.executeSync(VKUsersListRequest(ids)))
                        } catch (ex: Exception) {
                            continuation.resumeWithException(ex)
                        }
                    }).start()
                }
                (savedUsers as MutableLiveData).postValue(users)
                activeRequestSavedUsers = null
            }
        }
        fun requestCurrentUser(callback: RequestCallback<Unit>? = null) {
            activeRequestCurrentUser?.cancel()
            activeRequestCurrentUser = execute(callback) {
                (currentUser as MutableLiveData).postValue(Provider.getLastSignedAccount())
            }
        }
        fun requestRandomUser(callback: RequestCallback<Unit>? = null) {
            requestAnotherUser(callback) { Provider.requestRandomUser() }
        }
        fun requestUser(id: Int, callback: RequestCallback<Unit>? = null) {
            requestAnotherUser(callback) { Provider.loadUser(id) }
        }

        private fun requestAnotherUser(callback: RequestCallback<Unit>? = null, block: suspend CoroutineScope.() -> VKUser?) {
            activeRequest?.cancel()
            activeRequest = execute(callback) {
                val user = withTimeout(LOADING_USER_TIMEOUT, block)
                (showingUser as MutableLiveData).postValue(user)
                activeRequest = null
            }
        }

        override fun onLogin(token: VKAccessToken) {
            requestCurrentUser()
        }
        override fun onLoginFailed(errorCode: Int) {
            Log.e(TAG, "onLoginFailed: $errorCode")
            (currentUser as MutableLiveData).postValue(null)
        }
        override fun onTokenExpired() {
            Log.e(TAG, "onTokenExpired: ")
            (currentUser as MutableLiveData).postValue(null)
        }

    }

    internal object Config {
        fun createVKConfig(context: Context): VKApiConfig {
            return VKApiConfig(
                context = context,
                appId = context.resources.getIdentifier("com_vk_sdk_AppId", "integer", context.packageName),
                validationHandler = VKDefaultValidationHandler(context),
                lang = "ru")
        }
        fun getVKScopes() = arrayListOf(VKScope.WALL, VKScope.PHOTOS)
    }

    /**
     * For create VKUser
     */
    internal object Provider {

        private const val TAG: String = "VKUserProvider-TAG"
        private const val REQUEST_USERS_POOL_SIZE = 900

        private val loadedUsers: MutableSet<VKRawUser> = hashSetOf()

        suspend fun getLastSignedAccount(): VKCurrentUser? {
            return suspendCoroutine { continuation ->
                VK.execute(VKCurrentUserRequest(), vkApiCallback(
                    onSuccess = {
                        continuation.resume(it)
                    },
                    onFail = {
                        Log.e(TAG, "getLastSignedAccount Fail: $it")
                        continuation.resume(null)
                    }
                ))
            }
        }

        /**
         * Load full user with this Id
         *
         * @see VKUserRequest
         *
         * @param id User Id
         * @return User
         */
        suspend fun loadUser(id: Int): VKUser? {
            return suspendCoroutine {
                VK.execute(VKUserRequest(id), object : VKApiCallback<VKUser?> {
                    override fun fail(error: Exception) {
                        Log.e(TAG, "fail: $error")
                        it.resume(null)
                    }
                    override fun success(result: VKUser?) {
                        it.resume(result)
                    }
                })
            }
        }

        /**
         * Get random User
         *
         * @return Random User from VK
         *
         */
        suspend fun requestRandomUser(): VKUser {
            var user: VKUser? = null
            while (user == null) {
                if (loadedUsers.isEmpty()) loadedUsers.addAll(requestRawUsers(REQUEST_USERS_POOL_SIZE))
                val rawUser = loadedUsers.last()
                loadedUsers.remove(rawUser)
                user = loadUser(rawUser.id)
            }
            return user
        }

        /**
         * Request for getting list of raw users
         *
         * @see VKRawUsersRequest
         *
         * @param count Count users
         *
         * @return List of raw users
         *
         */
        private suspend fun requestRawUsers(count: Int): List<VKRawUser> {
            val users = mutableListOf<VKRawUser>()
            val lastId = Parser.parseLastId()
            while (users.isEmpty()) {
                users.addAll(filterUsers(loadRawUsers(getRandomUserIds(1, lastId, count))))
            }
            return users
        }

        /**
         * Filter not valid users
         */
        private fun filterUsers(users: List<VKRawUser>): List<VKRawUser> {
            return users.filter {
                //val isClosed = it.is_closed ?: true
                val deactivated = it.isDeactivated()
                val hasPhoto = it.has_photo

                return@filter deactivated && hasPhoto
            }
        }

        /**
         * Get array of user Ids
         *
         * @param fromInclude start Id
         * @param toInclude endId
         * @param count Count
         *
         * @return Array of Ids
         */
        private fun getRandomUserIds(fromInclude: Int, toInclude: Int, count: Int): IntArray {
            val ids = IntArray(count)
            (0 until count).forEach {
                ids[it] = (fromInclude..toInclude).random()
            }
            return ids
        }

        /**
         * Load users
         *
         * @see VKRawUsersRequest
         *
         * @param ids user Id
         * @return List of Users
         */
        private suspend fun loadRawUsers(ids: IntArray): List<VKRawUser> {
            return suspendCoroutine {
                VK.execute(VKRawUsersRequest(ids), object : VKApiCallback<List<VKRawUser>> {
                    override fun fail(error: Exception) {
                        Log.e(TAG, "fail: $error")
                        it.resumeWithException(error)
                    }

                    override fun success(result: List<VKRawUser>) {
                        it.resume(result)
                    }
                })
            }
        }

    }

    /**
     * For parsing last VK user ID
     */
    internal object Parser {

        fun parseLastSeen(user: VKUser): String? {
            try {
                val url = "$VK_BASE_URL/${user.domain}"
                val doc = Jsoup.connect(url).get()
                val e = doc.select("div.profile_online_lv")
                return e.text()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun <T> List<T>.getFromEnd(index: Int): T {
            return get((size - 1) - index)
        }

        private const val TAG: String = "VKUsersParser-TAG"

        private const val catalogVKUrl = "$VK_BASE_URL/catalog.php"

        private const val divCatalogWrap = "div.catalog_wrap"
        private const val hrefKey = "href"
        private const val idKey = "id"
        private const val defId = 1

        @WorkerThread
        @Throws(Exception::class)
        fun parseLastId(): Int {
            var url = catalogVKUrl
            var href: String
            do {
                href = Jsoup.connect(url).get()
                    .select(divCatalogWrap)
                    .last()
                    .children().getFromEnd(1)
                    .children().getFromEnd(1)
                    .attr(hrefKey)

                url = "$VK_BASE_URL/$href"
            } while (href.substring(0, 2) != idKey)
            return href.removePrefix("id").toInt()
        }

        @WorkerThread
        fun tryParseLastId(optId: Int = defId): Int {
            return try {
                parseLastId()
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "tryParseLastId Fail: $e")
                e.printStackTrace()
                optId
            }
        }

    }

}