package engine.gameobjects.test;

import util.Texture;

public class ModelTextureTest {
    private Texture texture;
    private float reflectivity;
    private float shininess;


    public ModelTextureTest(Texture texture, float reflectivity, float shininess) {
        this.texture = texture;
        this.reflectivity = reflectivity;
        this.shininess = shininess;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public float getShininess() {
        return shininess;
    }
}