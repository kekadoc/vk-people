package com.kekadoc.projects.vkpeople.vkapi.request

import com.kekadoc.projects.vkpeople.vkapi.data.VKRawUser
import com.kekadoc.projects.vkpeople.vkapi.data.VKUser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class VKRawUsersRequest(
    private val uids: IntArray = intArrayOf(),
    private val limit: Int = CHUNK_LIMIT): ApiCommand<List<VKRawUser>>() {

    companion object {
        private const val TAG: String = "VKUsersScoutingCommand-TAG"
        const val CHUNK_LIMIT = 900
    }

    override fun onExecute(manager: VKApiManager): List<VKRawUser> {
        if (uids.isEmpty()) {
            val call = VKMethodCall.Builder()
                .method(VKRequestApi.Users.GET)
                .args(VKRequestApi.Users.PARAM_FIELDS, VKUser.Fields.ID)
                .args(VKRequestApi.Users.PARAM_FIELDS, VKUser.Fields.DEACTIVATED)
                .args(VKRequestApi.Users.PARAM_FIELDS, VKUser.Fields.HAS_PHOTO)
                .version(manager.config.version)
                .build()
            return manager.execute(call, ResponseApiParser())
        } else {
            val result = ArrayList<VKRawUser>()
            val chunks = uids.toList().chunked(limit)
            for (chunk in chunks) {
                val call = VKMethodCall.Builder()
                    .method(VKRequestApi.Users.GET)
                    .args(VKRequestApi.Users.PARAM_USER_IDS, chunk.joinToString(","))
                    .args(VKRequestApi.Users.PARAM_FIELDS, VKUser.Fields.ID)
                    .args(VKRequestApi.Users.PARAM_FIELDS, VKUser.Fields.DEACTIVATED)
                    .args(VKRequestApi.Users.PARAM_FIELDS, VKUser.Fields.HAS_PHOTO)
                    .version(manager.config.version)
                    .build()
                result.addAll(manager.execute(call, ResponseApiParser()))
            }
            return result
        }
    }

    private class ResponseApiParser : VKApiResponseParser<List<VKRawUser>> {
        override fun parse(response: String): List<VKRawUser> {
            try {
                val ja = JSONObject(response).getJSONArray(VKRequestApi.RESPONSE)
                val r = ArrayList<VKRawUser>(ja.length())
                for (i in 0 until ja.length()) {
                    val user = VKRawUser.parse(ja.getJSONObject(i))
                    r.add(user)
                }
                return r
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}