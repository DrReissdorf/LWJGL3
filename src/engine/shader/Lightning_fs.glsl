#version 150
#extension GL_ARB_explicit_attrib_location : enable

layout (location = 0) out vec4 FragColor;
layout (location = 1) out vec4 BrightColor;

#define AMBILIGHT_DAY 0.15
#define AMBILIGHT_NIGHT 0.15

#define PCF_FORSAMPLE 1

#define GAMMA_CORRECTION_VALUE 2.2
#define EXPOSURE_BIAS 1

#define LIGHTS 1

uniform float uDayTime;

uniform vec3 cameraPos;
uniform vec3 backgroundColor;
uniform sampler2D uPositionTex;
uniform sampler2D uNormalTex;
uniform sampler2D uColorSpecTex;
uniform sampler2D uSpecTex;

uniform sampler2DShadow uShadowmap;
uniform mat4 uView;

in vec2 vTextureCoords;

struct Sun {
    vec3 color;
    vec3 position;
    mat4 projection;
    mat4 view;
    mat4 projectionView;
};
uniform Sun uSun;

struct Light {
    vec3 color;
    vec3 position;
    float range;
    mat4 projection;
    mat4 view;
    float intensity;
};
uniform Light[LIGHTS] uLights;

struct DirectionalLight {
    vec3 color;
    vec3 direction;
    float intensity;
};
uniform DirectionalLight uDirectionalLight;

uniform sampler2D uPingPongTexture; //blurred bright objects for bloom

vec3 blendTextures(sampler2D colorTexture, sampler2D bloomTexture) {
    vec3 color = texture(colorTexture,vTextureCoords).rgb;
    vec3 bloomColor = texture(bloomTexture, vTextureCoords).rgb;
    return color+bloomColor;
}

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

vec3 reinhardToneMapping(vec3 color) {
    vec3 mapped = color / (color + vec3(1.0));
    return mapped;
}

vec3 gammaCorrection(vec3 color, float gamma) {
    return pow(color.rgb, vec3(1.0 / gamma));
}

float shadows_PCF(in sampler2DShadow shadowmap, in vec4 shadowmapCoord, in float forSamples, in float nDotL ) {
    float bias = max(0.02 * (1.0 - nDotL), 0.005);
    //float bias = 0.002;

    vec3 ProjCoords = shadowmapCoord.xyz / shadowmapCoord.w;
    vec2 UVCoords;
    UVCoords.x = 0.5 * ProjCoords.x + 0.5;
    UVCoords.y = 0.5 * ProjCoords.y + 0.5;
    float z = 0.5 * ProjCoords.z + 0.5;

    /************ get texturesize *************/
    ivec2 texSize = textureSize(shadowmap,0);
    float xOffset = 1.0/float(texSize.x);
    float yOffset = 1.0/float(texSize.y);
    /******************************************/

    float shadowmap_factor = 0.0;
    float numberOfSamples = 0;
    for (float y = -forSamples ; y <= forSamples ; y++) {
        for (float x = -forSamples ; x <= forSamples ; x++) {

            //calculate offstes with size of texels
            vec2 Offsets = vec2(x * xOffset, y * yOffset);

            //add offsets to coordinates
            vec3 UVC = vec3(UVCoords + Offsets, z - bias);

            // add combined values to factor
            shadowmap_factor += texture(shadowmap, UVC);
            numberOfSamples++;
        }
    }

    return shadowmap_factor/numberOfSamples;
}

vec3 calculateSpecularBlinn(in vec3 N, in vec3 V, in vec3 L, in vec3 lightColor, in float nDotl, in float reflect, in float shine) {
    vec3 specular = vec3(0);

    if(nDotl > 0) {
        vec3 lightAddCam = L+V;
        vec3 H = normalize( lightAddCam/sqrt(dot(lightAddCam,lightAddCam)) );
        specular = lightColor * reflect * vec3(max(pow(dot(N, H), shine), 0.0));
    }

    return specular;
}

vec3 calculateDiffuse(in vec3 lightColor, in float nDotl) {
    return lightColor * vec3(max(nDotl, 0.0)) ;
}

float attenuationOfLight(in vec3 vPos, in vec3 lightPos, in float lightStartDist, in float lightEndDist) {
    float distance = length(vPos-lightPos);
    float lightIntense;

  /*  if(distance <= lightStartDist) {   //max helligkeit
        lightIntense = 1;
    } else if(distance >= lightEndDist) {
        lightIntense = 0;
    } else {
        lightIntense = (lightEndDist-distance)/(lightEndDist-lightStartDist);
    } */
    lightIntense = (lightEndDist-distance)/(lightEndDist-lightStartDist);
    return lightIntense;
}

float attenuation2(in vec3 vPos, in vec3 lightPos) {
    float distance = length(vPos-lightPos);
    return 1.0 / (distance * distance);
}

