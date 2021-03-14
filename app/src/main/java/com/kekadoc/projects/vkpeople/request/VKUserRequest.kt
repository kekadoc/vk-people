package com.kekadoc.projects.vkpeople.request

import android.util.Log
import com.kekadoc.projects.vkpeople.data.VKUser
import com.kekadoc.projects.vkpeople.getIntOrNull
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiExecutionException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject

class VKUserRequest(private val id: Int) : ApiCommand<VKUser?>() {

    companion object {
        private const val TAG: String = "VKUserRequest-TAG"
        private val arrayParams = arrayOf(
                VKUser.Fields.ID,
                VKUser.Fields.FIRST_NAME,
                VKUser.Fields.LAST_NAME,
                VKUser.Fields.DEACTIVATED,
                VKUser.Fields.IS_CLOSED,
                VKUser.Fields.CAN_ACCESS_CLOSED,
                VKUser.Fields.HAS_PHOTO,

                VKUser.Fields.BDATE,
                VKUser.Fields.COUNTRY,
                VKUser.Fields.CITY,
                VKUser.Fields.RELATION,
                VKUser.Fields.SEX,
                VKUser.Fields.STATUS,
                VKUser.Fields.DOMAIN,

                VKUser.Fields.COUNTERS,
                VKUser.Fields.PERSONAL,
                VKUser.Fields.LAST_SEEN,

                VKUser.Fields.BLACKLISTED,
                VKUser.Fields.BLACKLISTED_BY_ME,
                VKUser.Fields.IS_FAVORITE,
                VKUser.Fields.IS_FRIEND,
                VKUser.Fields.IS_HIDDEN_FROM_FEED,
                VKUser.Fields.ONLINE,
                VKUser.Fields.VERIFIED,
                VKUser.Fields.TRENDING,

                VKUser.Fields.ACTIVITIES,
                VKUser.Fields.INTERESTS,
                VKUser.Fields.MUSIC,
                VKUser.Fields.MOVIES,
                VKUser.Fields.TV,
                VKUser.Fields.BOOKS,
                VKUser.Fields.GAMES,
                VKUser.Fields.QUOTES,
                VKUser.Fields.ABOUT,

                VKUser.Fields.PHOTO_400_ORIG,
                VKUser.Fields.PHOTO_MAX_ORIG,
        )
    }

    override fun onExecute(manager: VKApiManager): VKUser? {
        val baseCall = VKMethodCall.Builder()
                .method(VKRequestApi.Users.GET)
                .args(VKRequestApi.Users.PARAM_USER_IDS, id)
                .args(VKRequestApi.Users.PARAM_FIELDS, arrayParams.joinToString(","))
                .version(manager.config.version)
                .build()
        val gifts = VKMethodCall.Builder()
                .method(VKRequestApi.Gifts.GET)
                .args(VKRequestApi.Gifts.PARAM_USER_ID, id)
                .version(manager.config.version)
                .build()

        var giftsCount: Int? = null
        try {
            giftsCount = manager.execute(gifts, object : VKApiResponseParser<Int?> {
                override fun parse(response: String?): Int? {
                    if (response == null) return null
                    val js = JSONObject(response)
                    val jsr = js.getJSONObject(VKRequestApi.RESPONSE)
                    return jsr.getIntOrNull(VKRequestApi.Gifts.RESULT_COUNT)
                }
            })
        }catch (e: VKApiExecutionException) {}

        val user = manager.execute(baseCall) { response ->
            Log.e(TAG, "parse: $response")
            try {
                val js = JSONObject(response!!)
                val jsr = js.getJSONArray(VKRequestApi.RESPONSE).getJSONObject(0)
                VKUser.parse(jsr)
            } catch (e: Exception) {
                null
            }
        }
        user?.giftsCount = giftsCount
        return user
    }

}