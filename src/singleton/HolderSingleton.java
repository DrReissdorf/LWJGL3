package singleton;

import engine.gameobjects.Light.Sun;
import engine.gameobjects.Camera;
import engine.gameobjects.GameObjectRoot;
import engine.gameobjects.ModelTexture;
import math.Vec3;
import util.Mesh;
import util.Texture;

import java.util.ArrayList;

import static engine.logic.Singleplayer.HEIGHT;
import static engine.logic.Singleplayer.WIDTH;

public class HolderSingleton {
    private static HolderSingleton holderSingleton;

    private int shadowMapSize = 2048;

    private Camera mainCamera;

    private ArrayList<GameObjectRoot> gameObjectRootTests;

    private ArrayList<Mesh> meshes;

    private ArrayList<ModelTexture> modelTextures;
    private ArrayList<Texture> textures;
    private Sun sun;

    private float mainCameraFov = 70.0f;
    private float mainCameraNear   = 0.01f;
    private float mainCameraFar    = 500.0f;

    private HolderSingleton() {
        mainCamera = new Camera(new GameObjectRoot(new Vec3(0.0f, 2.0f, -3.0f)),mainCameraFov, WIDTH, HEIGHT,mainCameraNear,mainCameraFar,3f);
        meshes = new ArrayList<>();
        modelTextures = new ArrayList<>();
        textures = new ArrayList<>();
        gameObjectRootTests = new ArrayList<>();
    }

    public static HolderSingleton getInstance() {
        if(holderSingleton == null) holderSingleton = new HolderSingleton();
        return holderSingleton;
    }

    public void addMesh(Mesh mesh) {
        meshes.add(mesh);
    }

    public Mesh getMesh(int position) {
        return meshes.get(position);
    }

    public void addModelTexture(ModelTexture modelTexture) {
        modelTextures.add(modelTexture);
    }

    public ModelTexture getModelTexture(int position) {
        return modelTextures.get(position);
    }

    public void addTexture(Texture texture) {
        textures.add(texture);
    }

    public Texture getTexture(int position) {
        return textures.get(position);
    }

    public int getShadowMapSize() {
        return shadowMapSize;
    }

    public void setShadowMapSize(int shadowMapSize) {
        this.shadowMapSize = shadowMapSize;
    }

    public void setSun(Sun sun) {
        this.sun = sun;
    }

    public Sun getSun() {
        return sun;
    }

    public Camera getMainCamera() {
        return mainCamera;
    }

    public ArrayList<GameObjectRoot> getGameObjectRootTests() {
        return gameObjectRootTests;
    }
}
