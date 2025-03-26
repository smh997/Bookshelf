package com.example.bookshelf.network

import com.google.gson.annotations.SerializedName


data class BooksResponse(
    val totalItems: Int,
    val items: List<BookItem>?
)

data class BookItem(
    @SerializedName("id") val id: String,
    @SerializedName("volumeInfo") val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    @SerializedName("title") val title: String,
    @SerializedName("authors") val authors: List<String>?,
    @SerializedName("publisher") val publisher: String?,
    @SerializedName("publishedDate") val publishedDate: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("pageCount") val pageCount: Int?,
    @SerializedName("imageLinks") val imageLinks: ImageLinks?,
    @SerializedName("infoLink") val infoLink: String?
)

data class ImageLinks(
    @SerializedName("thumbnail") val thumbnail: String?
)
