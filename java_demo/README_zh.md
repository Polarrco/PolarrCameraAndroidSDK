# 泼辣滤镜Camera SDK Java版版本
## 版权限制
包含本SDK在内的所有版本库中的内容，属于Polarr, Inc.版权所有。未经允许均不得用于商业目的。当前版本的示例SDK失效时间为2018年1月31日。如需要获取完整授权等更多相关信息，请联系我们[info@polarr.co](mailto:info@polarr.co)

## 增加 dependencies 到 Gradle文件
```groovy
// render sdk
compile (name: 'renderer-camera-release', ext: 'aar')
```
## 在GL线程中初始化 PolarrRender
```java
PolarrRender polarrRender = new PolarrRender();
  
@Override
public void onSurfaceCreated(GL10 gl, EGLConfig config) {
// call in gl thread
boolean fastMode = true; // true 为Camera应用优化
int inputTextureType = PolarrRender.EXTERNAL_OES; // PolarrRender.TEXTURE_2D, PolarrRender.EXTERNAL_OES
polarrRender.initRender(getResources(), getWidth(), getHeight(), fastMode, inputTextureType);
}
```
## 创建或传入Texture
### 创建Texture
```java
// 只需要调用一次
polarrRender.createInputTexture();
// bind a bitmap to sdk
int inputTexture = polarrRender.getTextureId();
GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTexture);
GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);

// 输入Texture变化后需要调用
polarrRender.updateInputTexture();
```
### 传入一个输入Texture
```java
//  默认为GL_TEXTURE_2D格式
polarrRender.setInputTexture(inputTexture);
// 输入Texture变化后需要调用
polarrRender.updateInputTexture();
```
## 设置输出Texture (非必须)
如果不设置输出Texture，SDK将会创建一个输出Texture。通过[获取输出的Texture](#获取输出的Texture)获取
```java
//  必须为GL_TEXTURE_2D格式
polarrRender.setOutputTexture(outputTexture);
```
## 更新渲染尺寸。更新后需要更新输入Texture
```java
// call in gl thread
polarrRender.updateSize(width, height);
```
## 渲染
```java
@Override
public void onDrawFrame(GL10 gl) {
    // call in GL thread
    polarrRender.drawFrame();
}
```
## 渲染多个滤镜到Texture上
```java
int gridOutputTexture; // 输出texture，不能和已经设置给polarrRender的output/input texture 相同
List<DrawingItem> drawingItems; // 要渲染的滤镜id和位置
DrawingItem.filterId; // 滤镜ID
DrawingItem.rect; // 渲染到输出texture的位置
  
polarrRender.drawFiltersFrame(drawingItems, gridOutputTexture);
```
## 获取输出的Texture
```java
int out = polarrRender.getOutputId();
```
## 释放资源
```java
// call in GL thread
polarrRender.release();
```
## 滤镜工具
SDK 内置了泼辣修图的滤镜包，滤镜包数据内置于renderer module中。
### 内置滤镜说明
通用系列
```java
FilterPackageUtil.F_COMMON_1 // 日系
FilterPackageUtil.F_COMMON_2 // 自然
FilterPackageUtil.F_COMMON_3 // 清晰
FilterPackageUtil.F_COMMON_4 // 海泡岩
FilterPackageUtil.F_COMMON_5 // 黑白
FilterPackageUtil.F_COMMON_6 // M1
```
前置系列
```java
FilterPackageUtil.F_FRONT_1 // 冰沙
FilterPackageUtil.F_FRONT_2 // 蓝湖
FilterPackageUtil.F_FRONT_3 // 巴黎10
FilterPackageUtil.F_FRONT_4 // T2
FilterPackageUtil.F_FRONT_5 // 缪斯
```
后置系列
```java
FilterPackageUtil.F_BACK_1 // M3
FilterPackageUtil.F_BACK_2 // T1
FilterPackageUtil.F_BACK_3 // C1
FilterPackageUtil.F_BACK_4 // Electric
FilterPackageUtil.F_BACK_5 // S109
 ```
光效系列
```java
FilterPackageUtil.F_MODE_1 // 模式1
FilterPackageUtil.F_MODE_2 // 模式2
FilterPackageUtil.F_MODE_3 // 模式3
FilterPackageUtil.F_MODE_4 // 模式4
```
默认效果
```java
FilterPackageUtil.F_DEFAULT
```
### 设置滤镜
```java
polarrRender.fastUpdateFilter(filterId);
```
### 恢复默认效果
```java
polarrRender.fastUpdateFilter(FilterPackageUtil.F_DEFAULT);
```
## 获取版本号
```java
String version = PolarrRender.Version();
```