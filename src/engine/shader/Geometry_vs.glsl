#version 150
#extension GL_ARB_separate_shader_objects : enable

layout(location=0) in vec3 aPosition;
layout(location=1) in vec3 aNormal;
layout(location=2) in vec2 textureCoords;

out vec3 normal;
out vec4 position;
out vec2 vTextureCoords;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uNormalMat;

void main(void) {
    vTextureCoords = textureCoords;

    vec4 worldPosition = uModel * vec4(aPosition,1.0);

    normal = normalize( mat3(uNormalMat) * aNormal );
    position = worldPosition;

    gl_Position = uProjection * uView * worldPosition;
}