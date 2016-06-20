#version 330 core

layout (location = 0) out vec4 gAlbedoSpec;
layout (location = 1) out vec4 gNormal;
layout (location = 2) out vec4 gPosition;

in vec2 vTextureCoords;
in vec3 normal;
in vec4 position;

out vec4 FragColor;

uniform sampler2D uTexture;
uniform float uHasTexture;
uniform float uTextureScale;
uniform float uShininess;
uniform float uReflectivity;

void main( void ) {

    if(uHasTexture > 0.5) gAlbedoSpec = vec4(texture(uTexture,vTextureCoords*uTextureScale).rgb,uShininess); // Diffuse
    else gAlbedoSpec = vec4(1.0,1.0,1.0,0.0);

    gNormal = vec4(normalize(normal),uReflectivity); // normals
    gPosition = vec4(position.xyz,uTextureScale); // Position

    //gl_FragData[0] = texture(uTexture,vTextureCoords); // Diffuse
    //gl_FragData[1] = vec4(normalize(normal),1.0); // normals
    //gl_FragData[2] = position; // Position

}