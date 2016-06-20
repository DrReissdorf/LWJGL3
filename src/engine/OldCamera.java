package engine;

import math.Mat4;
import math.Vec3;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class OldCamera {
    private Mat4 projectionMatrix;
    private long lastTime;
    private Vec3 position, direction, right, forward, up;
    private final float PI = 3.14159f;
    private float rotX;
    private float rotY;
    private boolean isFreeFlight = true;

    private float moveSpeed;

    public Mat4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public OldCamera(Vec3 position, float fov, int windowWidth, int windowHeight, float near, float far, float moveSpeed) {
        lastTime = System.currentTimeMillis();
        this.position = position;
        rotX = (float)Math.toRadians(180);
        rotY = (float)Math.toRadians(-45);
        this.moveSpeed = moveSpeed;
        projectionMatrix = Mat4.perspective( fov, windowWidth, windowHeight, near, far );

        calcDirection();
        calcRight();
        calcUp();
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public Vec3 getDirection() {
        calcDirection();
        return direction;
    }

    public Vec3 getRight() {
        calcRight();
        return right;
    }

    public Vec3 getUp() {
        calcUp();
        return up;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void calcDirection() {
        float x;
        float y;
        float z;

        x = (float)(cos(rotY) * sin(rotX));
        y = (float)(sin(rotY));
        z = (float)(cos(rotY) * cos(rotX));

        direction = new Vec3(x, y, z);
    }

    public void calcForward() {
        float x = (float)(sin(rotX));
        float z = (float)(cos(rotX));

        forward = new Vec3(x, 0, z);
    }

    public void calcRight() {
        float x = (float)(sin(rotX - PI/2.0f));
        float z = (float)(cos(rotX - PI/2.0f));

        right = new Vec3(x, 0, z);
    }

    public void moveForward(float speed) {
        if(isFreeFlight) {
            position.x += direction.x * speed;
            position.y += direction.y * speed;
            position.z += direction.z * speed;
        } else {
            position.x += forward.x * speed;
            position.y += 0;
            position.z += forward.z * speed;
        }
    }

    public void moveBackward(float speed) {
        if(isFreeFlight) {
            position.x -= direction.x * speed;
            position.y -= direction.y * speed;
            position.z -= direction.z * speed;
        } else {
            position.x -= forward.x * speed;
            position.y -= forward.y * speed;
            position.z -= forward.z * speed;
        }
    }

    public void moveRight(float speed) {
        position.x += right.x * speed;
        position.y += right.y * speed;
        position.z += right.z * speed;
    }

    public void moveLeft(float speed) {
        position.x -= right.x * speed;
        position.y -= right.y * speed;
        position.z -= right.z * speed;
    }

    public void moveDown(float speed) {
        position.y -=  speed;
    }

    public void moveUp(float speed) {
        position.y +=  speed;
    }

    public void calcUp() {
        up = Vec3.cross(right, direction);
    }

    public void addRotX(float rotX) {
        this.rotX -= rotX;
    }

    public void addRotY(float rotY) {
        this.rotY += rotY;
    }

    public Mat4 getViewMatrix() {
        calcDirection();
        calcRight();
        calcForward();
        calcUp();

        if((System.currentTimeMillis()-lastTime) > 1000) {
            System.out.println("run.Main Camera: Position - x:"+position.x+" y:"+position.y+" z:"+position.z);
            lastTime = System.currentTimeMillis();
        }

        Vec3 temp = new Vec3(position.x+direction.x, position.y+direction.y, position.z+direction.z);
        return Mat4.lookAt(position, temp, up);
    }

    public boolean isFreeFlight() {
        return isFreeFlight;
    }

    public void setFreeFlight(boolean freeFlight) {
        isFreeFlight = freeFlight;
        position.y = 0.5f;
        System.out.println("run.Main Camera: Set freeflight-mode to "+freeFlight);
    }
}
