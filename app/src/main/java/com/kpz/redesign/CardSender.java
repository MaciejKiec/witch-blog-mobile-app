package com.kpz.redesign;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;;


import java.io.File;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CardSender extends AsyncTask<Void, Void, JSONObject> {
    private static final String TAG = CardSender.class.getSimpleName();
    private final String directoryPath = "/detected_cards/";

    private static final String SERVER_URL = "https://cardrecognitionalgorithm.azurewebsites.net/process";
    private final ImageUploadCallback callback;
    private List<String> extractedCards;

    private String id;
    private final Context context;

    public interface ImageUploadCallback {
        void onUploadComplete(JSONObject response) throws JSONException;

        void onUploadFailure(String errorMessage);
    }

    public CardSender(ImageUploadCallback callback, List<String> extractedCards, String id, Context context) {
        this.callback = callback;
        this.extractedCards = extractedCards;
        this.id = id;
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        try {
            URL url = new URL(SERVER_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject requestJson = createRequestJson();
            connection.getOutputStream().write(requestJson.toString().getBytes());

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();
                return new JSONObject(response.toString());
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
    protected void onPostExecute(JSONObject response) {
        if (response != null) {
            try {
                callback.onUploadComplete(response);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            callback.onUploadFailure("Image upload failed");
        }
    }

    private JSONObject createRequestJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("number_of_cards", 3);
        json.put("image_1", encodeImageToBase64(extractedCards.get(0)));
        json.put("image_2", encodeImageToBase64(extractedCards.get(1)));
        json.put("image_3", encodeImageToBase64(extractedCards.get(2)));
        return json;
    }

    private String encodeImageToBase64(String imagePath) {
        // Replace this method with your own implementation to encode an image to Base64
        // Here's an example using BitmapFactory:
        File image = new File(context.getExternalFilesDir(directoryPath), "/" + imagePath);
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
