#version 150

#define AMBILIGHT_DAY 0.2
#define AMBILIGHT_NIGHT 0.2
#define PCF_FORSAMPLE 1

#define LIGHTS 6
uniform mat4[LIGHTS] uLightProjections;
uniform mat4[LIGHTS] uLightViews;
uniform vec3[LIGHTS] uLightPosArray;
uniform vec3[LIGHTS] uLightColorArray;
uniform float[LIGHTS] uLightRangesArray;
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

uniform vec3 uSunDirection;
uniform vec3 uSunColor;
uniform mat4 uSunProjection;
uniform mat4 uSunView;

out vec4 FragColor;

float shadows_PCF(sampler2DShadow shadowmap, vec4 shadowmapCoord, float forSamples, float nDotL ) {
    float bias = max(0.01 * (1.0 - nDotL), 0.002);
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

vec3 calculateSpecularBlinn(vec3 N, vec3 V, vec3 L, vec3 lightColor, float nDotl, float reflect, float shine) {
    vec3 specular = vec3(0);

    if(nDotl > 0) {
        vec3 lightAddCam = L+V;
        vec3 H = normalize( lightAddCam/sqrt(dot(lightAddCam,lightAddCam)) );
        specular = lightColor * reflect * vec3(max(pow(dot(N, H), shine), 0.0));
    }

    return specular;
}

vec3 calculateDiffuse(vec3 lightColor, float nDotl) {
    return lightColor * vec3(max(nDotl, 0.0)) ;
}

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
    vec4 position   = texture(uPositionTex, vTextureCoords);
    vec4 normal     = texture(uNormalTex, vTextureCoords);
    vec4 color      = texture(uColorSpecTex, vTextureCoords);
    vec4 specValues = texture(uSpecTex, vTextureCoords);

    if(normal.x==0.0 && normal.y==0.0 && normal.z==0.0) {
        FragColor = vec4(backgroundColor,1.0);
    } else {
        if(specValues.z == 1) { //light detected
            FragColor = color;
        } else {
            float spec_reflectivity = normal.a;
            float spec_shininess = color.a;


            //vec3 ambient = AMBILIGHT * color.rgb ;

               float ambient;
               if(uDayTime > 180) {
                   ambient = AMBILIGHT_NIGHT;
               } else {
                   ambient = AMBILIGHT_DAY;
               }

            vec3 diffuseFinal = vec3(0);
            vec3 specularFinal = vec3(0);

            /******************************************* SUN DIRECTIONAL LIGHT ******************************************/
            vec3 sun_diffuse = vec3(0);
            vec3 sun_specular = vec3(0);
            vec3 sun_pos = uSunDirection+position.xyz;
            vec3 sun_N = normalize(normal.xyz);
            vec3 sun_L = normalize(sun_pos-position.xyz);
            vec3 sun_V = normalize(cameraPos - position.xyz);
            float sun_nDotl = dot(sun_N, sun_L);
            sun_diffuse += calculateDiffuse(uSunColor,sun_nDotl);
            sun_specular += calculateSpecularBlinn(sun_N, sun_V, sun_L, uSunColor, sun_nDotl, specValues.g,specValues.r);
            /*************************************************************************************************************/

            /********************************************** NORMAL LIGHT *************************************************/
            float nDotl;
            float attenuation;
            vec3 L;
            for(int i=0 ; i<LIGHTS ; i++) {
                L = uLightPosArray[i]-position.xyz;

                if(length(L) < uLightRangesArray[i]) { // just calculate lightning for objects the light can actually reach
                    L = normalize(L);
                    vec3 N = normalize(normal.xyz);
                    vec3 V = normalize(cameraPos - position.xyz);

                    nDotl = dot(N,L);
                    attenuation = attenuationOfLight(position.xyz, uLightPosArray[i], 1 , uLightRangesArray[i] );

                    diffuseFinal += calculateDiffuse(uLightColorArray[i],nDotl) * attenuation;
                    specularFinal += calculateSpecularBlinn(N, V, L, uLightColorArray[i], nDotl, specValues.g,specValues.r) * attenuation;
                }
            }
            /*************************************************************************************************************/

            vec4 shadowCoords = uSunProjection * uSunView * vec4(position.xyz,1.0);
            float shadowFactor = shadows_PCF(uShadowmap,shadowCoords,PCF_FORSAMPLE,(dot(sun_N,sun_L)));
                //shadowFactor = 1;
            //FragColor = vec4((ambient+shadowFactor * (diffuseFinal+specularFinal)) * color.rgb, 1.0);
            //vec3 lighting = (ambient+shadowFactor * (sun_diffuse+sun_specular)) + diffuseFinal+specularFinal;  //FIRST OPTION
            vec3 sunLightingAndShadow = (shadowFactor * (sun_diffuse+sun_specular));
            vec3 LightSourceLighting = diffuseFinal+specularFinal;
            vec3 finalLighting = sunLightingAndShadow + LightSourceLighting + ambient;  //SECOND OPTION
            FragColor = vec4(finalLighting * color.rgb, 1.0);
        }
    }
}