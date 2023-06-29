package com.kpz.redesign;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class Divination extends AppCompatActivity {


    private static final String SERVER_URL = "https://witchblog.azurewebsites.net/api/v1/divination/anonymous";
    private static final String TAG = Divination.class.getSimpleName();
    private TextView responseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_divination);
        List<String> temp = Arrays.asList("chuj","kurwa");
        responseTextView = findViewById(R.id.responseTextView);
        new CardDataTask().execute(temp);
    }


    private class CardDataTask extends AsyncTask<List<String>, Void, String> {

        @Override
        protected String doInBackground(List<String>... params) {
            try {

//                JSONObject cardJson = new JSONObject();
//                cardJson.put("nameOfCard", "The Lovers");
//                cardJson.put("isReversed", "false");

                URL url = new URL(SERVER_URL);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");

                connection.setDoOutput(true);

                String jsonPayload = "{\"nameOfCard\": \"King of Cups\"}";
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(jsonPayload);
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    Log.e(TAG, "Server response code: " + responseCode);
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error occurred during image upload", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            // Update the UI with the response
            responseTextView.setText(response);
        }
    }
}