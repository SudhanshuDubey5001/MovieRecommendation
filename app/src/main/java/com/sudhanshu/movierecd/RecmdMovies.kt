package com.sudhanshu.movierecd

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import com.sudhanshu.movierecd.utils.Config
import com.sudhanshu.movierecd.data.Movie
import com.sudhanshu.movierecd.data.Movie2
import com.sudhanshu.movierecd.data.ResultRecommendedMovies
import com.sudhanshu.movierecd.utils.MovieUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.round

val recmdMoviesList = mutableStateListOf<Movie>()

class RecmdMovies : ComponentActivity() {

    val recommendationClient = RecommendationClient(this, Config())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val movieUtils = MovieUtils(this)
        var tensorFlowMovieList: List<Movie2>
        var inputMovieData = mutableListOf<Movie2>()

        runBlocking {
            launch {
                //get the tensorflow dataset movie list
                tensorFlowMovieList = movieUtils.getContent()

                //search movies in the tensorflow dataset otherwise add movies with similar genres
                var found = false

                for (i in selectedMovies) {
                    for (j in tensorFlowMovieList) {
                        if (j.title.contains(i.title, true)) {
                            inputMovieData.add(j)
                            found = true
                            Log.d("myLog", "Found!! :)")
                        }
                    }
                }

                if (!found) {
                    for (i in selectedMovies) {
                        for (j in tensorFlowMovieList) {
                            for (genre in j.genres) {
                                if (i.genre.contains(genre, true)) {
                                    if (inputMovieData.size < 10) inputMovieData.add(j)
                                }
                            }
                            inputMovieData.toSet().toList() //to remove duplicate items
                        }
                        inputMovieData.toSet().toList() //to remove duplicate items
                    }
                }

                Log.d("myLog", "Movies added: " + inputMovieData)

                recommendationClient.load()
                val list = recommendationClient.recommend(inputMovieData)
                Log.d("myLog", "Movies recommendations : " + list.size)
                //now search all the 10 movies to get Movie data->
                val resultRecommendedMovies = mutableMapOf<String, Float>()
                for (result in list) {
//                    searchQueries.add(ResultRecommendedMovies(result.item.title, result.confidence))
                    resultRecommendedMovies.put(result.item.title, result.confidence * 100f)
                }
                MainActivity().setupSearchMovieArrayAndCofindence(resultRecommendedMovies)
            }
        }

        setContent {
            View().makeMovieList(
                movies = recmdMoviesList,
                "Recommendations",
                false,
                this,
                true
            )
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                selectedMovies.clear()
                val intent = Intent(this@RecmdMovies, MainActivity::class.java)
                this@RecmdMovies.startActivity(intent)
                this@RecmdMovies.finish()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        recommendationClient.unload()
        recmdMoviesList.clear()
    }

}

