package com.sudhanshu.movierecd

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TensorFlowRecommendation(private val context: Context, private val input: Any) {

    fun performRecommendation(){
        //load model of tensorflow
        val buffer = loadModelFile(context.assets,"model.tflite")
        val tfLite = buffer?.let { Interpreter(it) }!!
        Log.d("myLog", "Model loaded")


    }
}

/** Google provided method to load the model from assets **/
@Throws(IOException::class)
fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer? {
    assetManager.openFd(modelPath).use { fileDescriptor ->
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
            )
        }
    }
}