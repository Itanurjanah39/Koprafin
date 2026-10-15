package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(

    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : DataLogin?    = DataLogin()
)

data class DataLogin(
    @SerializedName("token"      ) var token     : String? = null,
    @SerializedName("expired_at" ) var expiredAt : String? = null,
    @SerializedName("role"       ) var role      : String? = null
)

