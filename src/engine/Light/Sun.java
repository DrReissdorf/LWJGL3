package engine.Light;

import engine.GameObjectRoot;
import math.Mat4;
import math.Vec3;
import singleton.HolderSingleton;

public class Sun {
    private Mat4 viewMatrix;
    private Mat4 projectionMatrix;

    private Vec3 position;
    private Vec3 color;
    private float range;

    private float circleMoveSpeed;
    private float circleMoveAngle = 0;

    private float fov = 100f;

    float distanceToOrigin;

    private HolderSingleton holder;

    public Sun(Vec3 position, Vec3 color, float range) {
        holder = HolderSingleton.getInstance();
        this.position = position;
        this.color = color;
        this.range = range;
        projectionMatrix = Mat4.orthographic(10,-10,10,-10,-10,20);
        viewMatrix = Mat4.lookAt(position, new Vec3(), new Vec3(0, 1, 0));
    }

    public Sun(Vec3 position, Vec3 color, float range, float circleMoveSpeed, float distanceToOrigin, float circleMoveAngle) {
        holder = HolderSingleton.getInstance();
        this.position = position;
        this.color = color;
        this.range = range;
        this.circleMoveSpeed = circleMoveSpeed;
        this.distanceToOrigin = distanceToOrigin;
        projectionMatrix = Mat4.orthographic(10,-10,10,-10,-10,20);
        viewMatrix = Mat4.lookAt(position, new Vec3(), new Vec3(0, 1, 0));
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
        Vec3 entityPosition = position;

        position = new Vec3(0 + (float)Math.sin(circleMoveAngle) * distanceToOrigin, entityPosition.y, 0 + (float)Math.cos(circleMoveAngle) * distanceToOrigin);

        circleMoveAngle += circleMoveSpeed;
        if(circleMoveAngle >= 360) circleMoveAngle -= 360;

        updateViewMatrix();
    }

    private void updateViewMatrix() {
        viewMatrix = Mat4.lookAt(position, new Vec3(), new Vec3(0, 1, 0));
    }

    public Vec3 getPosition() {
        return position;
    }

    public Mat4 getViewMatrix() {
        return viewMatrix;
    }

    public Mat4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public void setProjectionMatrix(Mat4 projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }
}