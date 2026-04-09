package com.dan.mad_project_geoquest.api

import com.google.gson.annotations.SerializedName



data class User(
    @SerializedName("UserID") val UserID: Int = 0,
    @SerializedName("UserFirstname") val UserFirstname: String = "",
    @SerializedName("UserLastname") val UserLastname: String = "",
    @SerializedName("UserPhone") val UserPhone: String = "",
    @SerializedName("UserUsername") val UserUsername: String = "",
    @SerializedName("UserPassword") val UserPassword: String = "",
    @SerializedName("UserLatitude") val UserLatitude: Double = 0.0,
    @SerializedName("UserLongitude") val UserLongitude: Double = 0.0,
    @SerializedName("UserTimestamp") val UserTimestamp: Double = 0.0,
    @SerializedName("UserImageURL") val UserImageURL: String = ""
)

data class Event(
    @SerializedName("EventID") val EventID: Int = 0,
    @SerializedName("EventName") val EventName: String = "",
    @SerializedName("EventDescription") val EventDescription: String = "",
    @SerializedName("EventOwnerID") val EventOwnerID: Int = 0,
    @SerializedName("EventIspublic") val EventIspublic: Boolean = true,
    @SerializedName("EventStart") val EventStart: String = "",
    @SerializedName("EventFinish") val EventFinish: String = "",
    @SerializedName("EventStatusID") val EventStatusID: Int = 0,
    @SerializedName("EventOwner") val EventOwner: User? = null,
    @SerializedName("EventStatus") val EventStatus: Status? = null
)

data class Status(
    @SerializedName("StatusID") val StatusID: Int = 0,
    @SerializedName("StatusName") val StatusName: String = "",
    @SerializedName("StatusOrder") val StatusOrder: Int = 0
)

data class Player(
    @SerializedName("PlayerID") val PlayerID: Int = 0,
    @SerializedName("PlayerUserID") val PlayerUserID: Int = 0,
    @SerializedName("PlayerEventID") val PlayerEventID: Int = 0,
    @SerializedName("PlayerUser") val PlayerUser: User? = null,
    @SerializedName("PlayerEvent") val PlayerEvent: Event? = null
)

data class Cache(
    @SerializedName("CacheID") val CacheID: Int = 0,
    @SerializedName("CacheName") val CacheName: String = "",
    @SerializedName("CacheDescription") val CacheDescription: String = "",
    @SerializedName("CacheEventID") val CacheEventID: Int = 0,
    @SerializedName("CacheImageURL") val CacheImageURL: String = "",
    @SerializedName("CacheClue") val CacheClue: String = "",
    @SerializedName("CachePoints") val CachePoints: Double = 0.0,
    @SerializedName("CacheLatitude") val CacheLatitude: Double = 0.0,
    @SerializedName("CacheLongitude") val CacheLongitude: Double = 0.0,
    @SerializedName("CacheEvent") val CacheEvent: Event? = null
)

data class Find(
    @SerializedName("FindID") val FindID: Int = 0,
    @SerializedName("FindPlayerID") val FindPlayerID: Int = 0,
    @SerializedName("FindCacheID") val FindCacheID: Int = 0,
    @SerializedName("FindDatetime") val FindDatetime: String = "",
    @SerializedName("FindImageURL") val FindImageURL: String = "",
    @SerializedName("FindPlayer") val FindPlayer: Player? = null,
    @SerializedName("FindCache") val FindCache: Cache? = null
)



data class UserPayload(
    @SerializedName("UserFirstname") val UserFirstname: String,
    @SerializedName("UserLastname") val UserLastname: String,
    @SerializedName("UserPhone") val UserPhone: String,
    @SerializedName("UserUsername") val UserUsername: String,
    @SerializedName("UserPassword") val UserPassword: String,
    @SerializedName("UserLatitude") val UserLatitude: Double,
    @SerializedName("UserLongitude") val UserLongitude: Double,
    @SerializedName("UserTimestamp") val UserTimestamp: Double,
    @SerializedName("UserImageURL") val UserImageURL: String
)

data class EventPayload(
    @SerializedName("EventName") val EventName: String,
    @SerializedName("EventDescription") val EventDescription: String,
    @SerializedName("EventOwnerID") val EventOwnerID: Int,
    @SerializedName("EventIspublic") val EventIspublic: Boolean,
    @SerializedName("EventStart") val EventStart: String,
    @SerializedName("EventFinish") val EventFinish: String,
    @SerializedName("EventStatusID") val EventStatusID: Int
)

data class PlayerPayload(
    @SerializedName("PlayerUserID") val PlayerUserID: Int,
    @SerializedName("PlayerEventID") val PlayerEventID: Int
)

data class CachePayload(
    @SerializedName("CacheName") val CacheName: String,
    @SerializedName("CacheDescription") val CacheDescription: String,
    @SerializedName("CacheEventID") val CacheEventID: Int,
    @SerializedName("CacheImageURL") val CacheImageURL: String,
    @SerializedName("CacheClue") val CacheClue: String,
    @SerializedName("CachePoints") val CachePoints: Double,
    @SerializedName("CacheLatitude") val CacheLatitude: Double,
    @SerializedName("CacheLongitude") val CacheLongitude: Double
)

data class FindPayload(
    @SerializedName("FindPlayerID") val FindPlayerID: Int,
    @SerializedName("FindCacheID") val FindCacheID: Int,
    @SerializedName("FindDatetime") val FindDatetime: String,
    @SerializedName("FindImageURL") val FindImageURL: String
)