package com.kekadoc.projects.vkpeople

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import com.kekadoc.projects.vkpeople.data.VKCurrentUser
import com.kekadoc.projects.vkpeople.data.VKRawUser
import com.kekadoc.projects.vkpeople.data.VKUser
import com.kekadoc.projects.vkpeople.request.VKCurrentUserRequest
import com.kekadoc.projects.vkpeople.request.VKUserRequest
import com.kekadoc.projects.vkpeople.request.VKRawUsersRequest
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.VKApiConfig
import com.vk.api.sdk.VKDefaultValidationHandler
import com.vk.api.sdk.auth.VKScope
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG: String = "VKApi-TAG"

fun JSONObject.getStringOrNull(key: String): String? {
    return if (has(key)) getString(key)
    else null
}
fun JSONObject.getIntOrNull(key: String): Int? {
    return if (has(key)) getInt(key)
    else null
}
fun JSONObject.getLongOrNull(key: String): Long? {
    return if (has(key)) getLong(key)
    else null
}
fun JSONObject.getBooleanOrNull(key: String): Boolean? {
    return if (has(key)) getBoolean(key)
    else null
}
fun JSONObject.getJSONObjectOrNull(key: String): JSONObject? {
    return if (has(key)) getJSONObject(key)
    else null
}
fun JSONObject.getJSONArrayOrNull(key: String): JSONArray? {
    return if (has(key)) getJSONArray(key)
    else null
}
fun JSONObject.getOrNull(key: String): Any? {
    return if (has(key)) get(key)
    else null
}

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

private const val VK_BASE_URL = "https://vk.com"
private const val VK_APP_PACKAGE_ID = "com.vkontakte.android"


internal object VKApi {
    fun createVKConfig(context: Context): VKApiConfig {
        return VKApiConfig(
            context = context,
            appId = context.resources.getIdentifier("com_vk_sdk_AppId", "integer", context.packageName),
            validationHandler = VKDefaultValidationHandler(context),
            lang = "ru")
    }
    fun getVKScopes() = arrayListOf(VKScope.WALL, VKScope.PHOTOS)
}

val VK.BASE_URL: String
    get() = VK_BASE_URL
val VK.APP_PACKAGE_ID: String
    get() = VK_APP_PACKAGE_ID

fun Activity.startVKUserProfile(id: Int) {
    startVKActivity("${VK.BASE_URL}/id${id}")
}

fun Activity.startVKActivity(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

    val resInfo = packageManager.queryIntentActivities(intent, 0);
    if (resInfo.isEmpty()) return

    resInfo.forEach {
        if (it.activityInfo != null) {
            if (VK.APP_PACKAGE_ID == it.activityInfo.packageName) {
                intent.setPackage(it.activityInfo.packageName)
                return@forEach
            }
        }
    }

    startActivity(intent)
}

suspend fun VK.getLastSignedAccount(): VKCurrentUser? {
    return suspendCoroutine<VKCurrentUser?> { continuation ->
        VK.execute(VKCurrentUserRequest(), vkApiCallback(
            onSuccess = {
                continuation.resume(it)
            },
            onFail = {
                Log.e(TAG, "getLastSignedAccount: $it")
                continuation.resume(null)
            }
        ))
    }
}

suspend fun VK.loadUser(id: Int) = VKUserProvider.loadUser(id)
suspend fun VK.loadRandomUser() = VKUserProvider.requestRandomUser()

/**
 * For create VKUser
 */
internal object VKUserProvider {

    private const val TAG: String = "VKUserProvider-TAG"
    private const val REQUEST_USERS_POOL_SIZE = 900

    private val loadedUsers: MutableSet<VKRawUser> = hashSetOf()

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
                    Log.e(TAG, "success: $result")
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
        val lastId = VKUsersParser.parseLastId()
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
                    Log.e(TAG, "success: ${result.size}")
                    it.resume(result)
                }
            })
        }
    }

}

/**
 * For parsing last VK user ID
 */
internal object VKUsersParser {

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

    private const val catalogVKUrl = "${VK_BASE_URL}/catalog.php"

    private const val divCatalogWrap = "div.catalog_wrap"
    private const val hrefKey = "href"
    private const val idKey = "id"
    private const val defId = 1

    @WorkerThread
    fun parseLastId(): Int {
        try {
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
                Log.e(TAG, "parseLastId: $url")
            } while (href.substring(0, 2) != idKey)
            return href.removePrefix("id").toInt()
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "parseLastId: $e")
            e.printStackTrace()
            return defId
        }
    }

}