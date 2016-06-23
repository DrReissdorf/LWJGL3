#version 150
#extension GL_ARB_explicit_attrib_location : enable

layout(location=0) in vec3 aPosition;
layout(location=2) in vec2 aTextureCoords;

out vec2 vTextureCoords;

void main() {
    vTextureCoords = aTextureCoords;

    gl_Position = vec4(aPosition.x, aPosition.y, aPosition.z, 1.0);
}