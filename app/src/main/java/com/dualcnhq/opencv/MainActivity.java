package com.dualcnhq.opencv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dualcnhq.opencv.training.TrainingActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

//import java.io.FileNotFoundException;


public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {

    String nameToTweet = "";
    private static final String TAG = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;

    public static final int TRAINING = 0;
    public static final int SEARCHING = 1;
    public static final int IDLE = 2;

    private static final int frontCam = 1;
    private static final int backCam = 2;


    private int faceState = IDLE;
//    private int countTrain=0;

    //    private MenuItem               mItemFace50;
//    private MenuItem               mItemFace40;
//    private MenuItem               mItemFace30;
//    private MenuItem               mItemFace20;
//    private MenuItem               mItemType;
//
    private MenuItem nBackCam;
    private MenuItem mFrontCam;
    private MenuItem mEigen;


    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    //   private DetectionBasedTracker  mNativeDetector;

    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private int mLikely = 999;

    String mPath = "";

    private Tutorial3View mOpenCvCameraView;
    private int mChooseCamera = backCam;
    private String tweetID;

    EditText text;
    TextView textresult;
    private ImageView Iv;
    Bitmap mBitmap;
    Handler mHandler;

    PersonRecognizer fr;
    ToggleButton toggleButtonGrabar, toggleButtonTrain, buttonSearch;
    Button buttonCatalog;
    ImageView ivGreen, ivYellow, ivRed;
    ImageButton imCamera;

    TextView textState;
    com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;


    static final long MAXIMG = 10;

    ArrayList<Mat> alimgs = new ArrayList<Mat>();

    int[] labels = new int[(int) MAXIMG];
    int countImages = 0;

    ProfileManager profileManager;

    TextView userInfo;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //   System.loadLibrary("detection_based_tracker");


                    fr = new PersonRecognizer(mPath);
                    String s = getResources().getString(R.string.Straininig);
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                    fr.load();

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
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        //                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton getUserInfoButton = (FloatingActionButton) findViewById(R.id.getUserInfoButton);
        getUserInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO - Getting the Twitter Information
                String[] feedData = getTweetData();
                if (feedData == null) {
                    //TODO - Log Message
                    Log.e("Tweeter", "Were not able to fetch tweet information");
                }
                Intent i = new Intent(getApplicationContext(), UserInfo.class);
                i.putExtra(UserInfo.TWITTER_FEED_ARRAY, feedData);

                startActivity(i);
            }


        });

        FloatingActionButton trainButton = (FloatingActionButton) findViewById(R.id.training_button);
        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TrainingActivity.class);
                startActivity(intent);
            }
        });

        userInfo = (TextView) findViewById(R.id.user_info);

        FloatingActionButton twitterButton = (FloatingActionButton) findViewById(R.id.twitter);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!tweetID.isEmpty()) {
                    Log.i("qqqqq", "clicking the twitter button");

                    ConfigurationBuilder builder = new ConfigurationBuilder();
                    builder.setOAuthConsumerKey(getString(R.string.twitter_consumer_key));
                    builder.setOAuthConsumerSecret(getString(R.string.twitter_consumer_secret));

                    SharedPreferences sharedPreferences = getSharedPreferences(TwitterMainActivity.PREF_NAME, 0);

                    String access_token = sharedPreferences.getString(TwitterMainActivity.PREF_KEY_OAUTH_TOKEN, "");
                    String acces_token_secret = sharedPreferences.getString(TwitterMainActivity.PREF_KEY_OAUTH_SECRET, "");

                    AccessToken accessToken = new AccessToken(access_token, acces_token_secret);

                    Log.i("qqq", getString(R.string.twitter_consumer_key) + " " + getString(R.string.twitter_consumer_secret) + " " + sharedPreferences.getString(TwitterMainActivity.PREF_KEY_OAUTH_TOKEN, "") + " " + sharedPreferences.getString(TwitterMainActivity.PREF_KEY_OAUTH_SECRET, ""));


                    Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);


                    StatusUpdate statusUpdate = new StatusUpdate("I saw " + " " + nameToTweet + " on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + " !");
                    InputStream is = getResources().openRawResource(+R.mipmap.landscape);


                    try {
                        Log.i("qqq", "try to update status " + tweetID);

                        twitter.createFriendship(tweetID);
//                        Log.i("qqq", "after update status " + response.getText());
                        Toast.makeText(MainActivity.this, "Followed " + tweetID, Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Log.i("qqq", e.getMessage());

                    }

                }


            }

        });

        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.faceView);
        mOpenCvCameraView.setCvCameraViewListener(this);


        mPath = getFilesDir() + "/facerecogOCV/";

        profileManager = new ProfileManager(mPath);

        Iv = (ImageView) findViewById(R.id.imageView1);
        textresult = (TextView) findViewById(R.id.textView1);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj == "IMG") {

                } else {
                    if (mLikely < 30) {
                        Log.d("Main", "Name: unknown");
                        userInfo.setText("User: unknown");
                    } else if (msg.obj != null && !msg.obj.toString().isEmpty()) {
                        nameToTweet = msg.obj.toString();
                        Log.d("Main", "Name: " + msg.obj.toString());
                        userInfo.setText("Name: " + msg.obj.toString());
                    }
                }
            }
        };

        faceState = SEARCHING;

        boolean success = (new File(mPath)).mkdirs();
        if (!success) {
            Log.e("Error", "Error creating directory");
        }
    }

    private String[] getTweetData() {
        if (!nameToTweet.isEmpty()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(getString(R.string.twitter_consumer_key));
            builder.setOAuthConsumerSecret(getString(R.string.twitter_consumer_secret));

            SharedPreferences sharedPreferences = getSharedPreferences(TwitterMainActivity.PREF_NAME, 0);

            String access_token = sharedPreferences.getString(TwitterMainActivity.PREF_KEY_OAUTH_TOKEN, "");
            String acces_token_secret = sharedPreferences.getString(TwitterMainActivity.PREF_KEY_OAUTH_SECRET, "");

            AccessToken accessToken = new AccessToken(access_token, acces_token_secret);

            Log.i("qqq", getString(R.string.twitter_consumer_key) + " " + getString(R.string.twitter_consumer_secret) + " " + sharedPreferences.getString(TwitterMainActivity.PREF_KEY_OAUTH_TOKEN, "") + " " + sharedPreferences.getString(TwitterMainActivity.PREF_KEY_OAUTH_SECRET, ""));


            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

            //First param of Paging() is the page number, second is the number per page (this is capped around 200 I think.
            Paging paging = new Paging(1, 5);
            try {
                List<Status> statuses = twitter.getUserTimeline(tweetID, paging);
                String[] twitList = new String[statuses.size()];
                for (int i = 0; i < statuses.size(); i++) {
                    twitList[i] = statuses.get(i).getText();
                }

                return twitList;

            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);


    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        } else if (mDetectorType == NATIVE_DETECTOR) {
//            if (mNativeDetector != null)
//                mNativeDetector.detect(mGray, faces);
        } else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();

        if ((facesArray.length == 1) && (faceState == TRAINING) && (countImages < MAXIMG) && (!text.getText().toString().isEmpty())) {


            Mat m = new Mat();
            Rect r = facesArray[0];


            m = mRgba.submat(r);
            mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


            Utils.matToBitmap(m, mBitmap);
            // SaveBmp(mBitmap,"/sdcard/db/I("+countTrain+")"+countImages+".jpg");

            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            mHandler.sendMessage(msg);
            if (countImages < MAXIMG) {
                fr.add(m, text.getText().toString(), tweetID);
                countImages++;
            }

        } else if ((faceState == SEARCHING)) {
            if (facesArray.length > 0) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//stuff that updates ui

                    }
                });

                Mat m = new Mat();

                m = mGray.submat(facesArray[0]);
                mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


                Utils.matToBitmap(m, mBitmap);
                Message msg = new Message();
                String textTochange = "IMG";
                msg.obj = textTochange;
                mHandler.sendMessage(msg);

