package engine;


import math.Vec3;
import util.Mesh;

public class Model extends GameObject {
    private Mesh mesh;
    private ModelTexture modelTexture;
    private float textureScale;

    public Model(Vec3 position, Mesh mesh, ModelTexture modelTexture, float textureScale) {
        super(position);
        this.mesh = mesh;
        this.modelTexture = modelTexture;
        this.textureScale = textureScale;
    }

    public Model(Vec3 position, Mesh mesh, ModelTexture modelTexture, float textureScale, float scale) {
        super(position, scale);
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
