#version 150

#define LIGHTS 2

#define AMBILIGHT 0.2
#define BLINN_ADD_SHINE 100
#define SHADOW_BIAS 0.001
#define PCF_SAMPLES 1

in vec3 uNormal;
//in vec4 vSunView; //shadow
in vec2 vTextureCoords;
//in vec3 sun_N;
//in vec3 sun_V;
//in vec3 sun_L;
//in float sunAttenuation;

#ifdef LIGHTS
    in vec3[LIGHTS] light_N_array;
    in vec3[LIGHTS] light_V_array;
    in vec3[LIGHTS] light_L_array;
    uniform mat4[LIGHTS] uLightProjections;
    uniform mat4[LIGHTS] uLightViews;
    uniform vec3[LIGHTS] uLightPosArray;
    uniform vec3[LIGHTS] uLightColorArray;
    uniform float[LIGHTS] uLightRangesArray;
    uniform float uIsLight;
    uniform vec3 uCurrentLightColor;
    in float[LIGHTS] light_attenuation_array;

    in vec4[LIGHTS] uShadowViews;
    float[LIGHTS] shadowFactors;
#endif

/***************** MATS *************/
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uInvertedUView;
uniform mat4 uNormalMat;

/***************** LIGHTS *************/

uniform float uShininess;
uniform float uReflectivity;


/********** SHADOW ********************/
uniform sampler2DShadow uShadowmap;

/****************** TEXTURE ************/
uniform sampler2D uTexture;
uniform float uHasTexture;
uniform float uTextureScale;

out vec4 FragColor;

float attenuationOfLight(vec3 vPos, vec3 lightPos, float lightStartDist, float lightEndDist) {
    float distance = length(vPos-lightPos);
    float lightIntense;
    if(distance <= lightStartDist) {   //max helligkeit
        lightIntense = 1;
    } else if(distance >= lightEndDist) {
        lightIntense = 0;
    } else {
        lightIntense = max( (lightEndDist-distance)/(lightEndDist-lightStartDist), 0.0 );
    }
    return lightIntense;
}

vec3 calculateDiffuse(vec3 N, vec3 L, vec3 lightColor, float nDotl) {
    return lightColor * vec3(max(nDotl, 0.0)) ;
}

vec3 calculateSpecularBlinn(vec3 N, vec3 V, vec3 L, vec3 lightColor, float nDotl) {
    vec3 specular = vec3(0,0,0);

    vec3 lightAddCam = L+V;
    vec3 H = normalize( lightAddCam/sqrt(dot(lightAddCam,lightAddCam)) );
    specular =  lightColor * uReflectivity * vec3(max(pow(dot(N, H), uShininess+BLINN_ADD_SHINE), 0.0));

    return specular;
}

float shadows(vec4 shadowmapCoord) {
    vec3 ProjCoords = shadowmapCoord.xyz / shadowmapCoord.w;
    ProjCoords.x = 0.5 * ProjCoords.x + 0.5;
    ProjCoords.y = 0.5 * ProjCoords.y + 0.5;
    float z = 0.5 * ProjCoords.z + 0.5;

    /************ get texturesize *************/
    ivec2 texSize = textureSize(uShadowmap,0);
    float offset = 1.0/float(texSize.x);
    /******************************************/

    return texture(uShadowmap, vec3(vec2(ProjCoords) + vec2(offset, offset), z - SHADOW_BIAS));
}

float getAttenuation( vec4 shadowmapCoord ) {
    vec3 coord3 = 0.5 + 0.5 * shadowmapCoord.xyz / shadowmapCoord.w;
    coord3.z -= SHADOW_BIAS;
    float shadowmap_factor = texture(uShadowmap, coord3);
    return shadowmap_factor;
}

void main(void) {
    if(uIsLight > 0.9) {
        FragColor = vec4(uCurrentLightColor,1.0);
    } else {
        vec3 textureColor = vec3(texture(uTexture,vTextureCoords*uTextureScale));
        vec3 diffuse;
        vec3 specular;

        /************** DIFFUSE AND SPECULAR SUN CALCULATION ************************/
     /*   float nDotl = dot(sun_N,sun_L);

        diffuse = calculateDiffuse(sun_N, sun_L, uSunColor,nDotl);
        diffuse *= sunAttenuation;
        specular = calculateSpecularBlinn(sun_N, sun_V, sun_L, uSunColor, nDotl);
        specular *= sunAttenuation;
        */
        /*************************************************************************/

        float shadowFactor = 0;

        #ifdef LIGHTS
            float lightAttenuations = 1;

            for(int i=0 ; i<LIGHTS ; i++) {
                float nDotl = dot(light_N_array[i],light_L_array[i]);

                diffuse += calculateDiffuse(light_N_array[i], light_L_array[i], uLightColorArray[i],nDotl);
                specular += calculateSpecularBlinn(light_N_array[i], light_V_array[i], light_L_array[i], uLightColorArray[i], nDotl);

                lightAttenuations *= light_attenuation_array[i];

                shadowFactors[i] = shadows(uShadowViews[i]);
            }
        #endif

        vec3 diffuseFinal = diffuse;
        vec3 specularFinal = specular;

        //shadowFactor = shadows(vSunView);
        //float shadowFactor = smoothShadowsPCF(vSunView);

        //vec3 lighting = diffuseFinal*shadowFactor;
        vec3 lighting = diffuseFinal*shadowFactors[0]*shadowFactors[1];
        lighting += specularFinal*shadowFactor;
        //lighting *= sunAttenuation;
        lighting *= lightAttenuations;

        if(uHasTexture > 0.9) FragColor = vec4(textureColor*max(lighting,AMBILIGHT),1.0);
        else FragColor = vec4(lighting,1.0);
    }

}