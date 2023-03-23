package com.sudhanshu.movierecd.services.model.discoverMovies

data class DiscoverMoviesResponse(
    var results: List<Results>
//var response: String
)

data class Results(
    var id: Int,
    var title: String,
    var release_date: String,
    var overview: String,
    var poster_path: String,
    var genre_ids: List<Int>,
    var vote_average: String
)