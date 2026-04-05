package com.dan.mad_project_geoquest.api

data class User(
    val UserID: Int? = null,
    val UserFirstname: String = "",
    val UserLastname: String = "",
    val UserPhone: String = "",
    val UserUsername: String = "",
    val UserPassword: String = "",
    val UserLatitude: Double = 0.0,
    val UserLongitude: Double = 0.0,
    val UserTimestamp: Long = 0L,
    val UserImageURL: String = ""
)

data class Event(
    val EventID: Int? = null,
    val EventName: String = "",
    val EventDescription: String = "",
    val EventOwnerID: Int = 0,
    val EventIspublic: Boolean = true,
    val EventStart: String = "",
    val EventFinish: String = "",
    val EventStatusID: Int = 0,
    val EventOwner: User? = null,
    val EventStatus: Status? = null
)

data class Status(
    val StatusID: Int? = null,
    val StatusName: String = "",
    val StatusOrder: Int = 0
)

data class Player(
    val PlayerID: Int? = null,
    val PlayerUserID: Int = 0,
    val PlayerEventID: Int = 0,
    val PlayerUser: User? = null,
    val PlayerEvent: Event? = null
)

data class Cache(
    val CacheID: Int? = null,
    val CacheName: String = "",
    val CacheDescription: String = "",
    val CacheEventID: Int = 0,
    val CacheImageURL: String = "",
    val CacheClue: String = "",
    val CachePoints: Double = 0.0,
    val CacheLatitude: Double = 0.0,
    val CacheLongitude: Double = 0.0,
    val CacheEvent: Event? = null
)

data class Find(
    val FindID: Int? = null,
    val FindPlayerID: Int = 0,
    val FindCacheID: Int = 0,
    val FindDatetime: String = "",
    val FindImageURL: String = "",
    val FindPlayer: Player? = null,
    val FindCache: Cache? = null
)