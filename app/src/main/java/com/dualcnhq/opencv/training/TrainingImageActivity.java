package com.dualcnhq.opencv.training;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dualcnhq.opencv.ImageGallery;
import com.dualcnhq.opencv.MainActivity;
import com.dualcnhq.opencv.PersonRecognizer;
import com.dualcnhq.opencv.ProfileManager;
import com.dualcnhq.opencv.R;
import com.dualcnhq.opencv.Tutorial3View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TrainingImageActivity extends AppCompatActivity {

    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    private Tutorial3View tutorial3View;
    private ProfileManager profileManager;
    private CascadeClassifier mJavaDetector;
    private PersonRecognizer personRecognizer;
    private Bitmap mBitmap;
    private Mat mMat;
    private ImageView imageView;
    private Handler mHandler;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private int mDetectorType = JAVA_DETECTOR;
    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;

    private String name;
    private String twitter;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Setting Window
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_processing_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Getting profile values from intent
        Intent intent = getIntent();
        name = intent.getStringExtra(TrainingActivity.NAME_TAG);
        twitter = intent.getStringExtra(TrainingActivity.TWITTER_TAG);
        path = getFilesDir() + "/facerecogOCV/";

        imageView = (ImageView) findViewById(R.id.imgView);
        FloatingActionButton processPictureBtn = (FloatingActionButton) findViewById(R.id.processPictureBtn);
        processPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Initialize views
        profileManager = new ProfileManager(path);
        tutorial3View = (Tutorial3View) findViewById(R.id.tutorialView);
        tutorial3View.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                mGray = new Mat();
                mRgba = new Mat();
            }

            @Override
            public void onCameraViewStopped() {
                mGray.release();
                mRgba.release();
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                mRgba = inputFrame.rgba();
                mGray = inputFrame.gray();

                if (mAbsoluteFaceSize == 0) {
                    int height = mGray.rows();
                    if (Math.round(height * mRelativeFaceSize) > 0) {
                        mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                    }
                }

                MatOfRect faces = new MatOfRect();
                if (mDetectorType == JAVA_DETECTOR) {
                    if (mJavaDetector != null)
                        mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                                new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                }

                Rect[] facesArray = faces.toArray();
                if (facesArray.length == 1) {
                    Mat mat = new Mat();
                    Rect r = facesArray[0];

                    mat = mRgba.submat(r);
                    mBitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);

                    Message msg = new Message();
                    String textTochange = "IMG";
                    msg.obj = textTochange;
                    mHandler.sendMessage(msg);

                    Utils.matToBitmap(mat, mBitmap);
                    mMat = mat;
                }

                for (int i = 0; i < facesArray.length; i++) {
                    Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
                }

                return mRgba;
            }
        });

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj == "IMG") {
                    Canvas canvas = new Canvas();
                    canvas.setBitmap(mBitmap);
                    imageView.setImageBitmap(mBitmap);
                }
            }
        };

        boolean success = (new File(path)).mkdirs();
        if (!success) {
            //TODO - log the error.
        }
    }

    private void saveImage() {
        personRecognizer.add(mMat, name, twitter);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (tutorial3View != null)
            tutorial3View.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        tutorial3View.disableView();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {

                    personRecognizer = new PersonRecognizer(path);
                    personRecognizer.load();

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
//                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
//                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                            //                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                            cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
//                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    tutorial3View.enableView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
}
