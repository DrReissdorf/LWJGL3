package engine.graphics;

import engine.gameobjects.Light.Sun;
import engine.gameobjects.Camera;
import engine.gameobjects.GameObjectRoot;
import engine.gameobjects.Light.Light;
import engine.gameobjects.Model;
import engine.logic.Singleplayer;
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
    private final int MAX_TEX_RESOLUTION_WIDTH = 3840;
    private final int MAX_TEX_RESOLUTION_HEIGHT = 2160;
    private final float renderDistance = 50;

    private final String shaderLocation = "resources/shader/";

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

    public Renderer() {
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
        resizeTextures();

        screenQuad = new MeshCreator().createQuad();

        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public void renderScene(float dayTime, Vec3 backgroundColor) {
        renderGeometry();
        renderShadowMap();
        renderLightning(dayTime, backgroundColor);
        blurBloom();
        postProcessing();

    }

    private void renderShadowMap()  {
        glViewport(0, 0, holder.getShadowMapSize(), holder.getShadowMapSize());
        glBindFramebuffer( GL_FRAMEBUFFER, shadowFrameBuffer);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        glCullFace(GL_FRONT); //fix peter-panning

        Camera mainCamera = holder.getMainCamera();
        Sun sun = holder.getSun();

        shadowShader.useProgram();

        Mat4 mv = Mat4.mul(sun.getProjectionMatrix(), sun.getViewMatrix());

        for (GameObjectRoot gameObjectRoot : holder.getGameObjectRootTests()) {
            for(Model modelTest : gameObjectRoot.getAllModels()) {
                if(Vec3.length(Vec3.sub(gameObjectRoot.getPosition(),mainCamera.getRoot().getPosition()))<renderDistance) {
                    shadowShader.setUniform("uProjectionViewModel", Mat4.mul(mv,gameObjectRoot.getTranformationMatrix()));
                    modelTest.getMesh().draw(GL_TRIANGLES);
                }
            }
        }

        glCullFace(GL_BACK);
    }

    public void renderGeometry() {
        glBindFramebuffer(GL_FRAMEBUFFER, gBufferID);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glCullFace(GL_BACK);

        Camera mainCamera = holder.getMainCamera();

        geometryShader.useProgram();
        geometryShader.setUniform("uProjectionView", Mat4.mul(mainCamera.getProjectionMatrix(), mainCamera.getViewMatrix()));

        for(GameObjectRoot gameObjectRoot : holder.getGameObjectRootTests()) {
            for(Model modelTest : gameObjectRoot.getAllModels()) {
                if(Vec3.length(Vec3.sub(gameObjectRoot.getPosition(),mainCamera.getRoot().getPosition())) < renderDistance) {
                    geometryShader.setUniform("uModel",     gameObjectRoot.getTranformationMatrix());
                    geometryShader.setUniform("uNormalMat", createNormalMat(gameObjectRoot.getTranformationMatrix()));

                    Model model = modelTest;

                    if(model.getModelTexture() != null) {
                        geometryShader.setUniform("uHasTexture", 1.0f);
                        geometryShader.setUniform("uTexture",       model.getModelTexture().getTexture());
                        geometryShader.setUniform("uShininess",     model.getModelTexture().getShininess());
                        geometryShader.setUniform("uReflectivity",  model.getModelTexture().getReflectivity());
                        geometryShader.setUniform("uTextureScale",  model.getTextureScale());
                    } else {
                        geometryShader.setUniform("uHasTexture", 0.0f);
                        geometryShader.setUniform("uShininess", 100.0f);
                        geometryShader.setUniform("uReflectivity", 0.5f);
                    }

                    model.getMesh().draw(GL_TRIANGLES);
                }
            }
        }
    }

    public void renderLightning(float dayTime, Vec3 backgroundColor) {
        glBindFramebuffer(GL_FRAMEBUFFER, postProcessFramebuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, Singleplayer.WIDTH, Singleplayer.HEIGHT);

        ArrayList<Light> lights = new ArrayList<>();
        for (GameObjectRoot gameObjectRoot : holder.getGameObjectRootTests()) {
            for (Light lightTest : gameObjectRoot.getAllLights()) {
                if (Vec3.length(Vec3.sub(gameObjectRoot.getPosition(), lightTest.getPosition())) < renderDistance) {
                    lights.add(lightTest);
                }
            }
        }

        Camera mainCamera = holder.getMainCamera();

        lightningShader.useProgram();
        lightningShader.setUniform("backgroundColor", backgroundColor);
        lightningShader.setUniform("cameraPos", mainCamera.getRoot().getPosition());
        lightningShader.setUniform("uPingPongTexture", pingPongTextures[0]);

        lightningShader.setUniform("uDayTime", dayTime);

        lightningShader.setUniform("uPositionTex", gBufferPositionTex);
        lightningShader.setUniform("uNormalTex", gBufferNormalReflectTex);
        lightningShader.setUniform("uColorSpecTex", gBufferColorSpecTex);
        lightningShader.setUniform("uSpecTex", gBufferSpecTex);

        lightningShader.setUniform("uLights",lights,true);

        lightningShader.setUniform("uSun", holder.getSun());
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
            glViewport(0, 0, Singleplayer.WIDTH, Singleplayer.HEIGHT);

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
        glViewport(0, 0, Singleplayer.WIDTH, Singleplayer.HEIGHT);

        postProcessShader.useProgram();
        postProcessShader.setUniform("uTexture", postProcessTexture);
        postProcessShader.setUniform("uPingPongTexture", pingPongTextures[0]);
        screenQuad.draw();
    }

    private void resizeTextures() {
        glBindTexture( GL_TEXTURE_2D, postProcessTexture.getID() );
        glTexImage2D( GL_TEXTURE_2D, 0, GL_SRGB, Singleplayer.WIDTH, Singleplayer.HEIGHT, 0, GL_RGB, GL_FLOAT, (FloatBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, brightObjectsTexture.getID() );
        glTexImage2D( GL_TEXTURE_2D, 0, GL_RGB16F, Singleplayer.WIDTH, Singleplayer.HEIGHT, 0, GL_RGB, GL_FLOAT, (FloatBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, pingPongTextures[0].getID() );
        glTexImage2D( GL_TEXTURE_2D, 0, GL_RGB16F, Singleplayer.WIDTH, Singleplayer.HEIGHT, 0, GL_RGB, GL_FLOAT, (FloatBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, pingPongTextures[1].getID() );
        glTexImage2D( GL_TEXTURE_2D, 0, GL_RGB16F, Singleplayer.WIDTH, Singleplayer.HEIGHT, 0, GL_RGB, GL_FLOAT, (FloatBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, gBufferColorSpecTex.getID() );
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, Singleplayer.WIDTH, Singleplayer.HEIGHT, 0, GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer) null);
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, gBufferNormalReflectTex.getID() );
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, Singleplayer.WIDTH, Singleplayer.HEIGHT, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, gBufferPositionTex.getID() );
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, Singleplayer.WIDTH, Singleplayer.HEIGHT, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        glBindTexture( GL_TEXTURE_2D, 0 );

        glBindTexture( GL_TEXTURE_2D, gBufferSpecTex.getID() );
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, Singleplayer.WIDTH, Singleplayer.HEIGHT, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
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
        brightObjectsTexture = TextureFactory.createRGB16F_Texture(MAX_TEX_RESOLUTION_WIDTH,MAX_TEX_RESOLUTION_HEIGHT);
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

    /*private int[] getClosestLightsIndices(Vec3 currentPos) {
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
    } */

    private Mat4 createNormalMat(Mat4 modelMatrix) {
        return Mat4.inverse(modelMatrix).transpose();
    }
}
