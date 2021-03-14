package com.kekadoc.projects.vkpeople.data

import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class VKCurrentUser(
        /**
         * идентификатор пользователя
         */
        val id: Int,
        /**
         * имя
         */
        val first_name: String,
        /**
         * фамилия
         */
        val last_name: String,
        /**
         * короткий адрес страницы. Возвращается строка, содержащая короткий адрес страницы (например, andrew).
         * Если он не назначен, возвращается "id"+user_id, например, id35828305.
         */
        val domain: String,
        /**
         * url фотографии, имеющей ширину 400 пикселей.
         * Если у пользователя отсутствует фотография такого размера, в ответе вернется https://vk.com/images/camera_400.png.
         */
        val photo: String
) : Parcelable {
    companion object {
        private const val TAG: String = "VKCurrentUser-TAG"

        fun parse(jsonObject: JSONObject): VKCurrentUser {
            Log.e(TAG, "parse: $jsonObject")
            return VKCurrentUser(
                id = jsonObject.optInt("id", 0),
                first_name = jsonObject.optString("first_name", ""),
                last_name = jsonObject.optString("last_name", ""),
                domain = jsonObject.optString("domain", ""),
                    photo = jsonObject.optString("photo_200", ""),
            )
        }
    }

    fun getFullName(): String {
        return "$first_name $last_name"
    }

}