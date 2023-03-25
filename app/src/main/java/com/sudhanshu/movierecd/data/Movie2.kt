package com.sudhanshu.movierecd.data

data class Movie2(
    val id: Int,
    val title: String,
    val genres: List<String>,
    val count: Int,
    var liked: Boolean
)