#version 150
#extension GL_ARB_explicit_attrib_location : enable

#define LIGHTS 2

layout(location=0) in vec3 aPosition;
layout(location=1) in vec3 aNormal;
layout(location=2) in vec2 textureCoords;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uInvertedUView;
uniform mat4 uNormalMat;

out vec2 vTextureCoords;
/*
out vec3 sun_N;
out vec3 sun_V;
out vec3 sun_L;
out float sunAttenuation;
out vec4 vSunView;

uniform vec3 uSunPos;
uniform float uSunRange;
*/

// LIGHT
#ifdef LIGHTS
    uniform mat4[LIGHTS] uLightProjections;
    uniform mat4[LIGHTS] uLightViews;
    uniform vec3[LIGHTS] uLightPosArray;
    uniform vec3[LIGHTS] uLightColorArray;
    uniform float[LIGHTS] uLightRangesArray;
    out vec3[LIGHTS] light_N_array;
    out vec3[LIGHTS] light_V_array;
    out vec3[LIGHTS] light_L_array;
    out float[LIGHTS] light_attenuation_array;

    out vec4[LIGHTS] uShadowViews;
#endif


// SHADOW
uniform mat4 uSunProjection;
uniform mat4 uSunView;

float attenuationOfLight(vec3 vPos, vec3 lightPos, float lightStartDist, float lightEndDist) {
    float distance = length(vPos-lightPos);
    float lightIntense;

    if(distance <= lightStartDist) {   //max helligkeit
        lightIntense = 1;
    } else if(distance >= lightEndDist) {
        lightIntense = 0;
    } else {
        lightIntense = (lightEndDist-distance)/(lightEndDist-lightStartDist);
    }

    return lightIntense;
}

void main(void) {
    vTextureCoords = textureCoords;

    vec4 worldPosition = uModel * vec4(aPosition,1.0);

    // VertexPosition aus Sicht der Lichtquelle
/*    vSunView = uSunProjection * uSunView * worldPosition;

    vec3 sunWorldPosition = mat3(uModel) * uSunPos;
    sun_N = normalize( mat3(uNormalMat) * aNormal );
    sun_V = normalize( (uInvertedUView * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz );
    sun_L = normalize(sunWorldPosition - worldPosition.xyz);
    sunAttenuation = attenuationOfLight(worldPosition.xyz, sunWorldPosition, 1 , uSunRange );
*/

    for(int i=0 ; i<LIGHTS ; i++) {
        vec3 lightPosition = (mat3(uModel) * uLightPosArray[i]);
        light_N_array[i] = normalize( mat3(uNormalMat) * aNormal );
        light_V_array[i] = normalize( (uInvertedUView * vec4(0.0,0.0,0.0,1.0)).xyz - lightPosition);
        light_L_array[i] = normalize(lightPosition - worldPosition.xyz);
        light_attenuation_array[i] = attenuationOfLight(worldPosition.xyz, lightPosition, 1 , uLightRangesArray[i] );
        uShadowViews[i] = uLightProjections[i] * uLightViews[i] * worldPosition;
    }

    gl_Position = uProjection * uView * worldPosition;
}