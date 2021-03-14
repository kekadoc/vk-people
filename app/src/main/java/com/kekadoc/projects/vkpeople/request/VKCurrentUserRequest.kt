package com.kekadoc.projects.vkpeople.request

import com.kekadoc.projects.vkpeople.data.VKCurrentUser
import com.kekadoc.projects.vkpeople.data.VKUser
import com.kekadoc.projects.vkpeople.getJSONArrayOrNull
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
        val ja = r.getJSONArrayOrNull(VKRequestApi.RESPONSE)
        if (ja == null || ja.length() == 0) return null
        val jo = ja.getJSONObject(0)
        return VKCurrentUser.parse(jo)
    }

}