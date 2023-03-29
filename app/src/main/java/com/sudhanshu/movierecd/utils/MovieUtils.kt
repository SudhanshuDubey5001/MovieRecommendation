package com.sudhanshu.movierecd.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudhanshu.movierecd.Constants
import com.sudhanshu.movierecd.View
import com.sudhanshu.movierecd.data.Movie2
import com.sudhanshu.movierecd.welcomedialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class MovieUtils(context: Context) {
    val context = context
    private val items: MutableList<Movie2> = mutableListOf()

    suspend fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (ioException: IOException) {
                ioException.printStackTrace()
                null
            }
        }
    }

    suspend fun getContent(): List<Movie2> {
        if (items.isEmpty()) {
            val jsonFileString = getJsonDataFromAsset(context!!, Config().movieListPath)

            val gson = Gson()
            val listPersonType = object : TypeToken<List<Movie2>>() {}.type

            items.addAll(gson.fromJson(jsonFileString, listPersonType))
        }
        return items
    }

    //shared prefs to show welcome dialog only once
    fun showWelcomeDialog() {
        val sharedPreferences =
            context.getSharedPreferences("SharedPrefs_MRecommendation", Context.MODE_PRIVATE)
                ?: return
        if (sharedPreferences.getBoolean(Constants().SHARED_PREFS, true)) {
            welcomedialog.value = true
            with(sharedPreferences.edit()) {
                putBoolean(Constants().SHARED_PREFS, false)
                apply()
            }
        }
    }
}

