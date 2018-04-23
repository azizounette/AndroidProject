#pragma version(1)
#pragma rs java_package_name(com.example.alarvet.seguin_larvet_androidproject)

float brightnessScale = 1;

uchar4 RS_KERNEL negative(uchar4 in) {

    float4 out = rsUnpackColor8888(in);

    out.r *= brightnessScale/100;
    out.g *= brightnessScale/100;
    out.b *= brightnessScale/100;

    if(out.r > 1) {out.r = 1;}
    if(out.g > 1) {out.g = 1;}
    if(out.b > 1) {out.b = 1;}

    in = rsPackColorTo8888(out);

    return in;

}