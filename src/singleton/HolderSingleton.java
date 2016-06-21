package singleton;

import engine.*;
import engine.Light.Light;
import engine.Light.Sun;
import util.Mesh;
import util.Texture;

import java.util.ArrayList;

public class HolderSingleton {
    private int shadowMapSize = 2048;

    private static HolderSingleton holderSingleton;
    private ArrayList<Light> lights;
    private ArrayList<GameObject> entities;
    private ArrayList<Mesh> meshes;
    private ArrayList<Model> models;
    private ArrayList<ModelTexture> modelTextures;
    private ArrayList<Texture> textures;
    private Sun sun;

    private HolderSingleton() {
        lights = new ArrayList<>();
        entities = new ArrayList<>();
        meshes = new ArrayList<>();
        models = new ArrayList<Model>();
        modelTextures = new ArrayList<>();
        textures = new ArrayList<>();
    }

    public static HolderSingleton getInstance() {
        if(holderSingleton == null) holderSingleton = new HolderSingleton();
        return holderSingleton;
    }

    public void addEntity(GameObject entity) {
        entities.add(entity);
    }

    public GameObject getEntity(int position) {
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

    public ArrayList<GameObject> getEntityArrayList() {
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

    public ArrayList<GameObject> getEntities() {
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

    public void setSun(Sun sun) {
        this.sun = sun;
    }

    public Sun getSun() {
        return sun;
    }
}
