#ifndef _THRESHOLDER
#define _THRESHOLDER
#include <cv.h>
#include <android/log.h>

using namespace cv;

#define LOG_TAG "SNAPIT"

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

#define OTSU 100
#define NIBLACK 200
#define SAUVOLA 300 
#define WOLFJOLION 400

class BhThresholder
{
public :
    void doThreshold(InputArray src ,OutputArray dst,int method);
private:
};

#endif //_THRESHOLDER