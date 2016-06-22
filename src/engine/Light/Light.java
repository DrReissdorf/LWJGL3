package engine.Light;

import engine.GameObjectRoot;
import singleton.HolderSingleton;
import math.Mat4;
import math.Vec3;

public class Light {
    private GameObjectRoot gameObjectRoot;

    private Mat4 viewMatrix;
    private Mat4 projectionMatrix;

    private Vec3 color;
    private float range;

    private float circleMoveSpeed;
    private float circleMoveAngle = 0;

    private float fov = 100f;

    float distanceToOrigin;

    private HolderSingleton holder;

    public Light(Vec3 color, float range) {
        holder = HolderSingleton.getInstance();
        this.color = color;
        this.range = range;
        projectionMatrix = Mat4.perspective(fov, holder.getShadowMapSize(), holder.getShadowMapSize(), 0.1f, range);
    }

    public Light(Vec3 color, float range, float circleMoveSpeed, float distanceToOrigin, float circleMoveAngle) {
        holder = HolderSingleton.getInstance();
        this.color = color;
        this.range = range;
        this.circleMoveSpeed = circleMoveSpeed;
        this.distanceToOrigin = distanceToOrigin;
        this.circleMoveAngle = circleMoveAngle;
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

    public void moveAroundCenter() {
        Vec3 entityPosition = gameObjectRoot.getPosition();

        gameObjectRoot.setPosition(new Vec3(0 + (float)Math.sin(circleMoveAngle) * distanceToOrigin, entityPosition.y, 0 + (float)Math.cos(circleMoveAngle) * distanceToOrigin));

        circleMoveAngle += circleMoveSpeed;
        if(circleMoveAngle >= 360) circleMoveAngle -= 360;

        updateViewMatrix();
    }

    private void updateViewMatrix() {
        viewMatrix = Mat4.lookAt(gameObjectRoot.getPosition(), new Vec3(), new Vec3(0, 1, 0));
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

    public GameObjectRoot getGameObjectRoot() {
        return gameObjectRoot;
    }

    public void setGameObjectRoot(GameObjectRoot gameObjectRoot) {
        this.gameObjectRoot = gameObjectRoot;
    }
}
