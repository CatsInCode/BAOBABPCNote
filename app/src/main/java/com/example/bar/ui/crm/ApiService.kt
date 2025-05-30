package com.example.bar.ui.crm

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: OrderRequest
    ): OrderResponse

    @PUT("orders/{id}")
    suspend fun updateOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int,
        @Body request: OrderRequest
    ): OrderResponse

    @DELETE("orders/{id}")
    suspend fun deleteOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): Response<Unit>

    @GET("orders/my")
    suspend fun getUserOrders(
        @Header("Authorization") token: String
    ): List<OrderResponse>

    @GET("orders")
    suspend fun getAllOrders(
        @Header("Authorization") token: String
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

data class OrderRequest(
    val buildId: String,
    val contactPhone: String,
    val deliveryAddress: String,
    val notes: String? = null
)

data class OrderResponse(
    val orderId: Int,
    val userId: Int,
    val buildId: String,
    val status: String,
    val contactPhone: String,
    val deliveryAddress: String,
    val notes: String?,
    val createdAt: String
)