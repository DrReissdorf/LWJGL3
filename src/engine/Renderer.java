package engine;

import engine.Light.Light;
import engine.Light.Sun;
import engine.shader.MyShaderProgram;
import singleton.HolderSingleton;
import math.Mat4;
import math.Vec3;
import org.lwjgl.opengl.EXTTextureSRGB;
import org.lwjgl.opengl.GL11;
import toolbox.FrameBufferFactory;
import toolbox.FrameBufferTextureFactory;
import toolbox.MeshCreator;
import util.Mesh;
import util.Texture;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {
    private final int MAX_TEX_RESOLUTION_WIDTH = 3840;
    private final int MAX_TEX_RESOLUTION_HEIGHT = 2160;
    private final float renderDistance = 50;

    private final String shaderLocation = "src/engine/shader/";

    private MyShaderProgram geometryShader;
    private MyShaderProgram lightningShader;

    private MyShaderProgram shadowShader;
    private MyShaderProgram postProcessShader;

    private HolderSingleton holder;

    private Vec3[] lightPositionArray;
    private Vec3[] lightColorArray;
    private float[] lightRangeArray;
    private Mat4[] lightProjectionArray;
    private Mat4[] lightViewArray;

    private int shadowFrameBuffer;
    private int shadowTextureID;
    private Texture shadowMapTexture;

    private int postProcessTextureID;
    private int postProcessFrameBuffer;
    private Texture postProcessTexture;
    private Mesh postProcessQuad;

    private Mesh lightningQuad;
    private Texture gBufferPositionTex, gBufferNormalReflectTex, gBufferColorSpecTex, gBufferSpecTex;
    private int gBufferID;

    private int windowWidth, windowHeight;
    private final Vec3 backgroundColor = RgbToFloat(54,155,255);

    public Renderer(int windowWidth, int windowHeight) {
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;

        holder = HolderSingleton.getInstance();

        shadowShader        = new MyShaderProgram( shaderLocation + "Shadowmap_vs.glsl",    shaderLocation + "Shadowmap_fs.glsl" );
        postProcessShader   = new MyShaderProgram( shaderLocation + "Postprocess_vs.glsl",  shaderLocation + "Postprocess_fs.glsl" );
        geometryShader      = new MyShaderProgram( shaderLocation + "Geometry_vs.glsl",  shaderLocation + "Geometry_fs.glsl" );
        lightningShader     = new MyShaderProgram( shaderLocation + "Lightning_vs.glsl",  shaderLocation + "Lightning_fs.glsl" );

        initShadows();
        initGeometryRendering();
        initPostProcessing();
        resizeTextures();

        postProcessQuad = new MeshCreator().createQuad();
        lightningQuad = new MeshCreator().createQuad();

        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public void renderScene(Camera mainCamera) {
        renderGeometry(mainCamera);
        renderShadowMap(mainCamera);
        renderLightning(mainCamera);
        postProcess();
    }

    public void renderGeometry(Camera mainCamera) {
        glBindFramebuffer(GL_FRAMEBUFFER, gBufferID);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glCullFace(GL_BACK);

        geometryShader.useProgram();
        geometryShader.setUniform("uView",       mainCamera.getViewMatrix());
        geometryShader.setUniform("uProjection", mainCamera.getProjectionMatrix());

        for(GameObjectRoot gameObjectRoot : holder.getGameObjectRoots()) {
            if(Vec3.length(Vec3.sub(gameObjectRoot.getPosition(),mainCamera.getPosition())) < renderDistance && gameObjectRoot.getModel()!=null) {
                geometryShader.setUniform("uModel",     gameObjectRoot.getTranformationMatrix());
                geometryShader.setUniform("uNormalMat", createNormalMat(gameObjectRoot.getTranformationMatrix()));

                Model model = gameObjectRoot.getModel();

                if(gameObjectRoot.getLight() != null) {
                    geometryShader.setUniform("uIsLight", 1);
                    geometryShader.setUniform("uLightColor", gameObjectRoot.getLight().getColor());
                } else {
                    if(model.getModelTexture() != null) {
                        geometryShader.setUniform("uIsLight", 0);
                        geometryShader.setUniform("uHasTexture", 1.0f);
                        geometryShader.setUniform("uTexture",       model.getModelTexture().getTexture());
                        geometryShader.setUniform("uShininess",     model.getModelTexture().getShininess());
                        geometryShader.setUniform("uReflectivity",  model.getModelTexture().getReflectivity());
                        geometryShader.setUniform("uTextureScale",  model.getTextureScale());
                    } else {
                        geometryShader.setUniform("uHasTexture", 0.0f);
                        geometryShader.setUniform("uShininess", 100.0f);
                        geometryShader.setUniform("uReflectivity", 1.0f);
                    }
                }

                model.getMesh().draw(GL_TRIANGLES);
            }

        }
    }

    public void renderLightning(Camera mainCamera) {
        glBindFramebuffer(GL_FRAMEBUFFER, postProcessFrameBuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //glClearColor(0.0f,0.0f,0.0f,1f);
        //glClearColor(backgroundColor.x,backgroundColor.y,backgroundColor.z,1f);
        glViewport(0, 0, windowWidth, windowHeight);

        ArrayList<Light> lights = new ArrayList<>();

      /*  lightPositionArray = new Vec3[lights.size()];
        lightColorArray = new Vec3[lights.size()];
        lightRangeArray = new float[lights.size()];
        lightViewArray = new Mat4[lights.size()];
        lightProjectionArray = new Mat4[lights.size()];

        for(int i=0 ; i<lights.size() ; i++) {
            lightPositionArray[i] = lights.get(i).getPosition();
            lightColorArray[i] = lights.get(i).getColor();
            lightRangeArray[i] = lights.get(i).getRange();
            lightViewArray[i] = lights.get(i).getViewMatrix();
            lightProjectionArray[i] = lights.get(i).getProjectionMatrix();
        } */

        for (GameObjectRoot gameObjectRoot : holder.getGameObjectRoots()) {
            Light light = gameObjectRoot.getLight();
            if(light != null) {
                if (Vec3.length(Vec3.sub(gameObjectRoot.getPosition(), light.getPosition())) < renderDistance) {
                    lights.add(gameObjectRoot.getLight());
                }
            }
        }

        if(lights.size() > 0) {
            lightPositionArray = new Vec3[lights.size()];
            lightColorArray = new Vec3[lights.size()];
            lightRangeArray = new float[lights.size()];
            lightViewArray = new Mat4[lights.size()];
            lightProjectionArray = new Mat4[lights.size()];

            for(int i=0 ; i<lights.size() ; i++) {
                lightPositionArray[i] = lights.get(i).getPosition();
                lightColorArray[i] = lights.get(i).getColor();
                lightRangeArray[i] = lights.get(i).getRange();
                lightViewArray[i] = lights.get(i).getViewMatrix();
                lightProjectionArray[i] = lights.get(i).getProjectionMatrix();
            }
        }

        lightningShader.useProgram();
        lightningShader.setUniform("backgroundColor", backgroundColor);
        lightningShader.setUniform("cameraPos", mainCamera.getPosition());

        lightningShader.setUniform("uPositionTex", gBufferPositionTex);
        lightningShader.setUniform("uNormalTex", gBufferNormalReflectTex);
        lightningShader.setUniform("uColorSpecTex", gBufferColorSpecTex);
        lightningShader.setUniform("uSpecTex", gBufferSpecTex);

        lightningShader.setUniform("uLightProjections", lightProjectionArray);
        lightningShader.setUniform("uLightViews",       lightViewArray);
        lightningShader.setUniform("uLightPosArray",    lightPositionArray);
        lightningShader.setUniform("uLightColorArray",  lightColorArray);
        lightningShader.setUniform("uLightRangesArray", lightRangeArray);

        Sun sun = holder.getSun();
        lightningShader.setUniform("uSunProjection", sun.getProjectionMatrix());
        lightningShader.setUniform("uSunView", sun.getViewMatrix());
        lightningShader.setUniform("uSunDirection", sun.getPosition());
        lightningShader.setUniform("uSunColor", sun.getColor());

        lightningShader.setUniform("uShadowmap", shadowMapTexture);

        lightningQuad.draw();
    }



    private Mat4 createNormalMat(Mat4 modelMatrix) {
        return Mat4.inverse(modelMatrix).transpose();
    }

    private void renderShadowMap(Camera mainCamera)  {
        glViewport(0, 0, holder.getShadowMapSize(), holder.getShadowMapSize());
        glBindFramebuffer( GL_FRAMEBUFFER, shadowFrameBuffer);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        glCullFace(GL_FRONT);

        shadowShader.useProgram();

        Sun sun = holder.getSun();

        shadowShader.setUniform("uProjection", sun.getProjectionMatrix());
        shadowShader.setUniform("uView", sun.getViewMatrix());

        for (GameObjectRoot gameObjectRoot : holder.getGameObjectRoots()) {
            if(Vec3.length(Vec3.sub(gameObjectRoot.getPosition(),mainCamera.getPosition()))<renderDistance && gameObjectRoot.getModel()!=null) {
                shadowShader.setUniform("uModel", gameObjectRoot.getTranformationMatrix());
                gameObjectRoot.getModel().getMesh().draw(GL_TRIANGLES);
            }
        }

        glCullFace(GL_BACK);
    }

    public void postProcess() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, windowWidth, windowHeight);

        postProcessShader.useProgram();
        postProcessShader.setUniform("uTexture", postProcessTexture);
        postProcessQuad.draw();
    }

    private void resizeTextures() {
        glBindTexture( GL_TEXTURE_2D, postProcessTexture.getID() );
        glTexImage2D( GL_TEXTURE_2D, 0, EXTTextureSRGB.GL_SRGB_EXT, windowWidth, windowHeight, 0, GL_RGB, GL_FLOAT, (FloatBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, gBufferColorSpecTex.getID() );
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer) null);
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, gBufferNormalReflectTex.getID() );
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, gBufferPositionTex.getID() );
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, gBufferSpecTex.getID() );
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        glBindTexture( GL_TEXTURE_2D, 0 );
    }

    public void onResize( int width, int height ) {
        windowWidth  = width;
        windowHeight = height;

        resizeTextures();
    }

    public void initShadows() {
        shadowTextureID = FrameBufferTextureFactory.setupShadowMapTextureBuffer(holder.getShadowMapSize(), holder.getShadowMapSize());
        //shadowTextureID = FrameBufferTextureFactory.newShadowTex(holder.getShadowMapSize(), holder.getShadowMapSize());
        shadowMapTexture = new Texture(shadowTextureID);
        shadowFrameBuffer = FrameBufferFactory.setupShadowFrameBuffer(shadowTextureID);
    }

    private void initPostProcessing() {
        // To make texture resizable we need to assign it to 4k first, it just scales down
        postProcessTextureID = FrameBufferTextureFactory.setupPostProcessTextureBuffer(MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);
        postProcessTexture = new Texture(postProcessTextureID);
        postProcessFrameBuffer = FrameBufferFactory.setupPostProcessFrameBuffer(postProcessTextureID, MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);
    }

    private void initGeometryRendering() {
        int[] gBufferTextureIDs = FrameBufferTextureFactory.setupGeometryTextures(MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);

        gBufferColorSpecTex = new Texture(gBufferTextureIDs[0]);
        gBufferNormalReflectTex = new Texture(gBufferTextureIDs[1]);
        gBufferPositionTex = new Texture(gBufferTextureIDs[2]);
        gBufferSpecTex = new Texture(gBufferTextureIDs[3]);

        gBufferID = FrameBufferFactory.setup_Gbuffer(MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT , gBufferColorSpecTex, gBufferNormalReflectTex, gBufferPositionTex, gBufferSpecTex);
    }

    public Vec3 RgbToFloat(int r, int g, int b) {
        return new Vec3(r/255f,g/255f,b/255f);
    }

    private int[] getClosestLightsIndices(Vec3 currentPos) {
        Light closest = null;
        Light sndClosest = null;

        float closestFloat = Float.MAX_VALUE;
        float secondClosestFloat = Float.MAX_VALUE;
        float temp;

        for(Light light : holder.getLights()) {
            temp = Vec3.length(Vec3.sub(light.getPosition(),currentPos));

            if(temp < closestFloat) {
                sndClosest = closest;
                secondClosestFloat = closestFloat;
                closest = light;
                closestFloat = temp;
            } else if (temp < secondClosestFloat) {
                sndClosest = light;
                secondClosestFloat = temp;
            }
        }

        int[] lightIndices = {holder.getLights().indexOf(closest), holder.getLights().indexOf(sndClosest)};
        return lightIndices;
    }

   /* private void updateArrayLists() {
        lightPositions.clear();
        lightColors.clear();
        lightRanges.clear();
        lightViews.clear();
        lightProjections.clear();

        for(Light light : holder.getLights()) {
            lightPositions.add(light.getPosition());
            lightColors.add(light.getColor());
            lightRanges.add(light.getRange());
            lightViews.add(light.getViewMatrix());
            lightProjections.add(light.getProjectionMatrix());
        }
    } */

    private Mat4[] convertMat4ArrayListToMat4Array(ArrayList<Mat4> mats) {
        Mat4[] matArray = new Mat4[mats.size()];
        for(int i=0 ; i<mats.size() ; i++) {
            matArray[i] = mats.get(i);
        }

        return matArray;
    }

    private float[] convertFloatArrayListToFloatArray(ArrayList<Float> floats) {
        float[] floatArray = new float[floats.size()];
        for(int i=0 ; i<floats.size() ; i++) {
            floatArray[i] = floats.get(i);
        }

        return floatArray;
    }

    private Vec3[] convertVec3ArrayListToVec3Array(ArrayList<Vec3> vectors) {
        Vec3[] vecArray = new Vec3[vectors.size()];
        for(int i=0 ; i<vectors.size() ; i++) {
            vecArray[i] = vectors.get(i);
        }

        return vecArray;
    }
}
