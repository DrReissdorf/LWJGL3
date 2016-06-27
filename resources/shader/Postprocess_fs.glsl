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
uniform sampler2D uTexture;
uniform sampler2D uPingPongTexture; //blurred bright objects for bloom
out vec4 FragColor;

vec3 filmicToneMapping(vec3 color) {
    vec3 newColor;

    newColor = (((color*(A*color+C*B)+D*E)/(color*(A*color+B)+D*F)) - E/F);
    newColor /= ((WHITE*(A*WHITE+C*B)+D*E)/(WHITE*(A*WHITE+B)+D*F)) - E/F;

    return newColor;
}

vec3 blendTextures(sampler2D colorTexture, sampler2D bloomTexture) {
    vec3 color = texture(colorTexture,vTextureCoords).rgb;
    vec3 bloomColor = texture(bloomTexture, vTextureCoords).rgb;
    return color+bloomColor;
}

vec3 reinhardToneMapping(vec3 color) {
    vec3 mapped = color / (color + vec3(1.0));
    return mapped;
}

vec3 gammaCorrection(vec3 color, float gamma) {
    return pow(color.rgb, vec3(1.0 / gamma));
}

void main() {
    vec3 color = blendTextures(uTexture, uPingPongTexture);

    //color = filmicToneMapping(color.xyz*EXPOSURE_BIAS);
    color = reinhardToneMapping(color.xyz*EXPOSURE_BIAS);
    color =  gammaCorrection(color.rgb, GAMMA_CORRECTION_VALUE);

    FragColor = vec4(color,1.0);
}