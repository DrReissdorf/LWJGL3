#version 330 core
#extension GL_ARB_explicit_attrib_location : enable

layout(location=0) in vec3 aPosition;
layout(location=1) in vec3 aNormal;
layout(location=2) in vec2 textureCoords;

out vec2 vTextureCoords;

void main(void) {
    vTextureCoords = textureCoords;
    gl_Position = vec4(aPosition.x, aPosition.y, aPosition.z, 1.0);
}