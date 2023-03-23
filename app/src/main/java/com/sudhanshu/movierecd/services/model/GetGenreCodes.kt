package com.sudhanshu.movierecd.services.model

data class GetGenreCodes(
    val genres: List<Genre>
)

data class Genre(
    val id: Int,
    val name: String
)