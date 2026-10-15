package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : DataPage?    = DataPage()
)

data class DataProduct(
    @SerializedName("id"          ) var id          : Int?      = null,
    @SerializedName("category_id" ) var categoryId  : Int?      = null,
    @SerializedName("name"        ) var name        : String?   = null,
    @SerializedName("price"       ) var price       : String?   = null,
    @SerializedName("description" ) var description : String?   = null,
    @SerializedName("picture"     ) var picture: List<String>? = null,
    @SerializedName("category"    ) var category    : Category? = Category()
)
data class Category (

    @SerializedName("id"         ) var id        : Int?    = null,
    @SerializedName("name"       ) var name      : String? = null,
    @SerializedName("min_tenor"  ) var minTenor  : Int?    = null,
    @SerializedName("maks_tenor" ) var maksTenor : Int?    = null

)


data class Links (

    @SerializedName("url"    ) var url    : String?  = null,
    @SerializedName("label"  ) var label  : String?  = null,
    @SerializedName("page"   ) var page   : String?  = null,
    @SerializedName("active" ) var active : Boolean? = null

)

data class DataPage (

    @SerializedName("current_page"   ) var currentPage  : Int?             = null,
    @SerializedName("data"           ) var data         : ArrayList<DataProduct>  = arrayListOf(),
    @SerializedName("first_page_url" ) var firstPageUrl : String?          = null,
    @SerializedName("from"           ) var from         : Int?             = null,
    @SerializedName("last_page"      ) var lastPage     : Int?             = null,
    @SerializedName("last_page_url"  ) var lastPageUrl  : String?          = null,
    @SerializedName("links"          ) var links        : ArrayList<Links> = arrayListOf(),
    @SerializedName("next_page_url"  ) var nextPageUrl  : String?          = null,
    @SerializedName("path"           ) var path         : String?          = null,
    @SerializedName("per_page"       ) var perPage      : Int?             = null,
    @SerializedName("prev_page_url"  ) var prevPageUrl  : String?          = null,
    @SerializedName("to"             ) var to           : Int?             = null,
    @SerializedName("total"          ) var total        : Int?             = null

)
