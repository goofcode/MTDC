#include <jni.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_kr_ac_cau_goofcode_mtdc_MainActivity_ConvertRGBtoGray(
        JNIEnv *env, jobject instance, jlong matAddrInput, jlong matAddrResult) {

    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat*)matAddrResult;

    cvtColor(matInput, matResult, CV_RGBA2GRAY);
}


