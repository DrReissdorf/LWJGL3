package engine.gameobjects.Light;

import engine.gameobjects.GameObjectRoot;
import engine.gameobjects.GameObject;
import math.Mat4;
import math.Vec3;
import singleton.HolderSingleton;

public class Light extends GameObject {
    private Mat4 viewMatrix;
    private Mat4 projectionMatrix;

    private Vec3 rootPositionOffset;
    private Vec3 color;
    private float range;

    private float intensity;
    private float circleMoveSpeed;
    private float circleMoveAngle = 0;

    private float fov = 80f;

    float distanceToOrigin;

    private HolderSingleton holder;

    public Light(GameObjectRoot root, Vec3 rootPositionOffset, Vec3 color, float intensity, float range) {
        super(root);
        holder = HolderSingleton.getInstance();
        this.rootPositionOffset = rootPositionOffset;
        this.color = color;
        this.intensity = intensity;
        this.range = range;
        projectionMatrix = Mat4.perspective(fov, holder.getShadowMapSize(), holder.getShadowMapSize(), 0.1f, range);
    }

    public Light(GameObjectRoot root, Vec3 rootPositionOffset, Vec3 color, float intensity, float range, float circleMoveSpeed, float distanceToOrigin, float circleMoveAngle) {
        super(root);
        holder = HolderSingleton.getInstance();
        this.rootPositionOffset = rootPositionOffset;
        this.color = color;
        this.intensity = intensity;
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
        Vec3 entityPosition = getRoot().getPosition();

        getRoot().setPosition(new Vec3(0 + (float)Math.sin(circleMoveAngle) * distanceToOrigin, entityPosition.y, 0 + (float)Math.cos(circleMoveAngle) * distanceToOrigin));

        circleMoveAngle += circleMoveSpeed;
        if(circleMoveAngle >= 360) circleMoveAngle -= 360;

        updateViewMatrix();
    }

    private void updateViewMatrix() {
        viewMatrix = Mat4.lookAt(getRoot().getPosition(), new Vec3(), new Vec3(0, 1, 0));
    }

    public Vec3 getPosition() {
        return Vec3.add(getRoot().getPosition(),rootPositionOffset);
    }

    public Mat4 getViewMatrix() {
        viewMatrix = Mat4.lookAt(getRoot().getPosition(), new Vec3(), new Vec3(0, 1, 0));
        return viewMatrix;
    }

    public Mat4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public void setProjectionMatrix(Mat4 projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}
