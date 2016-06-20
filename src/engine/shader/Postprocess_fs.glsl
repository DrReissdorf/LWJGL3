#version 150

#define GAMMA_CORRECTION_VALUE 2.2

#define A 0.22      //Shoulder Strength
#define B 0.30      //Linear Strength
#define C 0.10      //Linear Angle
#define D 0.20      //Toe Strength
#define E 0.01      //Toe Numerator
#define F 0.30      //Toe Denominator
#define WHITE 11.2  // Linear White Point
#define EXPOSURE_BIAS 2

in vec2 vTextureCoords;
out vec4 FragColor;

uniform sampler2D uTexture;

vec3 gammaCorrection(vec3 color, float gamma) {
    return pow(color.rgb, vec3(1.0 / gamma));
}

vec3 filmicToneMapping(vec3 color, float exposureBias) {
    vec3 newColor;

    newColor = (((color*(A*color+C*B)+D*E)/(color*(A*color+B)+D*F)) - E/F) * EXPOSURE_BIAS;
    newColor /= ((WHITE*(A*WHITE+C*B)+D*E)/(WHITE*(A*WHITE+B)+D*F)) - E/F;

    return newColor;
}

void main() {
    vec4 color = texture(uTexture,vTextureCoords);
    color = vec4(filmicToneMapping(color.xyz,3.25),1.0);
    FragColor = vec4(gammaCorrection(color.rgb,GAMMA_CORRECTION_VALUE),1.0);
    }