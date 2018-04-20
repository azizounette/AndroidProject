#pragma version(1)
#pragma rs java_package_name(com.example.alarvet.seguin_larvet_androidproject)

uchar4 RS_KERNEL toSepia(uchar4 in) {
    float4 pixelf = rsUnpackColor8888(in);

    float gray = (0.30*pixelf.r + 0.59*pixelf.g + 0.11*pixelf.b);

    float tr = 0.393*pixelf.r + 0.796*pixelf.g + 0.189*pixelf.b;
    float tg = 0.349*pixelf.r + 0.686*pixelf.g + 0.168*pixelf.b;
    float tb = 0.272*pixelf.r + 0.534*pixelf.g + 0.131*pixelf.b;

    float red = fmin(tr, 255);
    float green = fmin(tg, 255);
    float blue = fmin(tb, 255);

    return rsPackColorTo8888(red, green, blue, pixelf.a);
}