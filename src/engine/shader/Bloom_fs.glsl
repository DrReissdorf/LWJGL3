#version 150

#define GAMMA_CORRECTION_VALUE 2.2

in vec2 vTextureCoords;
out vec4 FragColor;

uniform sampler2D uTexture;

void main() {
    vec4 color = texture(uTexture,vTextureCoords);
    FragColor = color;
}