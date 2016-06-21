#version 150
#extension GL_ARB_separate_shader_objects : enable

layout (location = 0) out vec4 gAlbedoSpec;
layout (location = 1) out vec4 gNormal;
layout (location = 2) out vec4 gPosition;
layout (location = 3) out vec4 gSpec;

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
    if(uHasTexture > 0.5) gAlbedoSpec = vec4(texture(uTexture,vTextureCoords*uTextureScale).rgb,1.0); // Diffuse
    else gAlbedoSpec = vec4(1.0,1.0,1.0,1.0);

    gNormal = vec4(normalize(normal),1.0); // normals
    gPosition = vec4(position.xyz,uTextureScale); // Position
    gSpec = vec4(uShininess, uReflectivity, 1.0, 1.0); // spec values (shini, reflect)
  /*  if(uHasTexture > 0.5) gl_FragData[0] = vec4(texture(uTexture,vTextureCoords*uTextureScale).rgb,uShininess); // Diffuse
    else gl_FragData[0] = vec4(1.0,1.0,1.0,uShininess);

    gl_FragData[1] = vec4(normalize(normal),uReflectivity); // normals
    gl_FragData[2] = vec4(position.xyz,uTextureScale); // Position

*/
}