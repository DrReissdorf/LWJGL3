package singleton;

import engine.gameobjects.Camera;
import engine.gameobjects.Light.DirectionalLight;
import engine.gameobjects.Light.Light;
import engine.gameobjects.Light.Sun;
import engine.gameobjects.GameObjectRoot;
import engine.gameobjects.Model;
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

    private ArrayList<Light> lights;
    private ArrayList<GameObjectRoot> entities;
    private ArrayList<Mesh> meshes;
    private ArrayList<Model> models;
    private ArrayList<GameObjectRoot> gameObjectRoots;
    private ArrayList<ModelTexture> modelTextures;
    private ArrayList<Texture> textures;
    private Sun sun;
    private DirectionalLight directionalLight;

    private float mainCameraFov = 70.0f;
    private float mainCameraNear   = 0.01f;
    private float mainCameraFar    = 500.0f;

    private HolderSingleton() {
        mainCamera = new Camera(new Vec3(0.0f, 2.0f, -3.0f), 0.1f,mainCameraFov, WIDTH, HEIGHT,mainCameraNear,mainCameraFar,3f);
        lights = new ArrayList<>();
        entities = new ArrayList<>();
        meshes = new ArrayList<>();
        models = new ArrayList<>();
        modelTextures = new ArrayList<>();
        textures = new ArrayList<>();
        gameObjectRoots = new ArrayList<>();
    }

    public static HolderSingleton getInstance() {
        if(holderSingleton == null) holderSingleton = new HolderSingleton();
        return holderSingleton;
    }

    public void addEntity(GameObjectRoot entity) {
        entities.add(entity);
    }

    public GameObjectRoot getEntity(int position) {
        return entities.get(position);
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

    public void addModel(Model model) {
        models.add(model);
    }

    public Model getModel(int position) {
        return models.get(position);
    }

    public void addTexture(Texture texture) {
        textures.add(texture);
    }

    public Texture getTexture(int position) {
        return textures.get(position);
    }

    public ArrayList<GameObjectRoot> getEntityArrayList() {
        return entities;
    }

    public int getShadowMapSize() {
        return shadowMapSize;
    }

    public void setShadowMapSize(int shadowMapSize) {
        this.shadowMapSize = shadowMapSize;
    }

    public void addLight(Light light) {
        lights.add(light);
    }

    public Light getLight(int position) {
        return lights.get(position);
    }

    public ArrayList<GameObjectRoot> getEntities() {
        return entities;
    }

    public ArrayList<Mesh> getMeshes() {
        return meshes;
    }

    public ArrayList<Model> getModels() {
        return models;
    }

    public ArrayList<ModelTexture> getModelTextures() {
        return modelTextures;
    }

    public ArrayList<Texture> getTextures() {
        return textures;
    }

    public ArrayList<Light> getLights() {
        return lights;
    }

    public ArrayList<GameObjectRoot> getGameObjectRoots() {
        return gameObjectRoots;
    }

    public GameObjectRoot getGameObjectRoot(int position) {
        return gameObjectRoots.get(position);
    }

    public void addGameObjectRoot(GameObjectRoot gameObjectRoot) {
        gameObjectRoots.add(gameObjectRoot);
    }

    public void setSun(Sun sun) {
        this.sun = sun;
    }

    public Sun getSun() {
        return sun;
    }

    public DirectionalLight getDirectionalLight() {
        return directionalLight;
    }

    public void setDirectionalLight(DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;
    }

    public Camera getMainCamera() {
        return mainCamera;
    }
}
