package com.sudhanshu.movierecd

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sudhanshu.movierecd.data.Movie

class View {

    @Composable
    fun progressHUD() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    }

    //make movie frame using data class of Movie attributes
    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun movieFrame(movie: Movie) {
        Surface(shape = MaterialTheme.shapes.medium, shadowElevation = 10.dp) {
            Row(modifier = Modifier.padding(10.dp)) {
                if (movie.isTMdb) {
                    GlideImage(
                        model = constants.image_baseURL + movie.poster,
                        contentDescription = "poster_image_TMdb",
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    GlideImage(
                        model = movie.poster,
                        contentDescription = "poster_image_OMdb",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        movie.title,
                        style = TextStyle(
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.titleColor)
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    Text(movie.descp, maxLines = 6, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Text("Genre: ", style = TextStyle(fontWeight = FontWeight.Bold))
                        Text(movie.genre)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        Text("Rating: ", style = TextStyle(fontWeight = FontWeight.Bold))
                        Text(movie.rating)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val checked = remember {
                        mutableStateOf(false)
                    }
                    //see if the movie is already added in favourite list, then toggle it true
                    if (selectedMovies.contains(movie)) checked.value = true

                    Surface(modifier = Modifier.align(Alignment.End)) {
                        Switch(checked = checked.value, onCheckedChange = {
                            checked.value = it
                            if (checked.value) {
                                selectedMovies.add(movie)
                                Log.d("myLog", "Movies added : " + movie.title)
                            }
                        })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }

    //use movie frame to make lists of movie frame
    @Composable
    fun makeMovieList(movies: List<Movie>, appBarTitle: String, isSearchBarEnabled: Boolean) {
        //show circular progress bar when necessary by changing load value
        var load by remember {
            mutableStateOf(false)
        }
        val listState = rememberLazyListState()
        Column {
            //App bar
            CenterAlignedTopAppBar(title = { Text(appBarTitle) })

            //Search movie
            if (isSearchBarEnabled) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    shadowElevation = 5.dp
                ) {
                    val textState = remember {
                        mutableStateOf(TextFieldValue())
                    }

                    Row() {
                        Surface(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = textState.value,
                                onValueChange = {
                                    textState.value = it
                                    Log.d("myLog", "Edittext value: " + textState.value)
                                },
                                placeholder = { Text("Search any movie") },
                                trailingIcon = {
                                    Image(
                                        painter = painterResource(id = R.drawable.sendicon),
                                        contentDescription = "Send",
                                        modifier = Modifier
                                            .padding(all = 10.dp)
                                            .clickable {
                                                load = true
                                                Log.d("myLog", "Query: " + textState.value.text)
                                                searchMovieAPICall(textState.value.text)
                                            }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            //list of movies

            Box {
                if (load) progressHUD()
                LazyColumn(state = listState) {
                    items(movies.size) { index ->
                        movieFrame(movie = movies[index])
                    }
                    load = false
                }
                addFloatingButton()
            }
        }
    }

    @Composable
    fun addFloatingButton() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(all = 20.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            val context = LocalContext.current
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, RecmdMovies::class.java)
                    context.startActivity(intent)
                }, containerColor = Color(ContextCompat.getColor(context, R.color.titleColor)),
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "fab")
            }
        }
    }
}