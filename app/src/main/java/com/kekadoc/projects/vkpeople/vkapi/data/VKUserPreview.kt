package com.kekadoc.projects.vkpeople.vkapi.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class VKUserPreview(
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
         * поле возвращается, если страница пользователя удалена или заблокирована, содержит
         * значение deleted или banned. В этом случае опциональные поля не возвращаются
         */
        val deactivated: Boolean,
        /**
         * скрыт ли профиль пользователя настройками приватности
         */
        val is_closed: Boolean,
        /**
         * может ли текущий пользователь видеть профиль при is_closed = 1 (например, он есть в друзьях)
         */
        val can_access_closed: Boolean,
        /**
         * 1, если пользователь установил фотографию для профиля.
         */
        val has_photo: Boolean,

        /**
         * url фотографии, имеющей ширину 200 пикселей.
         * Если у пользователя отсутствует фотография такого размера, в ответе вернется https://vk.com/images/camera_200.png.
         */
        val photo_200: String

) : Parcelable {

    companion object {

        fun parse(json: JSONObject): VKUserPreview {
            return VKUserPreview(
                    id = json.optInt(VKUser.Fields.ID),
                    first_name = json.optString(VKUser.Fields.FIRST_NAME),
                    last_name = json.optString(VKUser.Fields.LAST_NAME),
                    domain = json.optString(VKUser.Fields.DOMAIN),
                    deactivated = json.optString(VKUser.Fields.DEACTIVATED) != "",
                    is_closed = json.optInt(VKUser.Fields.IS_CLOSED) == 1,
                    can_access_closed = json.optInt(VKUser.Fields.CAN_ACCESS_CLOSED) == 1,
                    has_photo = json.optInt(VKUser.Fields.HAS_PHOTO) == 1,
                    photo_200 = json.optString(VKUser.Fields.PHOTO_200)
            )
        }

    }

    fun getFullName(): String {
        return "$first_name $last_name"
    }
    fun getScreenName(): String {
        return "@$domain"
    }

}