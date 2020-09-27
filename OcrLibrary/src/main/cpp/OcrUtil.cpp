#include "OcrUtil.h"
#include "clipper.hpp"
#include "RRLib.h"

ScaleParam getScaleParam(cv::Mat &src, const float scale) {
    int srcWidth = src.cols;
    int srcHeight = src.rows;
    int dstWidth = int((float) srcWidth * scale);
    int dstHeight = int((float) srcHeight * scale);
    if (dstWidth % 32 != 0) {
        dstWidth = (dstWidth / 32 - 1) * 32;
        dstWidth = std::max(dstWidth, 32);
    }
    if (dstHeight % 32 != 0) {
        dstHeight = (dstHeight / 32 - 1) * 32;
        dstHeight = std::max(dstHeight, 32);
    }
    float scaleWidth = (float) dstWidth / (float) srcWidth;
    float scaleHeight = (float) dstHeight / (float) srcHeight;
    return {srcWidth, srcHeight, dstWidth, dstHeight, scaleWidth, scaleHeight};
}

ScaleParam getScaleParam(cv::Mat &src, const int targetSize) {

    int srcWidth, srcHeight, dstWidth, dstHeight;
    srcWidth = dstWidth = src.cols;
    srcHeight = dstHeight = src.rows;

    float scale = 1.f;
    if (dstWidth > dstHeight) {
        scale = (float) targetSize / (float) dstWidth;
        dstWidth = targetSize;
        dstHeight = int((float) dstHeight * scale);
    } else {
        scale = (float) targetSize / (float) dstHeight;
        dstHeight = targetSize;
        dstWidth = int((float) dstWidth * scale);
    }
    if (dstWidth % 32 != 0) {
        dstWidth = (dstWidth / 32 - 1) * 32;
        dstWidth = std::max(dstWidth, 32);
    }
    if (dstHeight % 32 != 0) {
        dstHeight = (dstHeight / 32 - 1) * 32;
        dstHeight = std::max(dstHeight, 32);
    }
    float scaleWidth = (float) dstWidth / (float) srcWidth;
    float scaleHeight = (float) dstHeight / (float) srcHeight;
    return {srcWidth, srcHeight, dstWidth, dstHeight, scaleWidth, scaleHeight};
}

cv::RotatedRect getPartRectPlus(std::vector<cv::Point> &box, float scale) {
    cv::RotatedRect rect = cv::minAreaRect(box);
    int minSize = rect.size.width > rect.size.height ? rect.size.height : rect.size.width;
    rect.size.width = rect.size.width + (float) minSize * scale;
    rect.size.height = rect.size.height + (float) minSize * scale;
    return rect;
}

cv::RotatedRect getPartRectMinus(std::vector<cv::Point> &box, float scale) {
    cv::RotatedRect rect = cv::minAreaRect(box);
    int minSize = rect.size.width > rect.size.height ? rect.size.height : rect.size.width;
    //rect.size.width = rect.size.width - (float) minSize * scale;
    //rect.size.height = rect.size.height - (float) minSize * scale;
    if (rect.size.width > rect.size.height) {
        rect.size.width = rect.size.width - (float) minSize * (scale + 0.1);
        rect.size.height = rect.size.height - (float) minSize * (scale - 0.1);
    } else {
        rect.size.width = rect.size.width - (float) minSize * (scale - 0.1);
        rect.size.height = rect.size.height - (float) minSize * (scale + 0.1);
    }
    return rect;
}

void drawTextBox(cv::Mat &boxImg, cv::RotatedRect &rect) {
    cv::Point2f vertices[4];      //定义4个点的数组
    rect.points(vertices);   //将四个点存储到vertices数组中
    for (int i = 0; i < 4; i++)
        cv::line(boxImg, vertices[i], vertices[(i + 1) % 4], cv::Scalar(0, 0, 255), 2);
    //cv::polylines(srcmat, textpoint, true, cv::Scalar(0, 255, 0), 2);
}

cv::Mat matRotateClockWise180(cv::Mat src) {//顺时针180
    //0: 沿X轴翻转； >0: 沿Y轴翻转； <0: 沿X轴和Y轴翻转
    flip(src, src,
         0);// 翻转模式，flipCode == 0垂直翻转（沿X轴翻转），flipCode>0水平翻转（沿Y轴翻转），flipCode<0水平垂直翻转（先沿X轴翻转，再沿Y轴翻转，等价于旋转180°）
    flip(src, src, 1);
    return src;
    //transpose(src, src);// 矩阵转置
}

cv::Mat matRotateClockWise90(cv::Mat src) {
    // 矩阵转置
    transpose(src, src);
    //0: 沿X轴翻转； >0: 沿Y轴翻转； <0: 沿X轴和Y轴翻转
    flip(src, src,
         1);// 翻转模式，flipCode == 0垂直翻转（沿X轴翻转），flipCode>0水平翻转（沿Y轴翻转），flipCode<0水平垂直翻转（先沿X轴翻转，再沿Y轴翻转，等价于旋转180°）
    return src;
}

