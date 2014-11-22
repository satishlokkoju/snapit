package com.vision.snapit.main;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class SnapItActivity extends Activity implements CvCameraViewListener2
{
    private static final String  TAG                    = "SNAPIT::Activity";

    private static final int     VIEW_MODE_RGBA         = 0;
    private static final int     VIEW_MODE_ADAPTHRMEAN  = 1;
    private static final int     VIEW_MODE_ADAPTHRGAUSS = 2;
    private static final int     VIEW_MODE_OSTUBINARY   = 5;

    private int                  mViewMode;
    private Mat                  mRgba;
    private Mat                  mIntermediateMat;
    private Mat                  mGray;

    private MenuItem             mItemPreviewRGBA;
    private MenuItem             mItemPreviewATM;
    private MenuItem             mItemPreviewATG;
    private MenuItem             mItemPreviewOTSU;

    private CameraBridgeViewBase mOpenCvCameraView;

    public SnapItActivity()
    {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    static
    {
        if (!OpenCVLoader.initDebug())
        {
            // Handle initialization error
        }
        else
        {
            System.loadLibrary("mixed_sample");
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.snapit_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Preview Original");
        mItemPreviewATM = menu.add("Adaptive Threshold Mean");
        mItemPreviewATG = menu.add("Adaptive Threshold Gaussian");
        mItemPreviewOTSU = menu.add("OTSU Binary");
        return true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mOpenCvCameraView.enableView();
        // OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
        // mLoaderCallback);
    }

    public void onDestroy()
    {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height)
    {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped()
    {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame)
    {
        final int viewMode = mViewMode;
        switch (viewMode)
        {
            case VIEW_MODE_ADAPTHRMEAN:
                // input frame has gray scale format
                mIntermediateMat = inputFrame.gray(); 
                mGray = inputFrame.gray();
                mRgba = inputFrame.rgba();
                
                thresholdATM(mGray.getNativeObjAddr(), mIntermediateMat.getNativeObjAddr());
                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                
                break;
            case VIEW_MODE_RGBA:
                // input frame has RBGA format
                mRgba = inputFrame.rgba();
                break;
            case VIEW_MODE_ADAPTHRGAUSS:
                // input frame has gray scale format
                //mRgba = inputFrame.rgba();
                //Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
                //Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                mIntermediateMat = inputFrame.gray(); 
                mGray = inputFrame.gray();
                mRgba = inputFrame.rgba();
                
                thresholdATG(mGray.getNativeObjAddr(), mIntermediateMat.getNativeObjAddr());
                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_OSTUBINARY:
                // input frame has RGBA format
                mIntermediateMat = inputFrame.gray(); 
                mGray = inputFrame.gray();
                mRgba = inputFrame.rgba();
                
                thresholdOTSU(mGray.getNativeObjAddr(), mIntermediateMat.getNativeObjAddr());
                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
        }

        return mRgba;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemPreviewRGBA)
        {
            mViewMode = VIEW_MODE_RGBA;
        }
        else if (item == mItemPreviewATG)
        {
            mViewMode = VIEW_MODE_ADAPTHRGAUSS;
        }
        else if (item == mItemPreviewATM)
        {
            mViewMode = VIEW_MODE_ADAPTHRMEAN;
        }
        else if (item == mItemPreviewOTSU)
        {
            mViewMode = VIEW_MODE_OSTUBINARY;
        }

        return true;
    }

    public native void thresholdATM(long matAddrGr, long matAddrRgba);
    public native void thresholdATG(long matAddrGr, long matAddrRgba);
    public native void thresholdOTSU(long matAddrGr, long matAddrRgba);
}
