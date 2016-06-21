package engine;

import util.Texture;

public class ModelTexture {
    private Texture texture;
    private float reflectivity;
    private float shininess;


    public ModelTexture(Texture texture, float reflectivity, float shininess) {
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