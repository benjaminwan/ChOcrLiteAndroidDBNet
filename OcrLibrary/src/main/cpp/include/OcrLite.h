#ifndef __OCR_LITE_H__
#define __OCR_LITE_H__

#include "opencv2/core.hpp"
#include "ncnn/net.h"
#include "OcrUtil.h"

using namespace std;

struct TextBox {
    std::vector<cv::Point> box;
    float score;

    TextBox(std::vector<cv::Point> box,
            float score) : box(box), score(score) {};
};

struct Angle {
    int index;
    float score;

    Angle(int index,
          float score
    ) : index(index),
        score(score) {};
};

struct TextLine {
    std::string line;
    std::vector<float> scores;

    TextLine(std::string line,
             std::vector<float> scores) : line(line), scores(scores) {};
};

class OcrLite {
public:
    OcrLite(JNIEnv *env, jobject assetManager);

    std::string detect(cv::Mat &src, ScaleParam &scale, cv::Mat &imgBox);

private:
    std::vector<TextBox> getTextBoxes(cv::Mat &partImg, ScaleParam &s);

    Angle getAngle(cv::Mat &src);

    TextLine getTextLine(cv::Mat &src, int angleIndex);

    TextLine scoreToString(ncnn::Mat &score);

    ncnn::Extractor getExtractor(int angleIndex);

    ncnn::Net dbNet, angleNet, crnnNet, crnnVerticalNet;
    int numThread = 4;

    const float unClipRatio = 1.5;
    const float boxThresh = 0.5;
    const float thresh = 0.3;
    const int minSize = 3;
    const float meanValsDBNet[3] = {0.485 * 255, 0.456 * 255, 0.406 * 255};
    const float normValsDBNet[3] = {1.0 / 0.229 / 255.0, 1.0 / 0.224 / 255.0, 1.0 / 0.225 / 255.0};

    int angleDstWidth = 196;
    int angleDstHeight = 32;
    const float meanValsAngle[3] = {0.485 * 255, 0.456 * 255, 0.406 * 255};
    const float normValsAngle[3] = {1.0 / 0.229 / 255.0, 1.0 / 0.224 / 255.0, 1.0 / 0.225 / 255.0};

    int crnnDstHeight = 32;
    const float meanValsCrnn[1] = {127.5};
    const float normValsCrnn[1] = {1.0 / 127.5};

    std::vector<std::string> keys;
};


#endif