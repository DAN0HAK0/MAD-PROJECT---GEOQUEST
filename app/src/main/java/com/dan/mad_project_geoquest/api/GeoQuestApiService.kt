package com.dan.mad_project_geoquest.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GeoQuestApiService {

    @GET("users")
    suspend fun getUsers(@Query("key") key: String = API_KEY): List<User>

    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): User

    @POST("users")
    suspend fun createUser(
        @Body user: UserPayload,
        @Query("key") key: String = API_KEY
    ): Response<ResponseBody>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body user: UserPayload,
        @Query("key") key: String = API_KEY
    ): Response<ResponseBody>

    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Response<Unit>


    @GET("events")
    suspend fun getEvents(@Query("key") key: String = API_KEY): List<Event>

    @GET("events/{id}")
    suspend fun getEventById(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Event

    @GET("events/users/{id}")
    suspend fun getEventsByUser(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): List<Event>

    @POST("events")
    suspend fun createEvent(
        @Body event: EventPayload,
        @Query("key") key: String = API_KEY
    ): Response<ResponseBody>

    @PUT("events/{id}")
    suspend fun updateEvent(
        @Path("id") id: Int,
        @Body event: EventPayload,
        @Query("key") key: String = API_KEY
    ): Response<ResponseBody>

    @DELETE("events/{id}")
    suspend fun deleteEvent(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Response<Unit>



    @GET("status")
    suspend fun getStatuses(@Query("key") key: String = API_KEY): List<Status>




    @GET("players")
    suspend fun getPlayers(@Query("key") key: String = API_KEY): List<Player>

    @GET("players/{id}")
    suspend fun getPlayerById(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Player

    @GET("players/events/{id}")
    suspend fun getPlayersByEvent(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): List<Player>

    @POST("players")
    suspend fun createPlayer(
        @Body player: PlayerPayload,
        @Query("key") key: String = API_KEY
    ): Response<ResponseBody>

    @PUT("players/{id}")
    suspend fun updatePlayer(
        @Path("id") id: Int,
        @Body player: PlayerPayload,
        @Query("key") key: String = API_KEY
    ): Response<ResponseBody>

    @DELETE("players/{id}")
    suspend fun deletePlayer(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Response<Unit>






    @GET("caches")
    suspend fun getCaches(@Query("key") key: String = API_KEY): List<Cache>

    @GET("caches/{id}")
    suspend fun getCacheById(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Cache

    @GET("caches/events/{id}")
    suspend fun getCachesByEvent(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): List<Cache>

    @POST("caches")
    suspend fun createCache(
        @Body cache: CachePayload,
        @Query("key") key: String = API_KEY
    ): Response<ResponseBody>

    @PUT("caches/{id}")
    suspend fun updateCache(
        @Path("id") id: Int,
        @Body cache: CachePayload,
        @Query("key") key: String = API_KEY
    ): Response<ResponseBody>

    @DELETE("caches/{id}")
    suspend fun deleteCache(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Response<Unit>






    @GET("finds")
    suspend fun getFinds(@Query("key") key: String = API_KEY): List<Find>

    @GET("finds/{id}")
    suspend fun getFindById(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Find

    @GET("finds/events/{id}")
    suspend fun getFindsByEvent(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): List<Find>

    @GET("finds/players/{id}")
    suspend fun getFindsByPlayer(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): List<Find>

    @POST("finds")
    suspend fun createFind(
        @Body find: FindPayload,
        @Query("key") key: String = API_KEY
    ): Response<ResponseBody>

    @PUT("finds/{id}")
    suspend fun updateFind(
        @Path("id") id: Int,
        @Body find: FindPayload,
        @Query("key") key: String = API_KEY
    ): Response<ResponseBody>

    @DELETE("finds/{id}")
    suspend fun deleteFind(
        @Path("id") id: Int,
        @Query("key") key: String = API_KEY
    ): Response<Unit>
}