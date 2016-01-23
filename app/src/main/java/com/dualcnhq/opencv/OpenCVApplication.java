package com.dualcnhq.opencv;

import android.app.Application;


public class OpenCVApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

    }

    static{ System.loadLibrary("opencv_java"); }
}
