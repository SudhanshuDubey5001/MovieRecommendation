package com.sudhanshu.movierecd.utils;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {

  private FileUtils() {}
  public static MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath)
      throws IOException {
    try (AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
         FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
      FileChannel fileChannel = inputStream.getChannel();
      long startOffset = fileDescriptor.getStartOffset();
      long declaredLength = fileDescriptor.getDeclaredLength();
      return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
  }
}
