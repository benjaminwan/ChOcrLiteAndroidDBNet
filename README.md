# ChOcrLiteAndroidDBNet

#### APK下载
[Gitee下载](https://gitee.com/benjaminwan/ch-ocr-lite-android-dbnet/releases)

#### 介绍
chineseocr lite android dbnet，超轻量级中文ocr android demo，支持竖排文字识别, 支持ncnn推理 ( dbnet+anglenet+crnn)

**代码和模型均源自chineseocr lite**
详情请查看 [https://github.com/ouyanghuiyu/chineseocr_lite](https://github.com/ouyanghuiyu/chineseocr_lite)

采用ncnn神经网络前向计算框架[https://github.com/Tencent/ncnn](https://github.com/Tencent/ncnn)

整合了如下算法：
dbnet(图像分割)+shufflenet(文字方向检测)+crnn-lstm(文字识别)

#### 架构

1. 封装为独立的Library。
2. Native层以C++编写。
3. demo app以kotlin-jvm编写。
4. 内置的ncnn预编译库版本ncnn-android-lib.zip 20200916 b766c8c
5. 内置的opencv动态库版本opencv-3.4.10-android-sdk.zip

#### 编译环境

1.  AndroidStudio 4.0或以上
2.  NDK
3.  cmake 3.4.1或以上
#### psenet版本
[ChOcrLiteAndroidPseNet](https://github.com/benjaminwan/ChOcrLiteAndroidPseNet)
