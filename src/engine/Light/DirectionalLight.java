package engine.Light;

import math.Vec3;

public class DirectionalLight {

    private Vec3 color;
    private Vec3 direction;
    private float intensity;

    public DirectionalLight(Vec3 color, Vec3 direction, float intensity) {
        this.color = color;
        this.direction = direction;
        this.intensity = intensity;
    }

    public DirectionalLight(DirectionalLight light) {
        this(new Vec3(light.getColor()), new Vec3(light.getDirection()), light.getIntensity());
    }

    public Vec3 getColor() {
        return color;
    }

    public void setColor(Vec3 color) {
        this.color = color;
    }

    public Vec3 getDirection() {
        return direction;
    }

    public void setDirection(Vec3 direction) {
        this.direction = direction;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}
