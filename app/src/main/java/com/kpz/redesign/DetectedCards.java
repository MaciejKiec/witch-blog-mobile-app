package com.kpz.redesign;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DetectedCards extends AppCompatActivity {


    private final String directoryPath = "/detected_cards/";
    private int currentImage = 0;
    private List<String> extractedCards;
    private ImageView myImage;
    private ImageButton nextButton;
    private ImageButton previousButton;
    private TextView textView, divinationMessage;
    public static JSONObject card_names;
    private int counter = 0;
    private List<String> loadCards(){
        List<String> detectedCards = new ArrayList<>();
        File directory = getExternalFilesDir(directoryPath);
        if(!directory.exists()) return detectedCards;

        File[] files = directory.listFiles();
        assert files != null;
        for(final File file: files){
            if(file.isFile()) detectedCards.add(file.getName());
        }

        return detectedCards;
    }

    private void initTextView() throws JSONException {
        textView.setText(card_names.getString("0"));
        divinationMessage.setText(card_names.getString("3"));
    }

    private void init(){
        File image = new File(getExternalFilesDir(directoryPath), "/" +extractedCards.get(currentImage));
        Bitmap myBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        myImage.setImageBitmap(myBitmap);
    }

    public void next(View view) throws JSONException {
        counter += 1;
        currentImage += 1;
        textView.setText(card_names.getString(Integer.toString(counter)));
        File image = new File(getExternalFilesDir(directoryPath), "/" +extractedCards.get(currentImage));
        Bitmap myBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        myImage.setImageBitmap(myBitmap);
        if(currentImage == extractedCards.size() - 1){
            nextButton.setEnabled(false);
        }
        if(currentImage == 1){
            previousButton.setEnabled(true);
        }
    }

    public void previous(View view) throws JSONException {
        counter -= 1;
        currentImage -= 1;
        textView.setText(card_names.getString(Integer.toString(counter)));
        File image = new File(getExternalFilesDir(directoryPath), "/" +extractedCards.get(currentImage));
        Bitmap myBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        myImage.setImageBitmap(myBitmap);
        if(currentImage == 0){
            previousButton.setEnabled(false);
        }
        if(currentImage == 1){
            nextButton.setEnabled(true);
        }
    }



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
        setContentView(R.layout.activity_detected_cards);
        textView = (TextView) findViewById(R.id.Response);
        myImage = (ImageView) findViewById(R.id.extractedCardView);
        previousButton = (ImageButton) findViewById(R.id.PrevButton);
        nextButton = (ImageButton) findViewById(R.id.NextButton);
        divinationMessage = findViewById(R.id.DivinationMessage);
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);
        extractedCards = loadCards();
        init();

        CardSender task = new CardSender(new CardSender.ImageUploadCallback() {
            @Override
            public void onUploadComplete(JSONObject response) {
                card_names = response;
                nextButton.setEnabled(true);
                previousButton.setEnabled(true);
                try {
                    initTextView();
                }catch (JSONException e){
                    textView.setText("JsonException occured!");
                }
            }

            @Override
            public void onUploadFailure(String errorMessage) {
                textView.setText("Problem z serwerem Azure!");
            }
        }, extractedCards, Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID), this);

        task.execute();
    }

    public void PopUP(View view) {
        int isOpened = divinationMessage.getVisibility();
        if(isOpened == View.INVISIBLE){
            divinationMessage.setVisibility(View.VISIBLE);
        }else{
            divinationMessage.setVisibility(View.INVISIBLE);
        }
    }
}