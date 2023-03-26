package com.sudhanshu.movierecd

import android.content.Context
import android.util.Log
import com.sudhanshu.movierecd.utils.Config
import com.sudhanshu.movierecd.data.Movie2
import com.sudhanshu.movierecd.utils.FileUtils
import com.sudhanshu.movierecd.utils.MovieUtils
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import kotlinx.coroutines.*
import java.io.File

class RecommendationClient(private val context: Context, private val config: Config) {
    private val candidates: MutableMap<Int, Movie2> = HashMap()
    private var tflite: Interpreter? = null

    data class Result(
        val id: Int,
        val item: Movie2,
        val confidence: Float
    )

    /** Load the TF Lite model and dictionary.  */
    suspend fun load() {
        loadLocalModel()
        loadCandidateList()
    }

    private suspend fun loadLocalModel() {
        return withContext(Dispatchers.IO) {
            try {
                val buffer: ByteBuffer = FileUtils.loadModelFile(
                    context.assets, config.modelPath
                )
                initializeInterpreter(buffer)
                Log.v(TAG, "TFLite model loaded.")
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            }
        }
    }

    private suspend fun loadCandidateList() {
        val tf_movieList = MovieUtils(context).getContent()
        for (movie in tf_movieList) {
            candidates.put(movie.id, movie)
        }
        Log.d("myLog", "Candidate list loaded!")
    }

    private suspend fun preprocess(selectedMovies: List<Movie2>): IntArray {
        return withContext(Dispatchers.Default) {
            val inputContext = IntArray(config.inputLength)
            for (i in 0 until config.inputLength) {
                if (i < selectedMovies.size) {
                    val (id) = selectedMovies[i]
                    inputContext[i] = id
                } else {
                    // Padding input.
                    inputContext[i] = config.pad
                }
            }
            inputContext
        }
    }

    private suspend fun initializeInterpreter(model: Any) {
        return withContext(Dispatchers.IO) {
            tflite?.apply {
                close()
            }
            if (model is ByteBuffer) {
                tflite = Interpreter(model)
            } else {
                tflite = Interpreter(model as File)
            }
            Log.v(TAG, "TFLite model loaded.")
        }
    }

    fun unload() {
        tflite?.close()
        candidates.clear()
    }

    suspend fun recommend(selectedMovies: List<Movie2>): List<Result> {
        return withContext(Dispatchers.Default) {
            val inputs = arrayOf<Any>(preprocess(selectedMovies))

            // Run inference.
            val outputIds = IntArray(config.outputLength)
            val confidences = FloatArray(config.outputLength)
            val outputs: MutableMap<Int, Any> = HashMap()
            outputs[config.outputIdsIndex] = outputIds
            outputs[config.outputScoresIndex] = confidences
            tflite?.let {
                it.runForMultipleInputsOutputs(inputs, outputs)
                Log.d("myLog", "Output ids: " + outputs)
                postprocess(outputIds, confidences, selectedMovies)
            } ?: run {
                Log.e(TAG, "No tflite interpreter loaded")
                emptyList()
            }
        }
    }

    private suspend fun postprocess(
        outputIds: IntArray, confidences: FloatArray, selectedMovies: List<Movie2>
    ): List<Result> {
        return withContext(Dispatchers.Default) {
            val results = ArrayList<Result>()
            var i = 0
            while (results.size <= config.topK) {
                val movie: Movie2? = candidates.get(outputIds[i])
                if (movie != null) {
                    if (!selectedMovies.contains(movie)) results.add(
                        Result(
                            outputIds[i],
                            movie,
                            confidences[i]
                        )
                    )
                } else Log.d("myLog", "No movie found with id : " + outputIds[i])
                i++
            }
            results
        }
    }

    companion object {
        private const val TAG = "RecommendationClient"
    }
}