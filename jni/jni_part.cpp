#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

#include "binthresholder.hpp"

using namespace std;
using namespace cv;


extern "C" {

JNIEXPORT void JNICALL Java_com_vision_snapit_main_SnapItActivity_thresholdATM(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);
JNIEXPORT void JNICALL Java_com_vision_snapit_main_SnapItActivity_thresholdATG(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);
JNIEXPORT void JNICALL Java_com_vision_snapit_main_SnapItActivity_thresholdOTSU(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);

JNIEXPORT void JNICALL Java_com_vision_snapit_main_SnapItActivity_thresholdATM(JNIEnv*, jobject, jlong addrGray, jlong addrBinaryATM)
{
    Mat& mGrayIn  = *(Mat*)addrGray;
    Mat& mInter = *(Mat*)addrBinaryATM;
    //gaussian blur
    GaussianBlur(mGrayIn, mGrayIn, Size(3, 3), 0.5, 0.5);
    adaptiveThreshold(mGrayIn,mInter,255,ADAPTIVE_THRESH_GAUSSIAN_C,THRESH_BINARY, 11, 2);
    LOGI("Completed thresholdATM method");
    LOGI("Mat Out details: rows %d cols %d channels is %d ",mInter.rows,mInter.cols,mInter.channels());
}


JNIEXPORT void JNICALL Java_com_vision_snapit_main_SnapItActivity_thresholdATG(JNIEnv*, jobject, jlong addrGray, jlong addrInter)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mInter = *(Mat*)addrInter;
    //gaussian blur
    //GaussianBlur(mGr, mGr, Size(3, 3), 0.5, 0.5);
    adaptiveThreshold(mGr,mInter,255,ADAPTIVE_THRESH_MEAN_C,THRESH_BINARY, 11, 2);
    LOGI("Completed thresholdATG method");
    LOGI("Mat Out details: rows %d cols %d channels is %d ",mInter.rows,mInter.cols,mInter.channels());
}

JNIEXPORT void JNICALL Java_com_vision_snapit_main_SnapItActivity_thresholdOTSU(JNIEnv*, jobject, jlong addrGray, jlong addrInter)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mInter = *(Mat*)addrInter;
    Mat closed;

    LOGI("Using ThresholdOTSU method");
    LOGI("Mat details: rows %d cols %d channels %d",mGr.rows,mGr.cols,mGr.channels());

    //Convert
    mGr.convertTo( mGr, CV_8U );

#if 1
    //gaussian blur
    //GaussianBlur(mGr, mGr, Size(3, 3), 0.5, 0.5);
    // adaptative thresholding using Otsu's method,
    //threshold( mGr, mInter, 0, 255, THRESH_BINARY | THRESH_OTSU );
    BhThresholder binthr;
    binthr.doThreshold(mGr,mInter,WOLFJOLION);
#else

#if 1

#else
    int N = 19;
    Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(N, N));
    morphologyEx(mGr,closed,MORPH_CLOSE,kernel);
    mGr.convertTo(mGr, CV_32F); // divide requires floating-point
    divide(mGr, closed, mGr, 1, CV_32F);
    normalize(mGr, mGr, 0, 255, NORM_MINMAX);
    mGr.convertTo( mGr, CV_8U ); // convert back to unsigned int
#endif

    int blockSide = 50;
    Scalar m,s;

    for(int i=0;i<mGr.rows;i+=blockSide)
    {
           for (int j=0;j<mGr.cols;j+=blockSide)
           {
        	   int patchYsize = MIN(i+blockSide+1,mGr.rows);
        	   int patchXsize = MIN(j+blockSide+1,mGr.cols);

               Mat patch=mGr(Range(i,patchYsize),Range(j,patchXsize));
               Mat patchOut=mInter(Range(i,patchYsize),Range(j,patchXsize));

               meanStdDev(patch,m,s);
               LOGI("%f standard deviation is and mean is %f",s[0],m[0]);
               // Thresholding parameter (set smaller for lower contrast image)
				if(s[0]>15)
				{
				  LOGI("Patch rows %d cols %d. patchOut rows %d patchOut cols %d ", patch.rows,patch.cols,patchOut.rows,patchOut.cols);
				   //gaussian blur
				   GaussianBlur(patch, patch, Size(5, 5), 0);
				   // adaptative thresholding using Otsu's method,
				   threshold( patch, patchOut, 0, 255, THRESH_BINARY | THRESH_OTSU );
				}
				else
				{
				    adaptiveThreshold(patch,patchOut,255,ADAPTIVE_THRESH_MEAN_C,THRESH_BINARY, 3, 0);
				}

           }
    }
#endif
    LOGI("Completed ThresholdOTSU method");
    LOGI("Mat Out details: rows %d cols %d channels is %d ",mInter.rows,mInter.cols,mInter.channels());
}
}
