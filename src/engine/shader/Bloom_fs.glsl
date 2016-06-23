#version 150

in vec2 vTextureCoords;
out vec4 FragColor;

uniform sampler2D uTexture;
uniform sampler2D uPingPongTexture;

void main() {
    vec3 hdrColor = texture(uTexture, vTextureCoords).rgb;
    vec3 bloomColor = texture(uPingPongTexture, vTextureCoords).rgb;
    hdrColor += bloomColor; // additive blending

    FragColor = vec4(hdrColor, 1.0f);
}