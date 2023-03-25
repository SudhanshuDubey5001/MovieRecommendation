package com.sudhanshu.movierecd

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.sudhanshu.movierecd.data.Movie
import com.sudhanshu.movierecd.services.RetrofitBuilder
import com.sudhanshu.movierecd.services.model.GetGenreCodes
import com.sudhanshu.movierecd.data.SearchMovie
import com.sudhanshu.movierecd.services.model.discoverMovies.DiscoverMoviesResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


var selectedMovies = mutableListOf<Movie>()
val constants: Constants = Constants()
var isLoading = false
val genres = mutableMapOf<Int, String>()
var moviesList = mutableStateListOf<Movie>()


class MainActivity : ComponentActivity() {

    override fun onStart() {
        super.onStart()
        //clear the list when activity starts
        selectedMovies.clear()
        moviesList.clear()
        View().progressLoader.value = true
        Log.d("my","Lists are clear! ")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            setupGenreCodesToGenre()
            View().makeMovieList(moviesList, "Add movies", true,this)
        }
    }
}

//catch the error
fun onFailure(t: Throwable) {
    Log.d("myLog", "Error: " + t.toString())
}

//setup genre codes
fun setupGenreCodesToGenre() {
    val compD = CompositeDisposable()
    compD.add(
        RetrofitBuilder()
            .build(constants.baseURL_TMdb)
            .getGenreCodes()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { response -> onResponseGenreCode(response) },
                { t -> onFailure(t) })
    )
}

fun onResponseGenreCode(response: GetGenreCodes) {
    Log.d("myLog", "Genre codes: " + response.toString())
    for (genre in response.genres) {
        genres.put(genre.id, genre.name)
    }
    //now load movies
    loadMovieDiscoverData()
}

//get movie data from internet for initial population of list with popular/trending movies
fun loadMovieDiscoverData() {
    val compD = CompositeDisposable()
    compD.add(
        RetrofitBuilder()
            .build(constants.baseURL_TMdb)
            .discoverMoviesTMdb()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { response -> onResponse(response) },
                { t -> onFailure(t) })
    )
}

//catch response and make list
fun onResponse(response: DiscoverMoviesResponse) {
    View().progressLoader.value = false
    Log.d("myLog", "Response: " + response.toString())
    for (result in response.results) {
        //setup genre string
        var setGenre = ""
        var i = 0     //for erasing slash at the end of genre
        for (ids in result.genre_ids) {
            val slash = if (i == result.genre_ids.lastIndex) "" else "/"
            setGenre += genres.get(ids).toString() + slash
            i++
        }
        //setup every other attributes of each movie
        moviesList.add(
            Movie(
                result.title,
                result.overview,
                result.vote_average,
                setGenre,
                result.poster_path,
                true
            )
        )
    }
}

//search the movie
fun searchMovieAPICall(query: String) {
    val queryModified = query.replace("\\s".toRegex(), "+")
    Log.d("myLog", "Search query: " + queryModified)
    val compD = CompositeDisposable()
    compD.add(
        RetrofitBuilder()
            .build(constants.baseURL_OMdb)
            .searchOMdb(constants.OMdb_API_KEY, queryModified)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { response -> response_searchQuery(response) },
                { t -> onFailure(t) })
    )
}

var i = 0
var searchQueries = listOf<String>()
fun searchMovieAPICallForArray(query: List<String>) {
    searchQueries = query
    if(i<query.size) {
        val queryModified = searchQueries[i].substringBefore("(").replace("\\s".toRegex(), "+")
        Log.d("myLog", "Search query: " + queryModified)
        val compD = CompositeDisposable()
        compD.add(
            RetrofitBuilder()
                .build(constants.baseURL_OMdb)
                .searchOMdb(constants.OMdb_API_KEY, queryModified)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { response -> response_searchQueryForArray(response) },
                    { t -> onFailure(t) })
        )
    }else{
        i=0
        Log.d("myLog","Search complete!!")
    }
}

fun response_searchQueryForArray(response: SearchMovie) {
    View().progressLoader.value = false
    Log.d("myLog", response.toString())
    //dismiss the progress hud
    isLoading = false
    //add the search result at first index
    val movie = Movie(
        response.Title,
        response.Plot,
        response.imdbRating,
        response.Genre,
        response.Poster,
        false
    )
    recmdMoviesList.add(movie)
    i++;
    searchMovieAPICallForArray(searchQueries)
}

//catch the response of search query
fun response_searchQuery(response: SearchMovie) {
    View().progressLoader.value = false
    Log.d("myLog", response.toString())
    //dismiss the progress hud
    isLoading = false
    //empty the movies list and only show items selected by the user
    moviesList.clear()
    for (movie in selectedMovies) {
        moviesList.add(movie)
    }
    //add the search result at first index
    val movie = Movie(
        response.Title,
        response.Plot,
        response.imdbRating,
        response.Genre,
        response.Poster,
        false
    )
    moviesList.add(
        0,
        movie
    )
//    recmdMoviesList.add(movie)
}


