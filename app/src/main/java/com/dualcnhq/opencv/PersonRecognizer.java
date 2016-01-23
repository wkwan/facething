package com.dualcnhq.opencv;

import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;

import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;

import android.graphics.Bitmap;
import android.util.Log;

public class PersonRecognizer {

    private static final int DEFAULT_WIDTH = 128;
    private static final int DEFAULT_HEIGHT = 128;
    private static final String LOGGER = "PersonRecognizer.class";

    private FaceRecognizer faceRecognizer;
    private String path;
    private ProfileManager profileManager;
    private int personNum = 0;
    private int confidenceLevel = 999;

    public PersonRecognizer(String path) {
        this.faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(2, 8, 8, 8, 200);
        this.path = path;
        this.profileManager = new ProfileManager(path);
    }


    // Adding picture for the person.
    void add(Mat mat, String description) {

        // Transforming mat into bitmap & scaling
        Bitmap bmp = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        bmp = Bitmap.createScaledBitmap(bmp, DEFAULT_WIDTH, DEFAULT_HEIGHT, false);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path + description + "-" + personNum + ".jpg", true);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
            this.personNum++;
        } catch (Exception e) {
            Log.e(LOGGER, e.getCause() + " " + e.getMessage());
        }
    }

    public boolean train() {
        // Filter to get png files
        FilenameFilter pngFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg");
            }
        };

        // Getting image files from the rootFile
        File rootFile = new File(path);
        File[] imageFiles = rootFile.listFiles(pngFilter);


        MatVector images = new MatVector(imageFiles.length);
        int[] labels = new int[imageFiles.length];
        int counter = 0;
        int rootFileLength = path.length();

        // Iterating through each files
        for (File image : imageFiles) {

            // Getting the image absolutePath & Loading image
            String absolutePath = image.getAbsolutePath();
            IplImage img = cvLoadImage(absolutePath);

            // Checking for errors
            if (img == null) {
                Log.e("Error", "Error cVLoadImage");
            } else {
                Log.i("image", absolutePath);
            }

            // Looking for the "personNum" from the file name
            int lastIndexOfDash = absolutePath.lastIndexOf("-");
            int lastIndexOfDot = absolutePath.lastIndexOf(".");
            int currentCount = Integer.parseInt(absolutePath.substring(lastIndexOfDash + 1, lastIndexOfDot));
            if (this.personNum < currentCount) {
                this.personNum++;
            }

            String description = absolutePath.substring(rootFileLength, lastIndexOfDash);
            if (profileManager.getProfileByName(description) == null) {
                profileManager.addProfile(description, profileManager.getMaxId() + 1, "TestingText");
            }

            int label = profileManager.getProfileByName(description).getId();
            IplImage grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);
            cvCvtColor(img, grayImg, CV_BGR2GRAY);
            images.put(counter, grayImg);
            labels[counter] = label;
            counter++;
        }

        if (counter > 0 && profileManager.getMaxId() > 1) {
            faceRecognizer.train(images, labels);
        }

        profileManager.processSavingProfileList();
        return true;
    }

    // Checking if the labelsFile is at least 2
    public boolean canPredict() {
        return (profileManager.getMaxId() > 1);
    }

    public String predict(Mat m) {
        //TODO - Change to Null later validation?
        if (!canPredict()) {
            return "";
        }

        // Predicting Begins
        int label[] = new int[1];
        double confidence[] = new double[1];
        IplImage ipl = MatToIplImage(m, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.faceRecognizer.predict(ipl, label, confidence);

        // Setting the confidence level
        if (label[0] != -1) {
            confidenceLevel = (int) confidence[0];
        } else {
            confidenceLevel = -1;
        }

        // Returning predicted label
        if (label[0] != -1) {
            return profileManager.getProfileById(label[0]).getName(); //TODO - Null Pointer Detection
        } else {
            return "Unkown";
        }
    }


    private IplImage MatToIplImage(Mat m, int width, int heigth) {
        Bitmap bmp = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        return BitmapToIplImage(bmp, width, heigth);
    }

    private IplImage BitmapToIplImage(Bitmap bmp, int width, int height) {
        if ((width != -1) || (height != -1)) {
            Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, width, height, false);
            bmp = bmp2;
        }

        IplImage image = IplImage.create(bmp.getWidth(), bmp.getHeight(),
                IPL_DEPTH_8U, 4);

        bmp.copyPixelsToBuffer(image.getByteBuffer());

        IplImage grayImg = IplImage.create(image.width(), image.height(),
                IPL_DEPTH_8U, 1);

        cvCvtColor(image, grayImg, opencv_imgproc.CV_BGR2GRAY);

        return grayImg;
    }

    public void load() {
        train();
    }

    public int getConfidenceLevel() {
        return this.confidenceLevel;
    }


//    protected void SaveBmp(Bitmap bmp, String path) {
//        FileOutputStream file;
//        try {
//            file = new FileOutputStream(path, true);
//
//            bmp.compress(Bitmap.CompressFormat.JPEG, 100, file);
//            file.close();
//        } catch (Exception e) {
//            Log.e("", e.getMessage() + e.getCause());
//            e.printStackTrace();
//        }
//    }
//    void changeRecognizer(int nRec) {
//        switch (nRec) {
//            case 0:
//                faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(1, 8, 8, 8, 100);
//                break;
//            case 1:
//                faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createFisherFaceRecognizer();
//                break;
//            case 2:
//                faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createEigenFaceRecognizer();
//                break;
//        }
//        train();
//    }


}