void main(void) {
    vec4 position   = texture(uPositionTex, vTextureCoords);
    vec4 normal     = texture(uNormalTex, vTextureCoords);
    vec4 color      = texture(uColorSpecTex, vTextureCoords);
    vec4 specValues = texture(uSpecTex, vTextureCoords);

    if(normal.x==0.0 && normal.y==0.0 && normal.z==0.0) {
        FragColor = vec4(backgroundColor,1.0);
    } else {
        if(specValues.z == 1) { //light detected, color object in light color
            FragColor = color;
        } else {
            float spec_reflectivity = normal.a;
            float spec_shininess = color.a;

            vec3 ambient;
            if(uDayTime > 180) {
                ambient = AMBILIGHT_DAY * color.rgb;
            } else {
                ambient = AMBILIGHT_NIGHT * color.rgb;
            }

            vec3 diffuseFinal = vec3(0);
            vec3 specularFinal = vec3(0);

            vec3 N = normalize(normal.xyz);

            /******************************************* SUN DIRECTIONAL LIGHT ******************************************/
            vec3 sun_diffuse = vec3(0);
            vec3 sun_specular = vec3(0);
            vec3 sun_pos = uSun.position+position.xyz;
            vec3 sun_N = normalize(normal.xyz);
            vec3 sun_L = normalize(sun_pos-position.xyz);
            vec3 sun_V = normalize(cameraPos - position.xyz);
            float sun_nDotl = dot(sun_N, sun_L);
            sun_diffuse += calculateDiffuse(uSun.color,sun_nDotl);
            sun_specular += calculateSpecularBlinn(sun_N, sun_V, sun_L, uSun.color, sun_nDotl, specValues.g,specValues.r);

            /******************************************* DIRECTIONAL LIGHT ******************************************/
         /*   vec3 sun_diffuse = vec3(0);
            vec3 sun_specular = vec3(0);
            vec3 sun_direction = normalize((uDirectionalLight.direction));

            vec3 sun_V = normalize(cameraPos-position.xyz);
            float sun_nDotl = dot(N, sun_direction);
            sun_diffuse += calculateDiffuse(uSun.color,sun_nDotl);
            sun_specular += calculateSpecularBlinn(N, sun_V, sun_direction, uSun.color, sun_nDotl, specValues.g,specValues.r); */


            /********************************************** NORMAL LIGHT *************************************************/
            float nDotl;
            float attenuation;
            vec3 L;
            for(int i=0 ; i<LIGHTS ; i++) {
                L = uLights[i].position-position.xyz;

                if(length(L) < uLights[i].range) { // just calculate lightning for objects the light can actually reach
                    L = normalize(L);
                    vec3 V = normalize(cameraPos - position.xyz);

                    nDotl = dot(N,L);
                    attenuation = attenuationOfLight(position.xyz, uLights[i].position, 1 , uLights[i].range );
                    diffuseFinal += calculateDiffuse(uLights[i].color,nDotl) * attenuation * uLights[i].intensity;
                    specularFinal += calculateSpecularBlinn(N, V, L, uLights[i].color, nDotl, specValues.g,specValues.r) * attenuation * uLights[i].intensity;
                }
            }

            /***************************************** SHADOW ************************************************************/
            vec4 shadowCoords = uSun.projectionView * vec4(position.xyz,1.0);
            float shadowFactor = shadows_PCF(uShadowmap,shadowCoords,PCF_FORSAMPLE,(dot(N,sun_pos)));

            vec3 sunLightingAndShadow = (((0.25+shadowFactor) * (sun_diffuse+sun_specular))*color.rgb)+ambient;
            vec3 LightSourceLighting = ((diffuseFinal+specularFinal)*color.rgb)+ambient;
            vec3 finalLighting = sunLightingAndShadow + LightSourceLighting ;  //SECOND OPTION

            vec3 finalColor = finalLighting*color.rgb;

            /****************************** TONEMAP + GAMMA CORRECTION ***************************************************/
            vec3 postProcessed = finalColor + texture(uPingPongTexture,vTextureCoords).rgb;
            //postProcessed = filmicToneMapping(postProcessed.xyz*EXPOSURE_BIAS);
            postProcessed = reinhardToneMapping(postProcessed.xyz*EXPOSURE_BIAS);
            postProcessed =  gammaCorrection(postProcessed.rgb, GAMMA_CORRECTION_VALUE);

            FragColor = vec4(postProcessed, 1.0);
        }

        /* RENDER BRIGHTOBJECTSTEXTURE */
       // float brightness = dot(FragColor.rgb, vec3(0.2126, 0.7152, 0.0722));
       // if(brightness > 1.0) BrightColor = vec4(FragColor.rgb, 1.0);
    }
}