package com.kekadoc.projects.vkpeople.vkapi.request

import android.util.Log
import com.kekadoc.projects.vkpeople.vkapi.data.VKUser
import com.kekadoc.projects.vkpeople.vkapi.data.VKUserPreview
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONObject

class VKUsersListRequest(private val ids: List<Int>) : ApiCommand<List<VKUserPreview>>() {

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
                VKUser.Fields.DOMAIN,
                VKUser.Fields.PHOTO_200
        )
    }

    override fun onExecute(manager: VKApiManager): List<VKUserPreview> {
        val baseCall = VKMethodCall.Builder()
                .method(VKRequestApi.Users.GET)
                .args(VKRequestApi.Users.PARAM_USER_IDS, ids.joinToString(","))
                .args(VKRequestApi.Users.PARAM_FIELDS, arrayParams.joinToString(","))
                .version(manager.config.version)
                .build()

        return manager.execute(baseCall) { response ->
            try {
                val js = JSONObject(response!!)
                val jsArr = js.getJSONArray(VKRequestApi.RESPONSE)
                val list = mutableListOf<VKUserPreview>()
                (0 until jsArr.length()).forEach {
                    list.add(VKUserPreview.parse(jsArr.getJSONObject(it)))
                }
                Log.e(TAG, "onExecute: $list")
                return@execute list
            } catch (e: Exception) {
                Log.e(TAG, "onExecute: $e")
                emptyList()
            }
        }
    }

}