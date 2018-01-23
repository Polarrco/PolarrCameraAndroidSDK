#ifndef POLARR_RENDER_H
#define POLARR_RENDER_H 1

#define POLARR_SDK_VERSION "1.0.0_b11"

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

class RenderInternal;

class PolarrRender {
public:
    PolarrRender();

    virtual ~PolarrRender();

    void init(bool needEgl);

    void setYUVsize(int width, int height,
                    int stride, int scanline);

    void initFilter(POLARR_FILTER filterType);

    void applyFilter(unsigned char *inputBytes);

    void applyFilterYUV(unsigned char *yBytes, unsigned char *uvBytes);

private:
    RenderInternal *params;
};

#endif // POLARR_RENDER_H
