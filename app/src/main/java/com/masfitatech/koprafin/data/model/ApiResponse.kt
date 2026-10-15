package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName


data class ApiResponse (

    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null

)