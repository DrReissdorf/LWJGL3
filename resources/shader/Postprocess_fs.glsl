#version 150

#define GAMMA_CORRECTION_VALUE 2.2

#define EXPOSURE_BIAS 5

in vec2 vTextureCoords;
uniform sampler2D uTexture;
uniform sampler2D uPingPongTexture; //blurred bright objects for bloom
out vec4 FragColor;

vec3 filmicToneMapping(vec3 color) {
    const float A = 0.22;      //Shoulder Strength
    const float B = 0.30;      //Linear Strength
    const float C = 0.10;      //Linear Angle
    const float D = 0.20;      //Toe Strength
    const float E = 0.01;      //Toe Numerator
    const float F = 0.30;      //Toe Denominator
    const float WHITE = 11.2;  // Linear White Point

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
    //vec3 color = texture(uTexture,vTextureCoords).rgb;
    //color = filmicToneMapping(color.xyz*EXPOSURE_BIAS);
    color = reinhardToneMapping(color.xyz*EXPOSURE_BIAS);
    color =  gammaCorrection(color.rgb, GAMMA_CORRECTION_VALUE);

    FragColor = vec4(color,1.0);
}