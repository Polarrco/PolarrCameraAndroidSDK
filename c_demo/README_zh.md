# 泼辣滤镜Camera SDK C++版本
## 版权限制
包含本SDK在内的所有版本库中的内容，属于Polarr, Inc.版权所有。未经允许均不得用于商业目的。如需要获取完整授权等更多相关信息，请联系我们[info@polarr.co](mailto:info@polarr.co)

## 增加头文件
```objectivec
#include "polarrRender.h"
```
## 初始化 PolarrRender
如果在非OpenGL环境下调用，请将needEgl参数设置为true。
```objectivec
polarrRender = new PolarrRender;
bool needEgl;
polarrRender->init(needEgl);
```
## 设置YUV尺寸
```objectivec
INPUT_YUV_TYPE yuvType = INPUT_YUV_TYPE_NV12;
 polarrRender->setYUVsize(width, height, stride, scanline, yuvType);
```
## 滤镜
### 滤镜列表
参考头文件中的内置滤镜
```objectivec
enum POLARR_FILTER {
    F_COMMON_1,// 和风抹茶
    F_COMMON_2,// 秋天童话
    F_COMMON_3,// 青柠奶泡
    F_COMMON_4,// 海盐泡芙
    F_COMMON_5,// 黑咖啡
    F_COMMON_6,// 奇幻城堡
    F_FRONT_1,// 西柚冰沙
    F_FRONT_2,// 蓝莓松饼
    F_FRONT_3,// 雪域芝士
    F_FRONT_4,// 浆果奶酪
    F_FRONT_5,// 奶油慕斯
    F_BACK_1,// 花样年华
    F_BACK_2,// 情书
    F_BACK_3,// 2046
    F_BACK_4,// 莓果布丁
    F_BACK_5,// 重庆森林
    F_MODE_1, //模式1
    F_MODE_2, //模式2
    F_MODE_3, //模式3
    F_MODE_4, //模式4
    F_DEFAULT // 无滤镜
};
```
### 设置滤镜
```objectivec
polarrRender->initFilter(F_COMMON_1);
```
### 应用滤镜（连续的YUV数据）
该接口将复用输入数组
```objectivec
INPUT_YUV_TYPE yuvType = INPUT_YUV_TYPE_NV21;
polarrRender->applyFilter(yuvBytes, yuvType);
```
### 应用滤镜（分离的Y、UV数据）
该接口将复用输入数组
```objectivec
INPUT_YUV_TYPE yuvType = INPUT_YUV_TYPE_NV12;
polarrRender->applyFilterYUV(yBytes, uvBytes, yuvType);
```
### 支持的YUV格式
```objectivec
enum INPUT_YUV_TYPE {
    INPUT_YUV_TYPE_NV21,    // YUV420 NV21
    INPUT_YUV_TYPE_NV12,    // YUV420 NV12
    INPUT_YUV_TYPE_YV12     // YV12
};
```
## 释放资源
```objectivec
delete polarrRender;
```