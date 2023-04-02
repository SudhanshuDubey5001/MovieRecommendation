package com.sudhanshu.movierecd


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.sudhanshu.movierecd.data.Movie
import com.sudhanshu.movierecd.services.RetrofitBuilder
import com.sudhanshu.movierecd.services.model.GetGenreCodes
import com.sudhanshu.movierecd.data.SearchMovie
import com.sudhanshu.movierecd.services.model.discoverMovies.DiscoverMoviesResponse
import com.sudhanshu.movierecd.utils.MovieUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

var selectedMovies = mutableListOf<Movie>()
val constants: Constants = Constants()
val genres = mutableMapOf<Int, String>()
var moviesList = mutableStateListOf<Movie>()
var progressLoader = mutableStateOf(false)
var welcomedialog = mutableStateOf(false)
var movienotFounddialog = mutableStateOf(false)
var errorDialogBox = mutableStateOf(false)

class MainActivity : ComponentActivity() {

    override fun onStart() {
        super.onStart()
        //show welcome dialog
        MovieUtils(this).showWelcomeDialog()
    }

    override fun onStop() {
        super.onStop()
        //clear the list when activity starts
        selectedMovies.clear()
        moviesList.clear()
        //checking if CI works :)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressLoader.value = true
        setContent {
            setupGenreCodesToGenre()
            View().makeMovieList(
                moviesList,
                "Add movies",
                true,
                this,
                false
            )
        }
    }


    //catch the error
    fun onFailure(t: Throwable) {
        Log.d("myLog", "Error: " + t.toString())
        progressLoader.value = false
        errorDialogBox.value = true
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
        progressLoader.value = false
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
                    true,
                    false
                )
            )
        }
    }

    //search the movie
    fun searchMovieAPICall(query: String) {
        progressLoader.value = true
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
    var confidenceString = mutableListOf<String>()
    var searchQueriesArray = mutableListOf<String>()
    fun searchMovieAPICallForArray() {
        if (i < searchQueriesArray.size) {
            Log.d("myLog", "Search query: " + searchQueriesArray[i])
            val compD = CompositeDisposable()
            compD.add(
                RetrofitBuilder()
                    .build(constants.baseURL_OMdb)
                    .searchOMdb(constants.OMdb_API_KEY, searchQueriesArray[i])
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { response -> response_searchQueryForArray(response) },
                        { t -> onFailure(t) })
            )
        } else {
            i = 0
            Log.d("myLog", "Search complete!!")
            progressLoader.value = false
        }
    }

    fun response_searchQueryForArray(response: SearchMovie) {
        Log.d("myLog", response.toString())
        //false warning as the search can return null sometimes
        if (response.Title != null) {
            val movie = Movie(
                response.Title + " (" + response.Year + ")",
                response.Plot,
                response.imdbRating,
                response.Genre,
                response.Poster,
                false,
                true,
                confidenceString[i]
            )
            recmdMoviesList.add(movie)
        }
        i++;
        searchMovieAPICallForArray()
    }

    //catch the response of search query
    fun response_searchQuery(response: SearchMovie) {
        //false warning as the search can return null sometimes
        if (response.Title != null) {
            progressLoader.value = false
            Log.d("myLog", response.toString())
            //empty the movies list and only show items selected by the user
            moviesList.clear()
            //add the search result at first index
            val movie = Movie(
                response.Title + " (" + response.Year + ")",
                response.Plot,
                response.imdbRating,
                response.Genre,
                response.Poster,
                false,
                false
            )
            moviesList.add(movie)
        } else {
            progressLoader.value = false
            movienotFounddialog.value = true
        }
    }

    fun setupSearchMovieArrayAndCofindence(query: MutableMap<String, Float>) {
        progressLoader.value = true
        for (r in query) {
            val confidence = r.value.toInt().toString()
            Log.d("myLog", "Confidence rating after modification: " + confidence)
            confidenceString.add(confidence)
            Log.d("myLog", r.key + "--->" + r.value)
            searchQueriesArray.add(r.key.substringBefore(" (").replace("\\s".toRegex(), "+"))
        }
        searchMovieAPICallForArray()
    }
}


