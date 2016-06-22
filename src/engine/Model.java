package engine;


import math.Vec3;
import util.Mesh;

public class Model {
    private Mesh mesh;
    private ModelTexture modelTexture;
    private float textureScale;

    public Model(Mesh mesh, ModelTexture modelTexture, float textureScale) {
        this.mesh = mesh;
        this.modelTexture = modelTexture;
        this.textureScale = textureScale;
    }

    public Model(Mesh mesh, ModelTexture modelTexture, float textureScale, float scale) {
        this.mesh = mesh;
        this.modelTexture = modelTexture;
        this.textureScale = textureScale;
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
