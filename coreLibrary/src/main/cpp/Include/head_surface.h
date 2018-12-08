#ifndef  _HEAD_SURFACE_H__
#define  _HEAD_SURFACE_H__

#include <string>
using namespace std;

struct Rect
{
    Rect(int a, int b, int w, int h){
        left = a;
        top = b;
        width = w;
        height = h;
    }
    int left;
    int top;
    int width;
    int height;
};

class HeadSurfaceImpl;
class HeadSurface
{
public:
    HeadSurface(string datapath);
    ~HeadSurface();

    bool depth_to_mesh(string depth_png, Rect &face_roi,string mesh_out);
    bool depth_to_mesh_auto_face(string depth_png, string color_bmp, string mesh_out);
    bool register_to_basemesh(string fn_scanmesh, string fn_outmesh, bool befemale);

private:
    HeadSurfaceImpl *pImpl;    
    string datapath;
};

#endif
