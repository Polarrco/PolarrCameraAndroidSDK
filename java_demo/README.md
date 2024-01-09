# Polarr Camera SDK Java Version
## License
The SDK included in this repository must not be used for any commercial purposes without the direct written consent of Polarr, Inc. The current version of the SDK expires on December 31, 2024. For pricing and more info regarding the full license SDK, please email [hello@polarr.ai](mailto:hello@polarr.ai).

## Add dependencies to the gradle files 
```groovy
// render sdk
compile (name: 'renderer-camera-release', ext: 'aar')
```
## Initialize PolarrRender in the GL thread
```java
PolarrRender polarrRender = new PolarrRender();
  
@Override
public void onSurfaceCreated(GL10 gl, EGLConfig config) {
// call in gl thread
boolean fastMode = true; // fast rendering 
int inputTextureType = PolarrRender.EXTERNAL_OES; // PolarrRender.TEXTURE_2D, PolarrRender.EXTERNAL_OES
polarrRender.initRender(getResources(), getWidth(), getHeight(), fastMode, inputTextureType);
}
```
## Create or pass in Texture
### Create Texture
```java
// call this method only once
polarrRender.createInputTexture();
// bind a bitmap to sdk
int inputTexture = polarrRender.getTextureId();
GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTexture);
GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);

// update Texture explicitly using the following method
polarrRender.updateInputTexture();
```
### Pass in an input Texture
```java
//  default is GL_TEXTURE_2D
polarrRender.setInputTexture(inputTexture);
// update Texture explicitly using the following method
polarrRender.updateInputTexture();
```
## Set up output Texture (optional)
If you do not specify an output Texture, the SDK will create an output Texture. Using [Obtain output Texture](#Obtain output Texture) to acquire.
```java
//  Must be a GL_TEXTURE_2D
polarrRender.setOutputTexture(outputTexture);
```
## Update the Texture size
```java
// call in gl thread
polarrRender.updateSize(width, height);
```
## Render
```java
@Override
public void onDrawFrame(GL10 gl) {
    // call in GL thread
    polarrRender.drawFrame();
}
```
## Render multiple filters onto Texture
```java
int gridOutputTexture; // output Texture，must be different from polarrRender output/input texture
List<DrawingItem> drawingItems; // Need the filter id and rect to be rendered
DrawingItem.filterId; 
DrawingItem.rect; // rendered to the specified rect
  
polarrRender.drawFiltersFrame(drawingItems, gridOutputTexture);
```
## Obtain output Texture
```java
int out = polarrRender.getOutputId();
```
## Release resources
```java
// call in GL thread
polarrRender.release();
```
## Filters
SDK includes Polarr's proprietary filter packs, which are in side the renderer module.
### Filter descriptions 
General pack
```java
FilterPackageUtil.F_COMMON_1 // Japan
FilterPackageUtil.F_COMMON_2 // Nature
FilterPackageUtil.F_COMMON_3 // Clear
FilterPackageUtil.F_COMMON_4 // Clay
FilterPackageUtil.F_COMMON_5 // Black & White
FilterPackageUtil.F_COMMON_6 // M1
```
Front pack
```java
FilterPackageUtil.F_FRONT_1 // Icy
FilterPackageUtil.F_FRONT_2 // Lagoon
FilterPackageUtil.F_FRONT_3 // Paris 10
FilterPackageUtil.F_FRONT_4 // T2
FilterPackageUtil.F_FRONT_5 // Muse
```
Back pack
```java
FilterPackageUtil.F_BACK_1 // M3
FilterPackageUtil.F_BACK_2 // T1
FilterPackageUtil.F_BACK_3 // C1
FilterPackageUtil.F_BACK_4 // Electric
FilterPackageUtil.F_BACK_5 // S109
 ```
Light pack
```java
FilterPackageUtil.F_MODE_1 // M1
FilterPackageUtil.F_MODE_2 // M2
FilterPackageUtil.F_MODE_3 // M3
FilterPackageUtil.F_MODE_4 // M4
```
Default effect
```java
FilterPackageUtil.F_DEFAULT
```
### Set filter
```java
polarrRender.fastUpdateFilter(filterId);
```
### Reset to default filter
```java
polarrRender.fastUpdateFilter(FilterPackageUtil.F_DEFAULT);
```
## Get the version
```java
String version = PolarrRender.Version();
```
