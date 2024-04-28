package com.ifs21023.lostandfound.data.remote.retrofit

import com.ifs21023.lostandfound.data.remote.response.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface IApiService {
    @FormUrlEncoded
    @POST("auth/register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): DelcomResponse

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): DelcomLoginResponse

    @GET("users/me")
    suspend fun getMe(): DelcomUserResponse

    @FormUrlEncoded
    @POST("lost-founds")
    suspend fun postLostFound(
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("status") status: String?,
    ): DelcomAddLostFoundResponse

    @FormUrlEncoded
    @PUT("lost-founds/{id}")
    suspend fun putLostFound(
        @Path("id") lostfoundId: Int,
        @Field("title") title: String,
        @Field("description") description: String,
        @Query("status") status: String?,
        @Query("is_completed") isCompleted: Int?
    ): DelcomResponse

    @GET("lost-founds")
    suspend fun getLostFounds(
        @Query("is_completed") isCompleted: Int?,
        @Query("is_me") isMe: Int?,
        @Query("status") status: String?
    ): DelcomLostFoundsResponse

    @GET("lost-founds/{id}")
    suspend fun getLostFound(
        @Path("id") lostfoundId: Int,
    ): DelcomLostFoundResponse

    @DELETE("lost-founds/{id}")
    suspend fun deleteLostFound(
        @Path("id") lostfoundId: Int,
    ): DelcomResponse

    @Multipart
    @POST("lost-founds/{id}/cover")
    suspend fun addCoverLostFound(
        @Path("id") lostFoundId: Int,
        @Part cover: MultipartBody.Part,
    ): DelcomResponse

    @Multipart
    @POST("users/photo")
    suspend fun  addphoto(
        @Part photo : MultipartBody.Part,
    ):DelcomResponse
}
