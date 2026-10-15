package com.masfitatech.koprafin.data.network

import com.masfitatech.koprafin.data.model.AccountResponse
import com.masfitatech.koprafin.data.model.ApiResponse
import com.masfitatech.koprafin.data.model.BannerResponse
import com.masfitatech.koprafin.data.model.BannerStatusRequest
import com.masfitatech.koprafin.data.model.CategoryResponse
import com.masfitatech.koprafin.data.model.CounterCreditResponse
import com.masfitatech.koprafin.data.model.DetailCategoryResponse
import com.masfitatech.koprafin.data.model.DetailCreditResponse
import com.masfitatech.koprafin.data.model.DetailProductResponse
import com.masfitatech.koprafin.data.model.EmployeeDetailResponse
import com.masfitatech.koprafin.data.model.EmployeeResponse
import com.masfitatech.koprafin.data.model.HistoryDetailResponse
import com.masfitatech.koprafin.data.model.HistoryResponse
import com.masfitatech.koprafin.data.model.LoginResponse
import com.masfitatech.koprafin.data.model.ProductResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("auth/sign-in")
    suspend fun login(
        @Field("email") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @GET("auth/account")
    suspend fun getAccount(
        @Header("Authorization") token: String,
    ): Response<AccountResponse>

    @FormUrlEncoded
    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Field("email") email : String,
    ): Response<ApiResponse>

    @FormUrlEncoded
    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Field("email") email : String,
        @Field("otp") otp : String,
        @Field("new_password") newPass : String,
        @Field("new_password_confirmation") newPassConfirm : String,
    ): Response<ApiResponse>

    @GET("credit-applications/count/status")
    suspend fun getCreditStatus(
        @Header("Authorization") token: String,
    ): Response<CounterCreditResponse>


    @GET("credit-applications")
    suspend fun getHistory(
        @Header("Authorization") token: String,
    ): Response<HistoryResponse>

    @GET("credit-applications/{id}")
    suspend fun getHistoryDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<HistoryDetailResponse>

    @FormUrlEncoded
    @PUT("credit-applications/{id}/approval")
    suspend fun creditActions(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Field("action") action : String,
        @Field("remarks") remarks : String?
    ): Response<ApiResponse>

    @FormUrlEncoded
    @PUT("credit-applications/{id}/settlement")
    suspend fun creditFinish(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Field("remarks") remarks : String?
    ): Response<ApiResponse>


    @GET("categories")
    suspend fun getCategories(
        @Header("Authorization") token: String,
    ): Response<CategoryResponse>

    @GET("categories/{id}")
    suspend fun detailCategory(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<DetailCategoryResponse>

    @FormUrlEncoded
    @POST("categories")
    suspend fun createCategory(
        @Header("Authorization") token: String,
        @Field("name") name : String,
        @Field("min_tenor") minTenor : Int,
        @Field("maks_tenor") maxTenor : Int,
        @Field("margin") margin : Int,
    ): Response<ApiResponse>

    @FormUrlEncoded
    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Field("name") name : String?,
    @Field("min_tenor") minTenor : Int?,
    @Field("maks_tenor") maxTenor : Int?,
    @Field("margin") margin : Int?,
    ): Response<ApiResponse>

    @PUT("categories/{id}/delete")
    suspend fun deleteCategory(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse>

    //banner
    @GET("banners")
    suspend fun getBanner(
        @Header("Authorization") token: String,
    ): Response<BannerResponse>


    @Multipart
    @POST("banners")
    suspend fun createBanner(
        @Header("Authorization") token: String,
        @Part("status") status: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse>


    @PUT("banners/{id}/status")
    suspend fun updateBanner(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: BannerStatusRequest
    ): Response<ApiResponse>

    @PUT("banners/{id}/delete")
    suspend fun deleteBanner(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse>

    @GET("products")
    suspend fun getProducts(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = 1,
        @Query("per_page") perpage: Int? ,
        @Query("category_id") categoryId: Int? = null,
        @Query("search") search: String? = null
    ): Response<ProductResponse>
    @Multipart
    @POST("products")
    suspend fun addProduct(
        @Header("Authorization") token: String,
        @Part("category_id") categoryId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("description") description: RequestBody,
        @Part pictures: List<MultipartBody.Part>
    ): Response<ApiResponse>

    @Multipart
    @POST("products/{id}")
    suspend fun updateProduct(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part("category_id") categoryId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("description") description: RequestBody,
        @Part pictures: List<MultipartBody.Part>
    ): Response<ApiResponse>

    @GET("products/{id}")
    suspend fun getDetailProduct(
        @Header("Authorization") token: String,
        @Path("id") id : Int
    ): Response<DetailProductResponse>


    @PUT("products/{id}/delete")
    suspend fun deleteProduct(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse>


    @FormUrlEncoded
    @POST("credit-applications")
    suspend fun postApplyCredit(
        @Header("Authorization") token: String,
        @Field("product_id") id : Int,
        @Field("amount") amount : Int,
        @Field("tenor") tenor : Int
    ): Response<ApiResponse>

    @GET("credit-applications/{id}")
    suspend fun getDetailCredit(
        @Header("Authorization") token: String,
        @Path("id") id : Int
    ): Response<DetailCreditResponse>


    @GET("employees")
    suspend fun getEmployees(
        @Header("Authorization") token: String
    ): Response<EmployeeResponse>


    @GET("employees/{id}")
    suspend fun getEmployeeDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<EmployeeDetailResponse>


    @Multipart
    @POST("employees")
    suspend fun createEmployee(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("role") role: RequestBody,
        @Part("company") company: RequestBody,
        @Part("division") division: RequestBody,
        @Part("work_entry_date") joinDate: RequestBody,
        @Part avatar: MultipartBody.Part?
    ): Response<ApiResponse>


    @FormUrlEncoded
    @PUT("employees/{id}")
    suspend fun updateEmployee(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Field("name") name: String,
        @Field("nik") nik: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("role") role: String,
        @Field("company") company: String,
        @Field("division") division: String,
        @Field("work_entry_date") joinDate: String
    ): Response<ApiResponse>


    @Multipart
    @POST("employees/avatar/{id}")
    suspend fun updateAvatarEmployee(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part avatar: MultipartBody.Part
    ): Response<ApiResponse>

    @PUT("employees/{id}/delete")
    suspend fun deleteEmployee(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse>




}