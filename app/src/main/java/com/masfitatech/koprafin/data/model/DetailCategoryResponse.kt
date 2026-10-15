package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName

data class DetailCategoryResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : DetailCategory?    = DetailCategory()
)
data class DetailCategory(
    @SerializedName("id"         ) var id        : Int?    = null,
    @SerializedName("name"       ) var name      : String? = null,
    @SerializedName("margin"     ) var margin    : Int?    = null,
    @SerializedName("min_tenor"  ) var minTenor  : Int?    = null,
    @SerializedName("maks_tenor" ) var maksTenor : Int?    = null
)
