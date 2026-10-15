package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName

data class DetailProductResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : DataDetail?    = DataDetail()
)
data class DataDetail (

    @SerializedName("id"          ) var id          : Int?              = null,
    @SerializedName("category_id" ) var categoryId  : Int?              = null,
    @SerializedName("name"        ) var name        : String?           = null,
    @SerializedName("price"       ) var price       : String?           = null,
    @SerializedName("description" ) var description : String?           = null,
    @SerializedName("picture"     ) var picture     : ArrayList<String> = arrayListOf(),
    @SerializedName("category"    ) var category    : Category?         = Category()

)