bool cvPointCompare(cv::Point a, cv::Point b) {
    return a.x < b.x;
}

int
getMiniBoxes(std::vector<cv::Point> &invec, std::vector<cv::Point> &minboxvec, float &minedgesize,
             float &alledgesize) {
    cv::RotatedRect textrect = cv::minAreaRect(invec);
    cv::Mat boxPoints2f;
    cv::boxPoints(textrect, boxPoints2f);

    float *p1 = (float *) boxPoints2f.data;
    std::vector<cv::Point> tmpvev;
    for (int i = 0; i < 4; ++i, p1 += 2) {
        tmpvev.emplace_back((int) (p1[0]), (int) (p1[1]));
    }

    std::sort(tmpvev.begin(), tmpvev.end(), cvPointCompare);

    minboxvec.clear();

    int index1 = 0, index2 = 0, index3 = 0, index4 = 0;
    if (tmpvev[1].y > tmpvev[0].y) {
        index1 = 0;
        index4 = 1;
    } else {
        index1 = 1;
        index4 = 0;
    }

    if (tmpvev[3].y > tmpvev[2].y) {
        index2 = 2;
        index3 = 3;
    } else {
        index2 = 3;
        index3 = 2;
    }

    minboxvec.clear();

    minboxvec.push_back(tmpvev[index1]);
    minboxvec.push_back(tmpvev[index2]);
    minboxvec.push_back(tmpvev[index3]);
    minboxvec.push_back(tmpvev[index4]);

    minedgesize = (std::min)(textrect.size.width, textrect.size.height);
    alledgesize = 2.f * (textrect.size.width + textrect.size.height);


    return 1;
}

float boxScoreFast(cv::Mat &mapmat, std::vector<cv::Point> &_box) {
    std::vector<cv::Point> box = _box;
    int wid = mapmat.cols;
    int hi = mapmat.rows;
    int xmax = -1, xmin = 1000000, ymax = -1, ymin = 1000000;
    for (int i = 0; i < box.size(); ++i) {
        if (xmax < box[i].x)
            xmax = box[i].x;
        if (xmin > box[i].x)
            xmin = box[i].x;
        if (ymax < box[i].y)
            ymax = box[i].y;
        if (ymin > box[i].y)
            ymin = box[i].y;
    }
    xmax = (std::min)((std::max)(xmax, 0), wid - 1);
    xmin = (std::max)((std::min)(xmin, wid - 1), 0);
    ymax = (std::min)((std::max)(ymax, 0), hi - 1);
    ymin = (std::max)((std::min)(ymin, hi - 1), 0);

    for (int i = 0; i < box.size(); ++i) {
        box[i].x = box[i].x - xmin;
        box[i].y = box[i].y - ymin;
    }

    std::vector<std::vector<cv::Point>> tmpbox;
    tmpbox.push_back(box);
    cv::Mat maskmat(ymax - ymin + 1, xmax - xmin + 1, CV_8UC1, cv::Scalar(0, 0, 0));
    cv::fillPoly(maskmat, tmpbox, cv::Scalar(1, 1, 1), 1);

    // 	cv::Mat normat;
    // 	cv::normalize(maskmat, normat, 255, 0, cv::NORM_MINMAX);
    //
    // 	cv::Mat maskbinmat;
    // 	normat.convertTo(maskbinmat, CV_8UC1, 1);
    // 	imwrite("subbin.jpg", maskbinmat);

    //std::cout << mapmat << std::endl;

    return cv::mean(mapmat(cv::Rect(cv::Point(xmin, ymin), cv::Point(xmax + 1, ymax + 1))).clone(),
                    maskmat).val[0];

}


int unClip(std::vector<cv::Point> &minboxvec, float alledgesize, std::vector<cv::Point> &outvec,
           float unclip_ratio) {
    ClipperLib::Path poly;

    for (int i = 0; i < minboxvec.size(); ++i) {
        poly.push_back(ClipperLib::IntPoint(minboxvec[i].x, minboxvec[i].y));
    }

    double distace = unclip_ratio * ClipperLib::Area(poly) / (double) alledgesize;

    ClipperLib::ClipperOffset clipperoffset;
    clipperoffset.AddPath(poly, ClipperLib::JoinType::jtRound,
                          ClipperLib::EndType::etClosedPolygon);
    ClipperLib::Paths polys;
    polys.push_back(poly);
    clipperoffset.Execute(polys, distace);


    outvec.clear();
    std::vector<cv::Point> rsvec;
    for (int i = 0; i < polys.size(); ++i) {
        ClipperLib::Path tmppoly = polys[i];
        for (int j = 0; j < tmppoly.size(); ++j) {
            outvec.push_back(cv::Point(tmppoly[j].X, tmppoly[j].Y));
        }
    }

    return 1;

}