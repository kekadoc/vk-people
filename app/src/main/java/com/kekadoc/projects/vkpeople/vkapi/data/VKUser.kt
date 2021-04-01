package com.kekadoc.projects.vkpeople.vkapi.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class VKUser(
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
         * дата рождения. Возвращается в формате D.M.YYYY или D.M (если год рождения скрыт).
         * Если дата рождения скрыта целиком, поле отсутствует в ответе.
         */
        val bdate: String?,
        /**
         * информация о стране, указанной на странице пользователя в разделе «Контакты». Возвращаются следующие поля
         */
        val country: Country? = null,
        /**
         * информация о городе, указанном на странице пользователя в разделе «Контакты»
         */
        val city: City? = null,
        /**
         * семейное положение. Возможные значения:
        1 — не женат/не замужем;
        2 — есть друг/есть подруга;
        3 — помолвлен/помолвлена;
        4 — женат/замужем;
        5 — всё сложно;
        6 — в активном поиске;
        7 — влюблён/влюблена;
        8 — в гражданском браке;
        0 — не указано.

        Если в семейном положении указан другой пользователь, дополнительно возвращается объект relation_partner, содержащий id и имя этого человека.
         */
        val relation: Int,
        /**
         * пол. Возможные значения:
        1 — женский;
        2 — мужской;
        0 — пол не указан.
         */
        val sex: Int,
        /**
         * статус пользователя. Возвращается строка, содержащая текст статуса, расположенного в профиле под именем.
         * Если включена опция «Транслировать в статус играющую музыку», возвращается дополнительное поле status_audio,
         * содержащее информацию о композиции.
         */
        val status: String,
        /**
         * короткий адрес страницы. Возвращается строка, содержащая короткий адрес страницы (например, andrew).
         * Если он не назначен, возвращается "id"+user_id, например, id35828305.
         */
        val domain: String,

        /**
         * количество различных объектов у пользователя
         */
        val counters: Counters,
        /**
         * информация о полях из раздела «Жизненная позиция»
         */
        val personal: Personal? = null,
        /**
         *время последнего посещения. Объект, содержащий следующие поля:
        time (integer) — время последнего посещения в формате Unixtime.
        platform (integer) — тип платформы. Возможные значения:
        1 — мобильная версия;
        2 — приложение для iPhone;
        3 — приложение для iPad;
        4 — приложение для Android;
        5 — приложение для Windows Phone;
        6 — приложение для Windows 10;
        7 — полная версия сайта.
         */
        val last_seen: LastSeen,

        /**
         * информация о том, находится ли текущий пользователь в черном списке. Возможные значения:
         *    1 — находится;
         *    0 — не находится.
         */
        val blacklisted: Boolean,
        /**
         * информация о том, находится ли пользователь в черном списке у текущего пользователя. Возможные значения:
         *   1 — находится;
         *   0 — не находится.
         */
        val blacklisted_by_me: Boolean,
        /**
         * информация о том, есть ли пользователь в закладках у текущего пользователя. Возможные значения:
        1 — есть;
        0 — нет.
         */
        val is_favorite: Boolean,
        /**
         * информация о том, является ли пользователь другом текущего пользователя. Возможные значения:
        1 — да;
        0 — нет.
         */
        val is_friend: Boolean,
        /**
         * информация о том, скрыт ли пользователь из ленты новостей текущего пользователя. Возможные значения:
        1 — да;
        0 — нет.
         */
        val is_hidden_from_feed: Boolean,
        /**
         * информация о том, находится ли пользователь сейчас на сайте.
         * Если пользователь использует мобильное приложение либо мобильную версию,
         * возвращается дополнительное поле online_mobile, содержащее 1.
         * При этом, если используется именно приложение, дополнительно возвращается поле online_app,
         * содержащее его идентификатор.
         */
        val online: Boolean,
        /**
         * online_mobile
         */
        val online_mobile: Boolean,
        /**
         * возвращается 1, если страница пользователя верифицирована, 0 — если нет
         */
        val verified: Boolean,
        /**
         * информация о том, есть ли на странице пользователя «огонёк».
         */
        val trending: Boolean,

        /**
         * содержимое поля «Деятельность» из профиля.
         */
        val activities: String,
        /**
         * содержимое поля «Интересы» из профиля
         */
        val interests: String,
        /**
         * содержимое поля «Любимая музыка» из профиля пользователя
         */
        val music: String,
        /**
         * содержимое поля «Любимые фильмы» из профиля пользователя.
         */
        val movies: String,
        /**
         * любимые телешоу.
         */
        val tv: String,
        /**
         * содержимое поля «Любимые книги» из профиля пользователя.
         */
        val books: String,
        /**
         * содержимое поля «Любимые игры» из профиля.
         */
        val games: String,
        /**
         * любимые цитаты
         */
        val quotes: String,
        /**
         * содержимое поля «О себе» из профиля.
         */
        val about: String,
        /**
         * url фотографии, имеющей ширину 400 пикселей.
         * Если у пользователя отсутствует фотография такого размера, в ответе вернется https://vk.com/images/camera_400.png.
         */
        val photo_200: String,
        /**
         * url фотографии, имеющей ширину 400 пикселей.
         * Если у пользователя отсутствует фотография такого размера, в ответе вернется https://vk.com/images/camera_400.png.
         */
        val photo_400_orig: String,
        /**
         * url фотографии, имеющей ширину 400 пикселей.
         * Если у пользователя отсутствует фотография такого размера, в ответе вернется https://vk.com/images/camera_400.png.
         */
        val photo_max_orig: String,
        /**
         * Gifts
         */
        var giftsCount: Int? = null

) : Parcelable {

    companion object {

        fun getRelation(user: VKUser): String? {
            return when(user.relation) {
                1 -> {
                    when (user.sex) {
                        1 -> "не замужем"
                        2 -> "не женат"
                        else -> null
                    }
                }
                2 -> {
                    when (user.sex) {
                        1 -> "есть друг"
                        2 -> "есть подруга"
                        else -> null
                    }
                }
                3 -> {
                    when (user.sex) {
                        1 -> "помолвлена"
                        2 -> "помолвлен"
                        else -> null
                    }
                }
                4-> {
                    when (user.sex) {
                        1 -> "замужем"
                        2 -> "женат"
                        else -> null
                    }
                }
                5 -> "всё сложно"
                6 -> "в активном поиске"
                7 -> {
                    when (user.sex) {
                        1 -> "влюблена"
                        2 -> "влюблён"
                        else -> null
                    }
                }
                8 -> "в гражданском браке"
                else -> null
            }
        }

        fun parse(json: JSONObject): VKUser {
            return VKUser(
                    id = json.optInt(Fields.ID),
                    first_name = json.optString(Fields.FIRST_NAME),
                    last_name = json.optString(Fields.LAST_NAME),
                    deactivated = json.optString(Fields.DEACTIVATED) != "",
                    is_closed = json.optInt(Fields.IS_CLOSED) == 1,
                    can_access_closed = json.optInt(Fields.CAN_ACCESS_CLOSED) == 1,
                    has_photo = json.optInt(Fields.HAS_PHOTO) == 1,

                    bdate = json.optString(Fields.BDATE),
                    country = Country.parse(json.optJSONObject(Fields.COUNTRY)),
                    city = City.parse(json.optJSONObject(Fields.CITY)),
                    relation = json.optInt(Fields.RELATION),
                    sex = json.optInt(Fields.SEX),
                    status = json.optString(Fields.STATUS),
                    domain = json.optString(Fields.DOMAIN),

                    personal = Personal.parse(json.optJSONObject(Fields.COUNTERS)),
                    counters = Counters.parse(json.optJSONObject(Fields.COUNTERS) ?: JSONObject()),
                    last_seen = LastSeen.parse(json.optJSONObject(Fields.LAST_SEEN) ?: JSONObject()),

                    blacklisted = json.optInt(Fields.BLACKLISTED) == 1,
                    blacklisted_by_me = json.optInt(Fields.BLACKLISTED_BY_ME) == 1,
                    is_favorite = json.optInt(Fields.IS_FAVORITE) == 1,
                    is_friend = json.optInt(Fields.IS_FRIEND) == 1,
                    is_hidden_from_feed = json.optInt(Fields.IS_HIDDEN_FROM_FEED) == 1,
                    online = json.optInt(Fields.ONLINE) == 1,
                    online_mobile = json.optInt(Fields.ONLINE_MOBILE) == 1,
                    verified = json.optInt(Fields.VERIFIED) == 1,
                    trending = json.optInt(Fields.TRENDING) == 1,

                    activities = json.optString(Fields.ACTIVITIES),
                    interests = json.optString(Fields.INTERESTS),
                    music = json.optString(Fields.MUSIC),
                    movies = json.optString(Fields.MOVIES),
                    tv = json.optString(Fields.TV),
                    books = json.optString(Fields.BOOKS),
                    games = json.optString(Fields.GAMES),
                    quotes = json.optString(Fields.QUOTES),
                    about = json.optString(Fields.ABOUT),
                    photo_200 = json.optString(Fields.PHOTO_200),
                    photo_400_orig = json.optString(Fields.PHOTO_400_ORIG),
                    photo_max_orig = json.optString(Fields.PHOTO_MAX_ORIG)
            )
        }

    }

    object Fields {
        const val ID = "id"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val DEACTIVATED = "deactivated"
        const val IS_CLOSED = "is_closed"
        const val CAN_ACCESS_CLOSED = "can_access_closed"
        const val ABOUT = "about"
        const val ACTIVITIES = "activities"
        const val BDATE = "bdate"
        const val BLACKLISTED = "blacklisted"
        const val BLACKLISTED_BY_ME = "blacklisted_by_me"
        const val COUNTRY = "country"
        const val CITY = "city"
        const val DOMAIN = "domain"
        const val HAS_PHOTO = "has_photo"
        const val INTERESTS = "interests"
        const val IS_FAVORITE = "is_favorite"
        const val IS_FRIEND = "is_friend"
        const val IS_HIDDEN_FROM_FEED = "is_hidden_from_feed"
        const val LAST_SEEN = "last_seen"
        const val MOVIES = "movies"
        const val MUSIC = "music"
        const val ONLINE = "online"
        const val ONLINE_MOBILE = "online_mobile"
        const val PERSONAL = "personal"
        const val PHOTO_50 = "photo_50"
        const val PHOTO_100 = "photo_100"
        const val PHOTO_200_ORIG = "photo_200_orig"
        const val PHOTO_200 = "photo_200"
        const val PHOTO_400_ORIG = "photo_400_orig"
        const val PHOTO_ID = "photo_id"
        const val PHOTO_MAX = "photo_max"
        const val PHOTO_MAX_ORIG = "photo_max_orig"
        const val QUOTES = "quotes"
        const val RELATION = "relation"
        const val SEX = "sex"
        const val STATUS = "status"
        const val TV = "tv"
        const val VERIFIED = "verified"
        const val COUNTERS = "counters"
        const val BOOKS = "books"
        const val GAMES = "games"
        const val TRENDING = "trending"
    }

    @Parcelize
    data class Counters (
            /**
             * количество видеозаписей
             */
            val videos: Int,
            /**
             * количество аудиозаписей
             */
            val audios: Int,
            /**
             *  количество фотографий
             */
            val photos: Int,
            /**
             * количество сообществ
             */
            val groups: Int,
            /**
             * количество друзей
             */
            val friends: Int,
            /**
             * количество подписчиков
             */
            val followers: Int,

            /**
             * количество заметок
             */
            val notes: Int,
            /**
             * количество друзей онлайн
             */
            val online_friends: Int,
            /**
             *  количество общих друзей
             */
            val mutual_friends: Int,
            /**
             * количество видеозаписей с пользователем
             */
            val user_videos: Int,
            /**
             * количество объектов в блоке «Интересные страницы»
             */
            val pages: Int,
            /**
             * количество фотоальбомов
             */
            val albums: Int,
    ) : Parcelable {
        companion object {
            fun parse(json: JSONObject): Counters {
                return Counters(
                        albums = json.optInt("albums"),
                        videos = json.optInt("videos"),
                        audios = json.optInt("audios"),
                        photos = json.optInt("photos"),
                        notes = json.optInt("notes"),
                        friends = json.optInt("friends"),
                        groups = json.optInt("groups"),
                        online_friends = json.optInt("online_friends"),
                        mutual_friends = json.optInt("mutual_friends"),
                        user_videos = json.optInt("user_videos"),
                        followers = json.optInt("followers"),
                        pages = json.optInt("pages")
                )
            }
            fun toJson(counters: Counters?): JSONObject? {
                if (counters == null) return null
                return JSONObject().apply {
                    put("albums", counters.albums)
                    put("videos", counters.videos)
                    put("audios", counters.audios)
                    put("photos", counters.photos)
                    put("notes", counters.notes)
                    put("friends", counters.friends)
                    put("groups", counters.groups)
                    put("online_friends", counters.online_friends)
                    put("mutual_friends", counters.mutual_friends)
                    put("user_videos", counters.user_videos)
                    put("followers", counters.followers)
                    put("pages", counters.pages)
                }
            }
        }
    }
    @Parcelize
    data class Personal(
        /**
         *  политические предпочтения. Возможные значения:
        1 — коммунистические;
        2 — социалистические;
        3 — умеренные;
        4 — либеральные;
        5 — консервативные;
        6 — монархические;
        7 — ультраконсервативные;
        8 — индифферентные;
        9 — либертарианские.
         */
        val political: Int,
        /**
         * языки
         */
        val langs: List<String>,
        /**
         * мировоззрение
         */
        val religion: String,
        /**
         * источники вдохновения
         */
        val inspired_by: String,
        /**
         * главное в людях. Возможные значения:
        1 — ум и креативность;
        2 — доброта и честность;
        3 — красота и здоровье;
        4 — власть и богатство;
        5 — смелость и упорство;
        6 — юмор и жизнелюбие.
         */
        val people_main: Int,
        /**
         * главное в жизни. Возможные значения:
        1 — семья и дети;
        2 — карьера и деньги;
        3 — развлечения и отдых;
        4 — наука и исследования;
        5 — совершенствование мира;
        6 — саморазвитие;
        7 — красота и искусство;
        8 — слава и влияние;
         */
        val life_main: Int,
        /**
         * отношение к курению. Возможные значения:
        1 — резко негативное;
        2 — негативное;
        3 — компромиссное;
        4 — нейтральное;
        5 — положительное.
         */
        val smoking: Int,
        /**
         * отношение к алкоголю. Возможные значения:
        1 — резко негативное;
        2 — негативное;
        3 — компромиссное;
        4 — нейтральное;
        5 — положительное.
         */
        val alcohol: Int) : Parcelable {
        companion object {
            fun parse(json: JSONObject?): Personal? {
                if (json == null) return null
                return Personal(
                    json.optInt("political"),
                    emptyList(),
                    json.optString("religion"),
                    json.optString("inspired_by"),
                    json.optInt("people_main"),
                    json.optInt("life_main"),
                    json.optInt("smoking"),
                    json.optInt("alcohol")
                )
            }
            fun toJson(personal: Personal): JSONObject {
                return JSONObject().apply {
                    put("political", personal.political)
                    put("religion", personal.religion)
                    put("inspired_by", personal.inspired_by)
                    put("people_main", personal.people_main)
                    put("life_main", personal.life_main)
                    put("smoking", personal.smoking)
                    put("alcohol", personal.alcohol)
                }
            }
        }
    }
    @Parcelize
    data class LastSeen(
        /**
         *  время последнего посещения в формате Unixtime
         */
        val time: Int,
        /**
         * тип платформы. Возможные значения:
        1 — мобильная версия;
        2 — приложение для iPhone;
        3 — приложение для iPad;
        4 — приложение для Android;
        5 — приложение для Windows Phone;
        6 — приложение для Windows 10;
        7 — полная версия сайта.
         */
        val platform: Int
    ) : Parcelable {
        companion object {
            fun parse(json: JSONObject): LastSeen {
                return LastSeen(
                    json.optInt("time"),
                    json.optInt("platform"),
                )
            }
            fun toJson(city: LastSeen): JSONObject {
                return JSONObject().apply {
                    put("time", city.time)
                    put("platform", city.platform)
                }
            }
        }
    }
    @Parcelize
    data class Country (
        /**
         * идентификатор страны, который можно использовать для получения ее названия с помощью метода database.getCountriesById
         */
        val id: Int,
        /**
         *  название страны
         */
        val title: String
    ) : Parcelable {
        companion object {
            fun parse(json: JSONObject?): Country? {
                if (json == null) return null
                return Country(
                    json.optInt("id"),
                    json.optString("title"),
                )
            }
            fun toJson(country: Country): JSONObject {
                return JSONObject().apply {
                    put("id", country.id)
                    put("title", country.title)
                }
            }
        }
    }
    @Parcelize
    data class City (
        /**
         * идентификатор города, который можно использовать для получения его названия с помощью метода database.getCitiesById
         */
        val id: Int,
        /**
         * название города
         */
        val title: String
    ) : Parcelable {
        companion object {
            fun parse(json: JSONObject?): City? {
                if (json == null) return null
                return City(
                    json.optInt("id"),
                    json.optString("title"),
                )
            }
            fun toJson(city: City): JSONObject {
                return JSONObject().apply {
                    put("id", city.id)
                    put("title", city.title)
                }
            }
        }
    }

    fun getFullName(): String {
        return "$first_name $last_name"
    }
    fun getScreenName(): String {
        return "@$domain"
    }

}