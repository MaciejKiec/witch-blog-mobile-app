package com.kpz.redesign;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CardsExtractor {

    private static final String directoryPath = "/detected_cards/";

    private static Mat getMatFromBytes(String fileName, Activity activity){
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
        File image = new File(fileName);
        Log.e("test", fileName);
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Imgcodecs.imdecode(new MatOfByte(byteArray), -1);
    }

    public static void extractCardsFromImage(String fileName, Activity activity, int cardsInThePicture) {
        Mat img = getMatFromBytes(fileName, activity);
        //Core.rotate(img, img, Core.ROTATE_90_COUNTERCLOCKWISE);
        Mat original = img.clone();

        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        Mat blur = new Mat();
        Imgproc.GaussianBlur(gray, blur, new Size(5, 5), 0);

        Mat thresh = new Mat();
        Imgproc.threshold(blur, thresh, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7, 7));
        Mat dilate = new Mat();
        Imgproc.dilate(thresh, dilate, kernel, new Point(-1, -1), 1);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dilate, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        contours.sort(Comparator.comparingDouble(Imgproc::contourArea));
        List<MatOfPoint> largestContours = contours.subList(Math.max(contours.size() - cardsInThePicture, 0), contours.size());

        largestContours.sort((c1, c2) -> {
            Rect rect1 = Imgproc.boundingRect(c1);
            Rect rect2 = Imgproc.boundingRect(c2);
            return Integer.compare(rect1.x, rect2.x);
        });

        File directoryToClean = new File(activity.getExternalFilesDir(directoryPath).toString());
        String[] children = directoryToClean.list();
        for (String child : children) {
            new File(directoryToClean, child).delete();
        }

        File directory = new File(directoryPath);

        int imageNumber = 0;
        for (MatOfPoint contour : largestContours) {

            File file = new File(activity.getExternalFilesDir(directory.toString()),"/tarot_card_"+imageNumber+".jpg");
            String SavefileName = file.toString();

            Rect boundingRect = Imgproc.boundingRect(contour);
            Imgproc.rectangle(img, boundingRect.tl(), boundingRect.br(), new Scalar(36, 255, 12), 2);
            Mat roi = original.submat(boundingRect);
            Imgcodecs.imwrite(SavefileName, roi);
            imageNumber++;
        }
        if(!directory.exists()){
            directory.mkdirs();
        }
    }
}
