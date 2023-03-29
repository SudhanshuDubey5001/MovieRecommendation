package com.sudhanshu.movierecd

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sudhanshu.movierecd.data.Movie

class View() {
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
            if (!movie.isRecommended) openDialog.value = true
        }) {
            Row(modifier = Modifier.padding(10.dp)) {
                if (movie.isTMdb) {
                    AsyncImage(
                        model = constants.image_baseURL + movie.poster,
                        contentDescription = "poster_image_TMdb",
                        modifier = Modifier.weight(1f),
                        error = painterResource(id = R.drawable.error_image)
                    )
                } else {
                    AsyncImage(
                        model = movie.poster,
                        contentDescription = "poster_image_OMdb",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.error_image)
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

                    //show confidence % if the movie object is from recommended movies
                    if (movie.isRecommended) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(10.dp)
                        ) {
                            Column() {
                                Text(
                                    text = "Match", style = TextStyle(
                                        color = Color(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.titleColor
                                            )
                                        ),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = movie.confidence + "%", style = TextStyle(
                                        color = Color(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.match
                                            )
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }

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
        hideFloatingButton: Boolean
    ) {
        showWelcomeDialog(
            title = "Welcome",
            text = "Greetings! We are pleased to offer you personalized movie recommendations based on your preferences. You may select up to 10 movies from our list or alternatively, use the search bar to explore and add more options. We look forward to providing you with a delightful movie-watching experience.",
            dialog = welcomedialog
        )
        showMoviesNotFoundDialog(dialog = movienotFounddialog)
        showErrorDialogBox(dialog = errorDialogBox)

        val listState = rememberLazyListState()
        Column {
            //App bar
            MainContent(appBarTitle = appBarTitle, isRecommended = isSearchBarEnabled)

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
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
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
                                                Log.d("myLog", "Query: " + textState.value.text)
                                                MainActivity().searchMovieAPICall(textState.value.text)
                                                textState.value = TextFieldValue("")
                                            }
                                    )
                                },
                                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() },
                                onSearch = {
                                    focus.clearFocus()
                                    Log.d("myLog", "Query: " + textState.value.text)
                                    MainActivity().searchMovieAPICall(textState.value.text)
                                    textState.value = TextFieldValue("")
                                })
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
                if (!hideFloatingButton) addFloatingButton(dialog)
                progressHUD()
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
                    } else
                        Toast.makeText(
                        context,
                        "Favourite movies list is empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }, containerColor = Color(ContextCompat.getColor(context, R.color.titleColor)),
                contentColor = Color.White
            ) {
                Text(text = "Recommend")
            }
        }
    }

    @Composable
    fun showWelcomeDialog(title: String, text: String, dialog: MutableState<Boolean>) {
        MaterialTheme {
            if (dialog.value) {
                AlertDialog(onDismissRequest = { dialog.value = false },
                    title = {
                        Text(text = title)
                    },
                    text = {
                        Text(text = text)
                    },
                    confirmButton = {},
                    dismissButton = {
                        Button(onClick = {
                            dialog.value = false
                        }) {
                            Text("Okay")
                        }
                    })
            }
        }
    }

    @Composable
    fun showMoviesNotFoundDialog(dialog: MutableState<Boolean>) {
        MaterialTheme {
            if (dialog.value) {
                AlertDialog(onDismissRequest = { dialog.value = false },
                    title = {
                        Text(text = "Apologies")
                    },
                    text = {
                        Text(text = "Movie not found")
                    },
                    confirmButton = {},
                    dismissButton = {
                        Button(onClick = {
                            dialog.value = false
                        }) {
                            Text("Okay")
                        }
                    })
            }
        }
    }

    @Composable
    fun showErrorDialogBox(dialog: MutableState<Boolean>) {
        MaterialTheme {
            if (dialog.value) {
                AlertDialog(onDismissRequest = { dialog.value = false },
                    title = {
                        Text(text = "Error")
                    },
                    text = {
                        Text(text = "Uh-oh :( Seems like internet is not working properly")
                    },
                    confirmButton = {},
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

    @Composable
    fun MainContent(appBarTitle: String, isRecommended: Boolean) {

        // Create a boolean variable
        // to store the display menu state
        var mDisplayMenu by remember { mutableStateOf(false) }

        // fetching local context
        val mContext = LocalContext.current

        // Creating a Top bar
        CenterAlignedTopAppBar(
            title = { Text(appBarTitle) },
            actions = {
                if (isRecommended) {
                    // Creating Icon button for dropdown menu
                    IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                        Icon(Icons.Default.MoreVert, "")
                    }

                    // Creating a dropdown menu
                    DropdownMenu(
                        expanded = mDisplayMenu,
                        onDismissRequest = { mDisplayMenu = false }
                    ) {

                        // Creating dropdown menu item, on click
                        // would create a Toast message
                        DropdownMenuItem(text = { Text(text = "Empty favourite list") },
                            onClick = {
                                selectedMovies.clear()
                                Toast.makeText(
                                    mContext,
                                    "Favourite list cleared",
                                    Toast.LENGTH_SHORT
                                ).show()
                                mDisplayMenu = false
                            })
                    }
                }
            })
    }
}

