package com.example.cs708_android;

import android.os.Environment;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class CommModule {
    private final static String SERVER_IP ="http://192.168.10.126:5000/";/* "http://10.119.133.118:5000/"; "http://192.168.0.140:5000/"*/

    public static void callTargetDetectionApi(String command,File sceneImageFile,String depth_image,String translate_matrix,String camera_matrix, MainActivity sourceActivity)
    {
       // String targetFileFullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/is708.jpg";
       // sourceActivity.displayMessage("Uploading " + targetFileFullPath);
       // File sceneImageFile = new File(targetFileFullPath);
        // Calculate and log ALL data sizes
        int image_size_kb = (int)(sceneImageFile.length() / 1024);
        int depth_size_kb = (depth_image != null) ? (depth_image.length() / 1024) : 0;
        int translate_size_kb = (translate_matrix != null) ? (translate_matrix.length() / 1024) : 0;
        int camera_size_kb = (camera_matrix != null) ? (camera_matrix.length() / 1024) : 0;
        int total_size_kb = image_size_kb + depth_size_kb + translate_size_kb + camera_size_kb;

        Log.d("CommModule", "Image file size: " + image_size_kb + " KB");
        Log.d("CommModule", "Depth data size: " + depth_size_kb + " KB");
        Log.d("CommModule", "Translation matrix size: " + translate_size_kb + " KB");
        Log.d("CommModule", "Camera matrix size: " + camera_size_kb + " KB");
        Log.d("CommModule", "Total data size: " + total_size_kb + " KB (" + (total_size_kb / 1024.0) + " MB)");

        // Check if we're likely to exceed typical limits
        if (total_size_kb > 15 * 1024) { // 15MB warning threshold
            Log.w("CommModule", "WARNING: Request size may exceed server limits!");
        }

        String completeUrl = SERVER_IP + "detect_target";
        Log.d("CommModule", "Calling target detection API on " + completeUrl);

        AndroidNetworking.upload(completeUrl)
                .setPriority(Priority.IMMEDIATE)
                .addMultipartFile("fileToUpload",sceneImageFile)
                .addMultipartParameter("depth_image", depth_image)
                .addMultipartParameter("translate_matrix", translate_matrix)
                .addMultipartParameter("camera_matrix", camera_matrix)
                .addMultipartParameter("command", command)
                .build()
                .setUploadProgressListener(new UploadProgressListener()
                {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes)
                    {
                        // do anything with progress
                       // Log.d("CommModule", (bytesUploaded) + " %");
                    }
                })
                .getAsString(new StringRequestListener()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("CommModule", "API response: " + response);
                        sourceActivity.selectTargetScreenArea(response);
                        sceneImageFile.delete();
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d("CommModule", "Error: " + error.getErrorDetail());
                        Log.d("CommModule", "onError errorCode : " + error.getErrorCode());
                        Log.d("CommModule", "onError errorBody : " + error.getErrorBody());

                        // get parsed error object (If ApiError is your class)

                        //     sourceActivity.displayMessage(error.getErrorDetail());
                    }

                });

       /* AndroidNetworking.get("http://172.17.161.118:5000/get_messages10")
        //AndroidNetworking.get("https://www.google.com")
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("NetworkTest", "Internet is working!"+response);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("NetworkTest", "No internet: " + anError.getErrorDetail());
                    }
                });

        */
        AndroidNetworking.cancel(completeUrl);
    }

}

//public class CommModule {
//    private final static String SERVER_IP = "http://192.168.10.126:5000/";
//
//    public static void callTargetDetectionApi(String command, File sceneImageFile, String depth_image,
//                                              String translate_matrix, String camera_matrix, MainActivity sourceActivity) {
//        int file_size = Integer.parseInt(String.valueOf(sceneImageFile.length()/1024));
//        Log.d("CommModule", "File size: " + file_size + " KB");
//        String completeUrl = SERVER_IP + "detect_target";
//
//        try {
//            // Create a temp file for depth data
//            File depthFile = new File(sourceActivity.getCacheDir(), "depth_data.txt");
//            FileWriter writer = new FileWriter(depthFile);
//            writer.write(depth_image);
//            writer.close();
//
//            // Log the size of depth data for debugging
//            Log.d("CommModule", "Depth data size: " + depthFile.length()/1024 + " KB");
//
//            // Configure network library with longer timeouts
//            OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                    .connectTimeout(120, TimeUnit.SECONDS)
//                    .writeTimeout(120, TimeUnit.SECONDS)
//                    .readTimeout(120, TimeUnit.SECONDS)
//                    .build();
//
//            AndroidNetworking.initialize(sourceActivity.getApplicationContext(), okHttpClient);
//
//            // Use the file instead of a string parameter
//            AndroidNetworking.upload(completeUrl)
//                    .setPriority(Priority.IMMEDIATE)
//                    .addMultipartFile("fileToUpload", sceneImageFile)
//                    .addMultipartFile("depth_file", depthFile)  // Send as file
//                    .addMultipartParameter("translate_matrix", translate_matrix)
//                    .addMultipartParameter("camera_matrix", camera_matrix)
//                    .addMultipartParameter("command", command)
//                    .build()
//                    .setUploadProgressListener(new UploadProgressListener() {
//                        @Override
//                        public void onProgress(long bytesUploaded, long totalBytes) {
//                            // Calculate percentage
//                            int percentage = (int) (100 * bytesUploaded / totalBytes);
//                            Log.d("CommModule", "Upload progress: " + percentage + "%");
//                        }
//                    })
//                    .getAsString(new StringRequestListener() {
//                        @Override
//                        public void onResponse(String response) {
//                            Log.d("CommModule", "API response: " + response);
//                            sourceActivity.selectTargetScreenArea(response);
//                            sceneImageFile.delete();
//                            depthFile.delete();  // Clean up temp file
//                        }
//
//                        @Override
//                        public void onError(ANError error) {
//                            Log.d("CommModule", "Error: " + error.getErrorDetail());
//                            Log.d("CommModule", "onError errorCode : " + error.getErrorCode());
//                            Log.d("CommModule", "onError errorBody : " + error.getErrorBody());
//
//                            // Clean up anyway
//                            depthFile.delete();
//                        }
//                    });
//
//        } catch (IOException e) {
//            Log.e("CommModule", "Error creating depth data file", e);
//        }
//    }
//}
