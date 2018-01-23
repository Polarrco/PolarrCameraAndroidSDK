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
 polarrRender->setYUVsize(width, height, stride, scanline);
```
## 滤镜
### 滤镜列表
参考头文件中的内置滤镜
```objectivec
enum POLARR_FILTER {
    F_COMMON_1,// 日系
    F_COMMON_2,// 自然
    F_COMMON_3,// 清晰
    F_COMMON_4,// 海泡岩
    F_COMMON_5,// 黑白
    F_COMMON_6,// M1
    F_FRONT_1,// 冰沙
    F_FRONT_2,// 蓝湖
    F_FRONT_3,// 巴黎10
    F_FRONT_4,// T2
    F_FRONT_5,// 缪斯
    F_BACK_1,// M3
    F_BACK_2,// T1
    F_BACK_3,// C1
    F_BACK_4,// Electric
    F_BACK_5,// S109
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
该接口将复用输入数组, yuv格式为 YUV420 NV21
```objectivec
polarrRender->applyFilter(yuvBytes);
```
### 应用滤镜（分离的Y、UV数据）
该接口将复用输入数组, yuv格式为 YUV420 NV21
```objectivec
polarrRender->applyFilterYUV(yBytes, uvBytes);
```
## 释放资源
```objectivec
delete polarrRender;
```