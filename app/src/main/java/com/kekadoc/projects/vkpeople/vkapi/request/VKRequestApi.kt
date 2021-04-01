package com.kekadoc.projects.vkpeople.vkapi.request

object VKRequestApi {

    const val RESPONSE = "response"

    object Users {
        const val GET = "users.get"

        const val PARAM_USER_IDS = "user_ids"
        const val PARAM_FIELDS = "fields"
    }
    object Gifts {
        const val GET = "gifts.get"

        const val PARAM_USER_ID = "user_id"
        const val PARAM_COUNT = "count"

        const val RESULT_COUNT = "count"
        const val RESULT_ITEMS = "items"
    }
    object Database {
        const val GET_CITIES_BY_ID = "database.getCitiesById"

        const val GET_CITIES_BY_ID_PARAM = "city_ids"
        const val GET_CITIES_BY_ID_RESULT_ID = "id"
        const val GET_CITIES_BY_ID_RESULT_TITLE = "title"

        const val GET_COUNTRIES_BY_ID = "database.getCountriesById"

        const val GET_COUNTRIES_BY_ID_PARAM = "country_ids"
        const val GET_COUNTRIES_BY_ID_RESULT_ID = "id"
        const val GET_COUNTRIES_BY_ID_RESULT_TITLE = "title"
    }
}