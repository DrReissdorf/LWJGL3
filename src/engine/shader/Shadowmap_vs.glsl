#version 150
#extension GL_ARB_explicit_attrib_location : enable

layout(location=0) in vec3 aPosition;

uniform mat4 uProjectionViewModel;

void main() {
    gl_Position = uProjectionViewModel * vec4(aPosition, 1.0);
}