//                textTochange = fr.predict(m);
                Profile profile = fr.getPredictedProfile(m);
                if (profile != null) {
                    textTochange = profile.getName();
                    tweetID = profile.getTwitterID();
                    Log.i("qqqq", "getting tweetid " + tweetID);
                }

                mLikely = fr.getConfidenceLevel();
                msg = new Message();
                msg.obj = textTochange;
                mHandler.sendMessage(msg);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//stuff that updates ui

                    }
                });
            }


        }
        for (int i = 0; i < facesArray.length; i++)

            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        if (mOpenCvCameraView.numberCameras() > 1) {
            nBackCam = menu.add(getResources().getString(R.string.SFrontCamera));
            mFrontCam = menu.add(getResources().getString(R.string.SBackCamera));
//        mEigen = menu.add("EigenFaces");
//        mLBPH.setChecked(true);
        } else {
            imCamera.setVisibility(View.INVISIBLE);

        }
        //mOpenCvCameraView.setAutofocus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        nBackCam.setChecked(false);
        mFrontCam.setChecked(false);
        //  mEigen.setChecked(false);
        if (item == nBackCam) {
            mOpenCvCameraView.setCamFront();
            mChooseCamera = frontCam;
        }
        //fr.changeRecognizer(0);
        else if (item == mFrontCam) {
            mChooseCamera = backCam;
            mOpenCvCameraView.setCamBack();

        }

        item.setChecked(true);

        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }
}
