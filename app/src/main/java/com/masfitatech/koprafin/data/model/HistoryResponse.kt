package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName

data class HistoryResponse(
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: PaginatedHistory? = PaginatedHistory()
)

data class PaginatedHistory(

    @SerializedName("current_page") var currentPage: Int? = null,
    @SerializedName("data") var data: ArrayList<DataHistory> = arrayListOf(),
    @SerializedName("first_page_url") var firstPageUrl: String? = null,
    @SerializedName("from") var from: Int? = null,
    @SerializedName("last_page") var lastPage: Int? = null,
    @SerializedName("last_page_url") var lastPageUrl: String? = null,
    @SerializedName("links") var links: ArrayList<Links> = arrayListOf(),
    @SerializedName("next_page_url") var nextPageUrl: String? = null,
    @SerializedName("path") var path: String? = null,
    @SerializedName("per_page") var perPage: Int? = null,
    @SerializedName("prev_page_url") var prevPageUrl: String? = null,
    @SerializedName("to") var to: Int? = null,
    @SerializedName("total") var total: Int? = null
)

data class DataHistory(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("employee_id") var employeeId: Int? = null,
    @SerializedName("product_id") var productId: Int? = null,
    @SerializedName("tenor") var tenor: Int? = null,
    @SerializedName("total") var total: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("product") var product: DataProduct? = DataProduct(),
    @SerializedName("employee") var employee: Employee? = Employee()
)
