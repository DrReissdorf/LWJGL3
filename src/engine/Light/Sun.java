package engine.Light;

import engine.GameObjectRoot;
import engine.Model;
import math.Mat4;
import math.Vec3;

public class Sun {
    private GameObjectRoot gameObjectRoot;
    private Mat4 viewMatrix;
    private Mat4 projectionMatrix;

    private Vec3 color;
    private Vec3 originalColor;
    private float range;
    private Model model;

    public Sun(Vec3 color, float range) {
        this.color = color;
        originalColor = new Vec3(color);
        this.range = range;
        projectionMatrix = Mat4.orthographic(10,-10,10,-10,-10,20);

    }

    public Vec3 getColor() {
        return color;
    }

    public void setColor(Vec3 color) {
        this.color = color;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public void dayNightCycle(float dayTime, float dayTimeIncrease) {
        gameObjectRoot.setPosition(new Vec3(0 + (float)Math.cos(Math.toRadians(dayTime)) * 5, 0 + (float)Math.sin(Math.toRadians(dayTime)) * 5, gameObjectRoot.getPosition().z));

        float fadeSpeed = dayTimeIncrease/5;
        if(dayTime>180) {
            if(color.x > 0) color.x -= fadeSpeed;
            if(color.y > 0) color.y -= fadeSpeed;
            if(color.z > 0) color.z -= fadeSpeed;

        } else {
            if(color.x <= 0) color.x = 0.01f;
            if(color.y <= 0) color.y = 0.01f;
            if(color.z <= 0) color.z = 0.01f;
            if(color.x < originalColor.x) color.x += fadeSpeed;
            if(color.y < originalColor.y) color.y += fadeSpeed;
            if(color.z < originalColor.z) color.z += fadeSpeed;
        }
    }

    public Vec3 getPosition() {
        return gameObjectRoot.getPosition();
    }

    public Mat4 getViewMatrix() {
        viewMatrix = Mat4.lookAt(gameObjectRoot.getPosition(), new Vec3(), new Vec3(0, 1, 0));
        return viewMatrix;
    }

    public Mat4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public void setProjectionMatrix(Mat4 projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public GameObjectRoot getGameObjectRoot() {
        return gameObjectRoot;
    }

    public void setGameObjectRoot(GameObjectRoot gameObjectRoot) {
        this.gameObjectRoot = gameObjectRoot;
    }
}