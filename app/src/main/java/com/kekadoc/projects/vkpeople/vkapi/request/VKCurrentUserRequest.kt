package com.kekadoc.projects.vkpeople.vkapi.request

import com.kekadoc.projects.vkpeople.vkapi.data.VKCurrentUser
import com.kekadoc.projects.vkpeople.vkapi.data.VKUser
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKCurrentUserRequest : VKRequest<VKCurrentUser?>("users.get") {

    companion object {
        private const val TAG: String = "VKReqCurUser-TAG"
    }

    init {
        addParam(VKRequestApi.Users.PARAM_FIELDS, arrayOf(VKUser.Fields.PHOTO_200, VKUser.Fields.DOMAIN))
    }

    override fun parse(r: JSONObject): VKCurrentUser? {
        if (!r.has(VKRequestApi.RESPONSE)) return null
        val ja = r.getJSONArray(VKRequestApi.RESPONSE)
        if (ja.length() == 0) return null
        val jo = ja.getJSONObject(0)
        return VKCurrentUser.parse(jo)
    }

}