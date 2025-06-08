package com.example.bar.ui.crm

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("me")
    suspend fun getMyProfile(@Header("Authorization") token: String): UserProfileResponse

    @GET("orders/my")
    suspend fun getUserOrders(
        @Header("Authorization") token: String
    ): List<OrderResponse>

    @GET("orders")
    suspend fun getAllOrders(
        @Header("Authorization") token: String
    ): List<OrderResponse>

    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body order: OrderRequest
    ): List<OrderResponse>

    @PUT("orders/{id}")
    suspend fun updateOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int,
        @Body request: OrderUpdateRequest
    ): OrderResponse

    @DELETE("orders/{id}")
    suspend fun deleteOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): Response<ResponseBody>

    @PUT("users/{id}/ban")
    suspend fun banUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Response<ResponseBody>

    @PUT("users/{id}/role")
    suspend fun changeUserRole(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Query("role") role: String
    ): Response<ResponseBody>

    @PUT("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int,
        @Query("status") status: String
    ): OrderResponse

    @GET("users")
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): List<UserProfileResponse>

    @GET("orders/moderator")
    suspend fun getModeratorOrders(
        @Header("Authorization") token: String
    ): List<OrderResponse>

    @GET("orders/filter")
    suspend fun getFilteredOrders(
        @Header("Authorization") token: String,
        @Query("status") status: String?
    ): List<OrderResponse>



    companion object {
        private const val BASE_URL = "http://192.168.0.104:8080/"

        fun create(): ApiService {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
        }
    }
}

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(
    val message: String,
    val token: String,
    val role: String
)

data class UserProfileResponse(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val isBanned: Boolean
)

data class OrderRequest(
    val buildId: String,
    val contactPhone: String,
    val deliveryAddress: String,
    val notes: String? = null
)

data class OrderResponse(
    val orderId: Int,
    val userId: Int,
    val userEmail: String?, // Добавляем email пользователя
    val buildId: String,
    val buildDescription: String?, // Добавляем описание сборки
    val status: String,
    val contactPhone: String,
    val deliveryAddress: String,
    val notes: String?,
    val createdAt: String,
    val username: String?,
    val processedBy: Int?
)

data class OrderUpdateRequest(
    val buildId: String? = null,
    val status: String? = null,
    val contactPhone: String? = null,
    val deliveryAddress: String? = null,
    val notes: String? = null,
    var processedBy: Int? = null
)
