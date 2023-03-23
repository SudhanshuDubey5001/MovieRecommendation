package com.sudhanshu.movierecd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sudhanshu.movierecd.data.Movie

class RecmdMovies : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tf = TensorFlowRecommendation(this)
        tf.performRecommendation()

        val moviesList2 = listOf<Movie>(Movie("Batman",
            "here we go again",
            "2.4",
            "thriller",
            "https://m.media-amazon.com/images/M/MV5BMTMwNjAxMTc0Nl5BMl5BanBnXkFtZTcwODc3ODk5Mg@@._V1_SX300.jpg"
            ,false))

        setContent{
            View().makeMovieList(movies = moviesList2, "Recommendations",false)
        }

    }
}

