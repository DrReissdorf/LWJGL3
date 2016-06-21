package engine;

import singleton.HolderSingleton;
import math.Mat4;
import math.Vec3;

public class Light extends Entity {
    private Mat4 projectionMatrix;
    private Mat4 viewMatrix;

    private Vec3 color;
    private float range;

    private float circleMoveSpeed;
    private float circleMoveAngle = 0;

    private float fov = 100f;

    float distanceToOrigin;

    private HolderSingleton holder;

    public Light(Vec3 position, Vec3 color, float range) {
        super(position);
        holder = HolderSingleton.getInstance();
        this.color = color;
        this.range = range;
        projectionMatrix = Mat4.perspective(fov, holder.getShadowMapSize(), holder.getShadowMapSize(), 0.1f, range);
        //projectionMatrix = Mat4.orthographic(10,-10,10,-10,0,20);
        viewMatrix = Mat4.lookAt(position, new Vec3(), new Vec3(0, 1, 0));
    }

    public Light(Vec3 position, Vec3 color, float range, float circleMoveSpeed, float distanceToOrigin, float circleMoveAngle) {
        super(position);
        holder = HolderSingleton.getInstance();
        this.color = color;
        this.range = range;
        this.circleMoveSpeed = circleMoveSpeed;
        this.distanceToOrigin = distanceToOrigin;
        //projectionMatrix = Mat4.perspective(fov, holder.getShadowMapSize(), holder.getShadowMapSize(), 0.1f, range);
        projectionMatrix = Mat4.orthographic(10,-10,10,-10,0,20);
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
        Vec3 entityPosition = getPosition();

        setPosition(new Vec3(0 + (float)Math.sin(circleMoveAngle) * distanceToOrigin, entityPosition.y, 0 + (float)Math.cos(circleMoveAngle) * distanceToOrigin));

        circleMoveAngle += circleMoveSpeed;
        if(circleMoveAngle >= 360) circleMoveAngle -= 360;

        updateViewMatrix();
    }

    private void updateViewMatrix() {
        viewMatrix = Mat4.lookAt(getPosition(), new Vec3(), new Vec3(0, 1, 0));
    }

    private void updateProjectionMatrix(int shadowMapSize) {
        projectionMatrix = Mat4.perspective(fov, HolderSingleton.getInstance().getShadowMapSize(), HolderSingleton.getInstance().getShadowMapSize(), 0.1f, range);
    }

    public Mat4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public Mat4 getViewMatrix() {
        return viewMatrix;
    }
}
