package engine;

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
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

public class Renderer {
    private final String shaderLocation = "src/engine/shader/";
    private MyShaderProgram generalShader;
    private MyShaderProgram shadowShader;
    private MyShaderProgram postProcessShader;

    private HolderSingleton holder;

    private ArrayList<Vec3> lightPositions;
    private ArrayList<Vec3> lightColors;
    private ArrayList<Float> lightRanges;
    private ArrayList<Mat4> lightProjections;
    private ArrayList<Mat4> lightViews;

    private int shadowFrameBuffer;
    private int shadowTextureID;
    private Texture shadowMapTexture;

    private int postProcessTextureID;
    private int postProcessFrameBuffer;
    private Texture postProcessTexture;
    private Mesh postProcessQuad;

    private int windowWidth, windowHeight;
    private final Vec3 backgroundColor = RgbToFloat(200,200,200);

    public Renderer(int windowWidth, int windowHeight) {
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;

        holder = HolderSingleton.getInstance();

        generalShader       = new MyShaderProgram( shaderLocation + "Color_vs.glsl",        shaderLocation + "Color_fs.glsl" );
        shadowShader        = new MyShaderProgram( shaderLocation + "Shadowmap_vs.glsl",    shaderLocation + "Shadowmap_fs.glsl" );
        postProcessShader   = new MyShaderProgram( shaderLocation + "Postprocess_vs.glsl",  shaderLocation + "Postprocess_fs.glsl" );

        initShadows();
        initPostProcessing();

        postProcessQuad = new MeshCreator().createQuad();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);

        lightPositions = new ArrayList<>();
        lightColors = new ArrayList<>();
        lightRanges = new ArrayList<>();
        lightProjections = new ArrayList<>();
        lightViews = new ArrayList<>();
    }

    public void renderScene(Camera mainCamera) {
        renderShadowMap();
        renderModels(mainCamera);
        postProcess();
    }

    public void renderModels(Camera mainCamera) {

        updateArrayLists();

        glBindFramebuffer(GL_FRAMEBUFFER, postProcessFrameBuffer);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(backgroundColor.x, backgroundColor.y, backgroundColor.z, 1.0f);
        glViewport( 0, 0, windowWidth, windowHeight );
        glCullFace(GL_BACK);

        generalShader.useProgram();
        generalShader.setUniform("uView",       mainCamera.getViewMatrix() );
        generalShader.setUniform("uProjection", mainCamera.getProjectionMatrix() );

        generalShader.setUniform("uLightProjections", convertMat4ArrayListToMat4Array(lightProjections));
        generalShader.setUniform("uLightViews", convertMat4ArrayListToMat4Array(lightViews) );
        generalShader.setUniform("uLightPosArray", convertVec3ArrayListToVec3Array(lightPositions));
        generalShader.setUniform("uLightColorArray", convertVec3ArrayListToVec3Array(lightColors));
        generalShader.setUniform("uLightRangesArray", convertFloatArrayListToFloatArray(lightRanges));

        generalShader.setUniform("uInvertedUView", mainCamera.getViewMatrix().inverse());

        generalShader.setUniform("uShadowmap", shadowMapTexture);

        for( Model model : holder.getModels()) {
            generalShader.setUniform("uModel", model.getTranformationMatrix());
            generalShader.setUniform("uNormalMat", createNormalMat(model.getTranformationMatrix()));

        /*    if(model.getLight() != null) {
                generalShader.setUniform("uIsLight",1f);
                generalShader.setUniform("uCurrentLightColor", model.getLight().getColor());
            }
            else */generalShader.setUniform("uIsLight",0f);

            if(model.getModelTexture() != null) {
                generalShader.setUniform("uHasTexture", 1.0f);
                generalShader.setUniform("uTexture", model.getModelTexture().getTexture());
                generalShader.setUniform("uShininess", model.getModelTexture().getShininess());
                generalShader.setUniform("uReflectivity", model.getModelTexture().getReflectivity());
                generalShader.setUniform("uTextureScale", model.getTextureScale());
            } else {
                generalShader.setUniform("uHasTexture", 0.0f);
                generalShader.setUniform("uShininess", 100f);
                generalShader.setUniform("uReflectivity", 1f);
                generalShader.setUniform("uTextureScale", 1);
            }

            model.getMesh().draw(GL_TRIANGLES );
        }
    }

    private void updateArrayLists() {
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
    }

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

    private Mat4 createNormalMat(Mat4 modelMatrix) {
        return Mat4.inverse(modelMatrix).transpose();
    }

    private void renderShadowMap()  {
        glViewport(0, 0, holder.getShadowMapSize(), holder.getShadowMapSize());
        glBindFramebuffer( GL_FRAMEBUFFER, shadowFrameBuffer);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        glCullFace(GL_BACK);
        shadowShader.useProgram();

        for(Light light : holder.getLights()) {
        //TestLight light = holder.getLight(1);
            shadowShader.setUniform("uView", light.getViewMatrix());
            shadowShader.setUniform("uProjection", light.getProjectionMatrix());

            for (Model model : holder.getModels()) {
                shadowShader.setUniform("uModel", model.getTranformationMatrix());
                model.getMesh().draw(GL_TRIANGLES);
            }
        }
    }

    public void postProcess() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glClearColor(backgroundColor.x,backgroundColor.y,backgroundColor.z,1f);
        glViewport(0, 0, windowWidth, windowHeight);
        glCullFace(GL_BACK);

        postProcessShader.useProgram();
        postProcessShader.setUniform("uTexture", postProcessTexture);
        postProcessQuad.draw();
    }

    private void resizeTexture(int textureID, int width, int height) {
        glBindTexture( GL_TEXTURE_2D, textureID );
        glTexImage2D( GL_TEXTURE_2D, 0, EXTTextureSRGB.GL_SRGB_EXT, width, height,
                0, GL_RGB, GL_FLOAT, (FloatBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );
    }

    public void onResize( int width, int height ) {
        windowWidth  = width;
        windowHeight = height;
        resizeTexture(postProcessTextureID, width, height);
    }

    public void initShadows() {
        shadowTextureID = FrameBufferTextureFactory.setupShadowMapTextureBuffer(holder.getShadowMapSize(), holder.getShadowMapSize());
        shadowMapTexture = new Texture(shadowTextureID);
        shadowFrameBuffer = FrameBufferFactory.setupShadowFrameBuffer(shadowTextureID);
    }

    private void initPostProcessing() {
        // To make texture resizable we need to assign it to 4k first, it just scales down
        postProcessTextureID = FrameBufferTextureFactory.setupPostProcessTextureBuffer(3840, 2160);
        postProcessTexture = new Texture(postProcessTextureID);
        postProcessFrameBuffer = FrameBufferFactory.setupPostProcessFrameBuffer(postProcessTextureID, 3840, 2160);
        resizeTexture(postProcessTextureID, windowWidth, windowHeight);
    }

    public Vec3 RgbToFloat(int r, int g, int b) {
        return new Vec3(r/255f,g/255f,b/255f);
    }
}
