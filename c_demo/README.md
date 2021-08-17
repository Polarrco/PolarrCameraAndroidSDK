# Polarr Camera SDK C++ Version
## License
The SDK included in this repository must not be used for any commercial purposes without the direct written consent of Polarr, Inc. The current version of the SDK expires on December 31, 2021. For pricing and more info regarding the full license SDK, please email [hello@polarr.ai](mailto:hello@polarr.ai).

## Include header
```objectivec
#include "polarrRender.h"
```
## Initialize PolarrRender
If method is called outside an OpenGL environment, then please set needEgl to be true.
```objectivec
polarrRender = new PolarrRender;
bool needEgl;
polarrRender->init(needEgl);
```
## Set YUV dimensions
```objectivec
 polarrRender->setYUVsize(width, height, stride, scanline);
```
## Filters
### Filter packs
Refer to the header file for list of included filters
```objectivec
enum POLARR_FILTER {
    F_COMMON_1,// Japan
    F_COMMON_2,// Nature
    F_COMMON_3,// Clear
    F_COMMON_4,// Clay
    F_COMMON_5,// B&W
    F_COMMON_6,// M1
    F_FRONT_1,// Icy
    F_FRONT_2,// Lagoon
    F_FRONT_3,// Paris 10
    F_FRONT_4,// T2
    F_FRONT_5,// Muse
    F_BACK_1,// M3
    F_BACK_2,// T1
    F_BACK_3,// C1
    F_BACK_4,// Electric
    F_BACK_5,// S109
    F_MODE_1, //M1
    F_MODE_2, //M2
    F_MODE_3, //M3
    F_MODE_4, //M4
    F_DEFAULT //No filter
};
```
### Set filter
```objectivec
polarrRender->initFilter(F_COMMON_1);
```
### Apply filter via YUV format
This interface will reuse the input parameter, YUV format is YUV420 NV21
```objectivec
polarrRender->applyFilter(yuvBytes);
```
### Apply filter via separate Y and UV parameters
This interface will reuse the input parameter, YUV format is YUV420 NV21
```objectivec
polarrRender->applyFilterYUV(yBytes, uvBytes);
```
## Release resources 
```objectivec
delete polarrRender;
```
