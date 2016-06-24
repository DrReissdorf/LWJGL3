package engine;

import engine.Light.Light;
import engine.Light.Sun;
import engine.shader.MyShaderProgram;
import singleton.HolderSingleton;
import math.Mat4;
import math.Vec3;
import org.lwjgl.opengl.GL11;
import toolbox.FrameBufferFactory;
import toolbox.TextureFactory;
import toolbox.MeshCreator;
import util.Mesh;
import util.Texture;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL21.GL_SRGB;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {
  //  private final int MAX_TEX_RESOLUTION_WIDTH = 3840;
    //private final int MAX_TEX_RESOLUTION_HEIGHT = 2160;
    private final int MAX_TEX_RESOLUTION_WIDTH = 1280;
    private final int MAX_TEX_RESOLUTION_HEIGHT = 720;
    private final float renderDistance = 100;

    private final String shaderLocation = "src/engine/shader/";

    private MyShaderProgram geometryShader;
    private MyShaderProgram lightningShader;
    private MyShaderProgram shadowShader;
    private MyShaderProgram postProcessShader;
    private MyShaderProgram blurShader;


    private HolderSingleton holder;

    private int shadowFrameBuffer;
    private int shadowTextureID;
    private Texture shadowMapTexture;

    private int postProcessFramebuffer;
    private Texture postProcessTexture;

    private Texture brightObjectsTexture;

    private Texture[] pingPongTextures;
    private int[] pingPongFrameBuffers;

    private Mesh screenQuad;
    private Texture gBufferPositionTex, gBufferNormalReflectTex, gBufferColorSpecTex, gBufferSpecTex;
    private int gBufferID;

    private int windowWidth, windowHeight;

    public Renderer(int windowWidth, int windowHeight) {
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;

        holder = HolderSingleton.getInstance();

        shadowShader        = new MyShaderProgram( shaderLocation + "Shadowmap_vs.glsl",    shaderLocation + "Shadowmap_fs.glsl" );
        postProcessShader   = new MyShaderProgram( shaderLocation + "Postprocess_vs.glsl",  shaderLocation + "Postprocess_fs.glsl" );
        geometryShader      = new MyShaderProgram( shaderLocation + "Geometry_vs.glsl",  shaderLocation + "Geometry_fs.glsl" );
        lightningShader     = new MyShaderProgram( shaderLocation + "Lightning_vs.glsl",  shaderLocation + "Lightning_fs.glsl" );
        blurShader          = new MyShaderProgram( shaderLocation + "Blur_vs.glsl",  shaderLocation + "Blur_fs.glsl" );

        initShadows();
        initGeometryRendering();
        initPostProcessing();
        initBloomProcessing();
        //resizeTextures();

        screenQuad = new MeshCreator().createQuad();

        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public void renderScene(float dayTime, Vec3 backgroundColor, Camera mainCamera) {
        renderGeometry(mainCamera);
        renderShadowMap(mainCamera);
        renderLightning(dayTime, backgroundColor,mainCamera);
        //blurBloom();
        postProcessing();

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
            if(Vec3.length(Vec3.sub(gameObjectRoot.getPosition(),mainCamera.getPosition()))<renderDistance && gameObjectRoot.getModel()!=null && gameObjectRoot.getLight()==null && gameObjectRoot.getSun()==null) {
                shadowShader.setUniform("uModel", gameObjectRoot.getTranformationMatrix());
                gameObjectRoot.getModel().getMesh().draw(GL_TRIANGLES);
            }
        }

        glCullFace(GL_BACK);
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

                if(gameObjectRoot.getSun() != null) {
                    geometryShader.setUniform("uIsLight", 1);
                    geometryShader.setUniform("uLightColor", gameObjectRoot.getSun().getColor());
                } else if(gameObjectRoot.getLight() != null) {
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
                        geometryShader.setUniform("uIsLight", 0);
                        geometryShader.setUniform("uHasTexture", 0.0f);
                        geometryShader.setUniform("uShininess", 100.0f);
                        geometryShader.setUniform("uReflectivity", 0.5f);
                    }
                }

                model.getMesh().draw(GL_TRIANGLES);
            }

        }
    }

    public void renderLightning(float dayTime, Vec3 backgroundColor, Camera mainCamera) {
        glBindFramebuffer(GL_FRAMEBUFFER, postProcessFramebuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, windowWidth, windowHeight);

        ArrayList<Light> lights = new ArrayList<>();

        for (GameObjectRoot gameObjectRoot : holder.getGameObjectRoots()) {
            Light light = gameObjectRoot.getLight();
            if(light != null) {
                if (Vec3.length(Vec3.sub(gameObjectRoot.getPosition(), light.getPosition())) < renderDistance) {
                    lights.add(gameObjectRoot.getLight());
                }
            }
        }

        lightningShader.useProgram();
        lightningShader.setUniform("backgroundColor", backgroundColor);
        lightningShader.setUniform("cameraPos", mainCamera.getPosition());

        lightningShader.setUniform("uDayTime", dayTime);

        lightningShader.setUniform("uPositionTex", gBufferPositionTex);
        lightningShader.setUniform("uNormalTex", gBufferNormalReflectTex);
        lightningShader.setUniform("uColorSpecTex", gBufferColorSpecTex);
        lightningShader.setUniform("uSpecTex", gBufferSpecTex);

        lightningShader.setUniform("uLights",lights);

        Sun sun = holder.getSun();
        lightningShader.setUniform("uSun",sun);
        lightningShader.setUniform("uDirectionalLight",holder.getDirectionalLight());

        lightningShader.setUniform("uShadowmap", shadowMapTexture);

        screenQuad.draw();
    }

    public void blurBloom() {
        boolean first_iteration=true, horizontal = true;
        int blurAmount = 10;
        blurShader.useProgram();
        for (int i = 0; i < blurAmount; i++) {
            int textureIndex, frambufferIndex;
            if(horizontal)  {
                textureIndex = 1;
                frambufferIndex=0;
            }
            else {
                textureIndex = 0;
                frambufferIndex = 1;
            }
            horizontal = !horizontal;

            glBindFramebuffer(GL_FRAMEBUFFER, pingPongFrameBuffers[frambufferIndex]);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glViewport(0, 0, windowWidth, windowHeight);

            blurShader.setUniform("uHorizontal", textureIndex);

            if(first_iteration) blurShader.setUniform("uTexture", brightObjectsTexture);
            else blurShader.setUniform("uTexture", pingPongTextures[textureIndex]);
            screenQuad.draw();
            if (first_iteration) first_iteration = false;
        }
    }

    public void postProcessing() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, windowWidth, windowHeight);

        postProcessShader.useProgram();
        postProcessShader.setUniform("uTexture", postProcessTexture);
        postProcessShader.setUniform("uPingPongTexture", pingPongTextures[0]);
        screenQuad.draw();
    }

    private void resizeTextures() {
        glBindTexture( GL_TEXTURE_2D, postProcessTexture.getID() );
        glTexImage2D( GL_TEXTURE_2D, 0, GL_SRGB, windowWidth, windowHeight, 0, GL_RGB, GL_FLOAT, (FloatBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, brightObjectsTexture.getID() );
        glTexImage2D( GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight, 0, GL_RGB, GL_FLOAT, (FloatBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, pingPongTextures[0].getID() );
        glTexImage2D( GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight, 0, GL_RGB, GL_FLOAT, (FloatBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, pingPongTextures[1].getID() );
        glTexImage2D( GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight, 0, GL_RGB, GL_FLOAT, (FloatBuffer)null );
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

    public void initShadows() {
        shadowTextureID = TextureFactory.setupShadowMapTextureBuffer(holder.getShadowMapSize(), holder.getShadowMapSize());
        shadowMapTexture = new Texture(shadowTextureID);
        shadowFrameBuffer = FrameBufferFactory.setupShadowFrameBuffer(shadowTextureID);
    }

    private void initPostProcessing() {
        // To make texture resizable we need to assign it to 4k first, it just scales down
        postProcessTexture = TextureFactory.createSRGB_Texture(MAX_TEX_RESOLUTION_WIDTH,MAX_TEX_RESOLUTION_HEIGHT);
        brightObjectsTexture = TextureFactory.createRGB16F_Texture(windowWidth,windowHeight);
        postProcessFramebuffer = FrameBufferFactory.create2AttachmentFramebuffer(postProcessTexture, brightObjectsTexture, MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);
    }

    private void initBloomProcessing() {
        pingPongFrameBuffers = new int[2];
        pingPongTextures = new Texture[2];
        pingPongTextures[0] = TextureFactory.createRGB16F_Texture(MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);
        pingPongTextures[1] = TextureFactory.createRGB16F_Texture(MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);
        pingPongFrameBuffers[0] = FrameBufferFactory.create1AttachmentFramebuffer(pingPongTextures[0], MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);
        pingPongFrameBuffers[1] = FrameBufferFactory.create1AttachmentFramebuffer(pingPongTextures[1], MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);
    }

    private void initGeometryRendering() {
        gBufferColorSpecTex = TextureFactory.createRGB16F_Texture(MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);
        gBufferNormalReflectTex = TextureFactory.createRGB16F_Texture(MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);
        gBufferPositionTex = TextureFactory.createRGB16F_Texture(MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);
        gBufferSpecTex = TextureFactory.createRGB16F_Texture(MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT);
        gBufferID = FrameBufferFactory.setup_Gbuffer(MAX_TEX_RESOLUTION_WIDTH, MAX_TEX_RESOLUTION_HEIGHT , gBufferColorSpecTex, gBufferNormalReflectTex, gBufferPositionTex, gBufferSpecTex);
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

    private Mat4 createNormalMat(Mat4 modelMatrix) {
        return Mat4.inverse(modelMatrix).transpose();
    }
}
