package com.dan.mad_project_geoquest.api

import retrofit2.http.*

// Replace with your team's key from Graeme — keeping public key for now
const val API_KEY = "577p2m"
const val BASE_URL = "https://mark0s.com/geoquest/v1/"

interface GeoQuestApiService {

    // ── Users ────────────────────────────────────────────────────
    @GET("api/users")
    suspend fun getUsers(@Query("key") key: String = API_KEY): List<User>

    @GET("api/users/{id}")
    suspend fun getUserById(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): User

    @POST("api/users")
    suspend fun createUser(
        @Body user: User,
        @Query("key") key: String = API_KEY
    ): User

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body user: User,
        @Query("key") key: String = API_KEY
    ): User

    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    )

    // ── Events ───────────────────────────────────────────────────
    @GET("api/events")
    suspend fun getEvents(@Query("key") key: String = API_KEY): List<Event>

    @GET("api/events/{id}")
    suspend fun getEventById(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Event

    @GET("api/events/users/{id}")
    suspend fun getEventsByUser(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): List<Event>

    @POST("api/events")
    suspend fun createEvent(
        @Body event: Event,
        @Query("key") key: String = API_KEY
    ): Event

    @PUT("api/events/{id}")
    suspend fun updateEvent(
        @Path("id") id: Int,
        @Body event: Event,
        @Query("key") key: String = API_KEY
    ): Event

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    )

    // ── Status ───────────────────────────────────────────────────
    @GET("api/status")
    suspend fun getStatuses(@Query("key") key: String = API_KEY): List<Status>

    // ── Players ──────────────────────────────────────────────────
    @GET("api/players")
    suspend fun getPlayers(@Query("key") key: String = API_KEY): List<Player>

    @GET("api/players/{id}")
    suspend fun getPlayerById(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Player

    @GET("api/players/events/{id}")
    suspend fun getPlayersByEvent(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): List<Player>

    @POST("api/players")
    suspend fun createPlayer(
        @Body player: Player,
        @Query("key") key: String = API_KEY
    ): Player

    @PUT("api/players/{id}")
    suspend fun updatePlayer(
        @Path("id") id: Int,
        @Body player: Player,
        @Query("key") key: String = API_KEY
    ): Player

    @DELETE("api/players/{id}")
    suspend fun deletePlayer(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    )

    // ── Caches ───────────────────────────────────────────────────
    @GET("api/caches")
    suspend fun getCaches(@Query("key") key: String = API_KEY): List<Cache>

    @GET("api/caches/{id}")
    suspend fun getCacheById(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Cache

    @GET("api/caches/events/{id}")
    suspend fun getCachesByEvent(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): List<Cache>

    @POST("api/caches")
    suspend fun createCache(
        @Body cache: Cache,
        @Query("key") key: String = API_KEY
    ): Cache

    @PUT("api/caches/{id}")
    suspend fun updateCache(
        @Path("id") id: Int,
        @Body cache: Cache,
        @Query("key") key: String = API_KEY
    ): Cache

    @DELETE("api/caches/{id}")
    suspend fun deleteCache(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    )

    // ── Finds ────────────────────────────────────────────────────
    @GET("api/finds")
    suspend fun getFinds(@Query("key") key: String = API_KEY): List<Find>

    @GET("api/finds/{id}")
    suspend fun getFindById(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Find

    @GET("api/finds/events/{id}")
    suspend fun getFindsByEvent(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): List<Find>

    @GET("api/finds/players/{id}")
    suspend fun getFindsByPlayer(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): List<Find>

    @POST("api/finds")
    suspend fun createFind(
        @Body find: Find,
        @Query("key") key: String = API_KEY
    ): Find

    @PUT("api/finds/{id}")
    suspend fun updateFind(
        @Path("id") id: Int,
        @Body find: Find,
        @Query("key") key: String = API_KEY
    ): Find

    @DELETE("api/finds/{id}")
    suspend fun deleteFind(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    )
}