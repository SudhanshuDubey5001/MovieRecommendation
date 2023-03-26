package com.sudhanshu.movierecd

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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

class View() {

    var progressLoader = mutableStateOf(false)

    @Composable
    fun progressHUD() {
        if (progressLoader.value) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
    }

    //make movie frame using data class of Movie attributes
    @OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun movieFrame(movie: Movie, context: Context) {
        val openDialog = remember { mutableStateOf(false) }

        Surface(shape = MaterialTheme.shapes.medium, shadowElevation = 10.dp, onClick = {
            openDialog.value = true
        }) {
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

//                    Surface(modifier = Modifier.align(Alignment.End)) {
//                    }

                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        MaterialTheme {
            Column {
                if (openDialog.value) {
                    AlertDialog(
                        onDismissRequest = {
                            openDialog.value = false
                        },
                        title = {
                            Text(text = "Add this movie to favourites?")
                        },
                        text = {
                            Text(movie.title)
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (selectedMovies.size == 10) Toast.makeText(
                                        context,
                                        "Favourite list is full!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    else {
                                        movie.isFav = true
                                        selectedMovies.add(movie)
                                        moviesList.remove(movie)
                                        Toast.makeText(
                                            context,
                                            "Movie added",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    openDialog.value = false
                                }) {
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    openDialog.value = false
                                }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

        }
    }

    //use movie frame to make lists of movie frame
    @Composable
    fun makeMovieList(
        movies: List<Movie>,
        appBarTitle: String,
        isSearchBarEnabled: Boolean,
        context: Context,
        hide: Boolean
    ) {
        //show circular progress bar when necessary by changing load value
        progressHUD()
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

                    val focus = LocalFocusManager.current

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
                                                focus.clearFocus()
                                                progressLoader.value = true
                                                Log.d("myLog", "Query: " + textState.value.text)
                                                searchMovieAPICall(textState.value.text)
                                            }
                                    )
                                },
                                keyboardActions = KeyboardActions(onDone = { focus.clearFocus()})
                            )
                        }
                    }
                }
            }

            //list of movies
            val dialog = remember { mutableStateOf(false) }
            Box {
                LazyColumn(state = listState) {
                    items(movies.size) { index ->
                        movieFrame(movie = movies[index], context)
                    }
                }
                if(!hide) addFloatingButton(dialog)
            }
            MaterialTheme {
                if (dialog.value) {
                    AlertDialog(onDismissRequest = { dialog.value = false },
                        title = {
                            Text("Proceed with recommendation based on selected movies?")
                        },
                        text = {
                            Text(text = "Total movies selected: " + selectedMovies.size)
                        },
                        confirmButton = {
                            Button(onClick = {
                                val intent = Intent(context, RecmdMovies::class.java)
                                context.startActivity(intent)
                                dialog.value = false
                            }) {
                                Text(text = "Proceed")
                            }
                        },
                        dismissButton = {
                            Button(onClick = {
                                dialog.value = false
                            }) {
                                Text("Cancel")
                            }
                        })
                }
            }
        }
    }

    @Composable
    fun addFloatingButton(dialog: MutableState<Boolean>) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(all = 20.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current

            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedMovies.size > 0) {
                        dialog.value = true
                    } else Toast.makeText(
                        context,
                        "Favourite movies list is empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }, containerColor = Color(ContextCompat.getColor(context, R.color.titleColor)),
                contentColor = Color.White
            ) {
                Text(text = "Recommend")
//                Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "fab")
            }
        }
    }
}
