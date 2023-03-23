package com.sudhanshu.movierecd.services

import com.sudhanshu.movierecd.services.model.GetGenreCodes
import com.sudhanshu.movierecd.data.SearchMovie
import com.sudhanshu.movierecd.services.model.discoverMovies.DiscoverMoviesResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

const val apiKey_OMdb = "1908460b"
const val apiKey_TMdb = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlMjFiZDM0YmJiMjE5ODZhNzkzNmVhNmQzM2Q0MjcyYyIsInN1YiI6IjY0MTg3ODA2NmEyMjI3MDA4NWYwZGNkZiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.O3yAtdAPwikWjRIEderEKCzIUTFhjBAScxaoZFYHkmw"

interface MovieAPIs {

    //for initial population with trending movies
    @Headers("Content-Type: application/json",
        "Authorization: "+ apiKey_TMdb)
    @GET("discover/movie")
    fun discoverMoviesTMdb(): Observable<DiscoverMoviesResponse>


    //for getting the genres based on genre codes
    @Headers("Content-Type: application/json",
        "Authorization: "+ apiKey_TMdb)
    @GET("genre/movie/list")
    fun getGenreCodes(): Observable<GetGenreCodes>

    //for searching a particular movie
    @GET("/")
    fun searchOMdb(@Query("apikey") apiKey:String, @Query("t") searchQuery: String): Observable<SearchMovie>
}