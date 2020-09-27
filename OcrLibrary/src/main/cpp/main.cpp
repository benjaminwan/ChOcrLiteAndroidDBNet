#include "ncnn/net.h"
#include "Logger.h"
#include "BitmapUtils.h"
#include "OcrLite.h"
#include "OcrUtil.h"

static OcrLite *ocrLite;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_benjaminwan_ocrlibrary_OcrEngine_init(JNIEnv *env, jobject thiz, jobject assetManager) {

    ocrLite = new OcrLite(env, assetManager);
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_benjaminwan_ocrlibrary_OcrEngine_detectResize(JNIEnv *env, jobject thiz, jobject input,
                                                       jint reSize, jobject output) {
    cv::Mat imgRGBA, imgBGR, imgOut;
    bitmapToMat(env, input, imgRGBA);
    cv::cvtColor(imgRGBA, imgBGR, cv::COLOR_RGBA2BGR);

    //按比例缩小图像，减少文字分割时间
    ScaleParam s = getScaleParam(imgRGBA, reSize);//例：按长或宽缩放 src.cols=不缩放，src.cols/2=长度缩小一半

    cv::Mat imgBox = imgBGR.clone();
    std::string outStr = ocrLite->detect(imgBGR, s, imgBox);

    cv::cvtColor(imgBox, imgOut, cv::COLOR_BGR2RGBA);
    matToBitmap(env, imgOut, output);

    return env->NewStringUTF(outStr.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_benjaminwan_ocrlibrary_OcrEngine_detectScale(JNIEnv *env, jobject thiz, jobject input,
                                                      jfloat scale, jobject output) {
    cv::Mat imgRGBA, imgBGR, imgOut;
    bitmapToMat(env, input, imgRGBA);
    cv::cvtColor(imgRGBA, imgBGR, cv::COLOR_RGBA2BGR);

    //按比例缩小图像，减少文字分割时间
    ScaleParam s = getScaleParam(imgBGR, scale);//例：按比例缩放 1.0f=不缩放，0.5f=按比例缩小50%

    cv::Mat imgBox = imgBGR.clone();
    std::string outStr = ocrLite->detect(imgBGR, s, imgBox);

    cv::cvtColor(imgBox, imgOut, cv::COLOR_BGR2RGBA);
    matToBitmap(env, imgOut, output);

    return env->NewStringUTF(outStr.c_str());

}