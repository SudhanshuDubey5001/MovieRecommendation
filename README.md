Movie Recommendation Android Application

Built for demonstration purpose 

Usage - 
1. User will select movies from the trending/popular movies list at start of the page or search any movie/series from the search bar.
2. Select movies and add it to the favourite list (maximum = 10)
3. Press the "recommend" button to suggest movies based on selected movies features. 

Functioning -
- Developed using Kotlin as base language and Jetpack Compose library (Material3) for UI
- Currently using Google Tensor provided already trained CNN (Convolutional neural network encoder) model to provide recommendation. It is based on context encoding. 
- The application is also linked with Google Firebase analytics and Big Query which will be used to process the analytics data and obtain training data. Currently, I am still waiting on sufficient data following which I will use Firebase model management and ML API to create the TF lite mode.
- Once the TF lite mode is downloaded in our app (currently using Google provided), we need to provide the input array with IDs (and other movie features such as genre) and then obtain the output via TensorFlow tflite model.

Libraries and APIs used - 
1. Google TensorFlow
2. Retrofit with Rxjava2 
3. Jetpack Compose (Material3)
4. Gson
5. Glide (for images)
6. TMdb (movie database API for populating initial screen with recent/popular movies)
7. OMdb (movie database to search movies based on title and also to get recommendation results)

Update - 
Linked the project to Continuous Integration (CI) pipeline using Jenkins. 