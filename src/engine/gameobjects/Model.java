package engine.gameobjects;


import util.Mesh;

public class Model {
    private Mesh mesh;
    private ModelTexture modelTexture;
    private float textureScale;
    private float scale;

    public Model(Mesh mesh, ModelTexture modelTexture, float textureScale, float scale) {
        this.mesh = mesh;
        this.modelTexture = modelTexture;
        this.textureScale = textureScale;
        this.scale = scale;
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

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
