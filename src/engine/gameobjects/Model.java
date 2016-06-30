package engine.gameobjects;


import engine.gameobjects.GameObjectRoot;
import engine.gameobjects.GameObject;
import engine.gameobjects.ModelTexture;
import util.Mesh;

public class Model extends GameObject {
    private Mesh mesh;
    private ModelTexture modelTexture;
    private float textureScale;

    public Model(GameObjectRoot root, Mesh mesh, ModelTexture modelTexture, float textureScale, float scale) {
        super(root);
        this.mesh = mesh;
        this.modelTexture = modelTexture;
        this.textureScale = textureScale;
        root.setScale(scale);
    }

    public Mesh getMesh() {
        return mesh;
    }

    public ModelTexture getModelTexture() {
        return modelTexture;
    }

    public float getTextureScale() {
        return textureScale;
    }
}
