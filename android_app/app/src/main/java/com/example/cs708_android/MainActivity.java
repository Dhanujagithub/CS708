package com.example.cs708_android;

import static android.content.ContentValues.TAG;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import  com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.error.ANError;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.androidnetworking.AndroidNetworking;
import com.example.cs708_android.databinding.ActivityMainBinding;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private TextInputEditText command;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        String commandText= "chair";
        ImageView imageView = findViewById(R.id.imageView);
        String env_instance = "2";
        try {
            // Load image from assets folder
            InputStream inputStream = getAssets().open("data/image/depthimage"+env_instance+".png");
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            // Rotate the bitmap by 90 degrees
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            imageView.setImageBitmap(rotatedBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Display a toast message
        InputStream inputStream = null;
        InputStream inputStream_depth = null;
        InputStream inputStream_intrinsic = null;
        InputStream inputStream_trans = null;
        String depth_image="";
        String camera_matrix="";
        String translate_matrix="";
        try {
            inputStream = getAssets().open("data/image/depthimage"+env_instance+".png");
            File scene_image = inputStreamToFile(inputStream, "depthimage"+env_instance+".png");

            inputStream_depth = getAssets().open("data/depth/depth"+env_instance+".txt");
            depth_image = readAssetFile(inputStream_depth);

            inputStream_intrinsic = getAssets().open("data/intric_matrix/matrix"+env_instance+".txt");
            camera_matrix = readAssetFile(inputStream_intrinsic);

            inputStream_trans = getAssets().open("data/trans_matrix/matrix"+env_instance+".txt");
            translate_matrix = readAssetFile(inputStream_trans);

            //depth_image = readAssetFile(inputStream_depth);
            Log.d("depth image", "lenth: " + depth_image.length());
            com.example.cs708_android.CommModule.callTargetDetectionApi(commandText,scene_image,depth_image,translate_matrix,camera_matrix,MainActivity.this);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Button Send_data = findViewById(R.id.button);
        //implement the function to send data to the server
        Send_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button Reset = findViewById(R.id.button2);
        //implement the function to reset bbox and width display from the current screen
        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button Previous = findViewById(R.id.btnPrev);
        //implement the function to go to previous scene environment
        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button Next = findViewById(R.id.btnNext);
        //implement the function to go to next scene environment
        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        command = findViewById(R.id.input);

        // Example: Get the text from the input field
        String widthText = command.getText().toString();
    }

    public void selectTargetScreenArea(String response){
        //overlay bounding box of target object and display width on the current image and display in the screen

    }

    private File inputStreamToFile(InputStream inputStream, String fileName) throws IOException {
        // Create a temporary file in the cache directory
        File file = new File(getCacheDir(), fileName);
        FileOutputStream outputStream = new FileOutputStream(file);

        // Write input stream to file
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        // Close streams
        outputStream.close();
        inputStream.close();

        return file;
    }

    private String readAssetFile(InputStream inputStream) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {

            Log.d("load depth", "Error: " + e);
        }
        return content.toString();
    }



}

