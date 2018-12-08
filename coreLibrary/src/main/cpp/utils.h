int Clamp(int value)
{
   if(value > 255) return 255;
   if(value < 0)   return 0;
   return value;
}

int GetR(int y, int u, int v)
{
    int C = y - 16;
    int D = u - 128;
    int E = v - 128;
    return Clamp(( 298 * C + 409 * E + 128) >> 8);
}

int GetG(int y, int u, int v)
{
   int C = y - 16;
   int D = u - 128;
   int E = v - 128;
   return Clamp(( 298 * C - 100 * D - 208 * E + 128) >> 8);
}

int GetB(int y, int u, int v)
{
   int C = y - 16;
   int D = u - 128;
   int E = v - 128;
   return Clamp(( 298 * C + 516 * D + 128) >> 8);
}

int GetY(int r, int g, int b)
{
    return (( 66 * r + 129 * g + 25 * b + 128) >> 8) +  16;
}

int GetU(int r, int g, int b)
{
    return (( -38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
}

int GetV(int r, int g, int b)
{
    return (( 112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
}
