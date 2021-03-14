package com.kekadoc.projects.vkpeople.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Класс для предварительной загрузки пользователей
 */
@Parcelize
data class VKRawUser(
        /**
         * идентификатор пользователя
         */
        val id: Int,
        /**
         * поле возвращается, если страница пользователя удалена или заблокирована, содержит
         * значение deleted или banned. В этом случае опциональные поля не возвращаются
         */
        val deactivated: String,
        /**
         * 1, если пользователь установил фотографию для профиля.
         */
        val has_photo: Boolean) : Parcelable {

        companion object {
                fun parse(jsonObject: JSONObject): VKRawUser {
                        return VKRawUser(
                                id = jsonObject.optInt("id", 0),
                                deactivated = jsonObject.optString("deactivated", ""),
                                has_photo = jsonObject.optInt("has_photo", 0) == 1
                        )
                }
        }

        fun isDeactivated() = deactivated == ""

}