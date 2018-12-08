//#include "cameraShader.h"

typedef enum RotationAngle
{
    Angle_0   = 0,
    Angle_90  = 1,
    Angle_180 = 2,
    Angle_270 = 3
} RotationAngle;

void CmOpenGLES_init(int pWidth, int pHeight, RotationAngle rotation_angle, int is_horizontal_flip);
void CmOpenGLES_changeRotation(RotationAngle rotation_angle, int is_horizontal_flip);
void CmOpenGLES_changeLayout(int width, int height);
void CmOpenGLES_drawNV21Frame(int8_t *yuvDatas, int size);
void CmOpenGLES_drawI420Frame(unsigned char *i420DatasY, int sizeY, unsigned char *i420DatasU, int sizeU, unsigned char *i420DatasV, int sizeV);
void CmOpenGLES_release();
Instance * getInstance();


