#ifndef POLARR_RENDER_H
#define POLARR_RENDER_H 1

#define POLARR_SDK_VERSION "1.0.0_b14"

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
