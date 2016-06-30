package engine.gameobjects;

import engine.gameobjects.GameObjectRoot;
import engine.gameobjects.GameObject;
import math.Mat4;
import math.Vec3;

public class Camera extends GameObject {
    private Mat4 projectionMatrix;
    private long lastTime;
    private boolean isFreeFlight = true;
    private float moveSpeed;
    private final boolean printPosition = false;

    public Camera(GameObjectRoot root, float fov, int windowWidth, int windowHeight, float near, float far, float moveSpeed) {
        super(root);
        lastTime = System.currentTimeMillis();
        this.moveSpeed = moveSpeed;
        projectionMatrix = Mat4.perspective( fov, windowWidth, windowHeight, near, far );
    }

    public Mat4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public Mat4 getViewMatrix() {
        Vec3 position = getRoot().getPosition();
        Vec3 direction = getRoot().getDirection();
        Vec3 up = getRoot().getUp();

        if(printPosition) {
            if((System.currentTimeMillis()-lastTime) > 1000) {
                System.out.println("Main Camera: Position - x:"+position.x+" y:"+position.y+" z:"+position.z+"  Rotation - x:"+getRoot().getRotX()+" y:"+getRoot().getRotY());
                lastTime = System.currentTimeMillis();
            }
        }

        Vec3 temp = new Vec3(position.x+direction.x, position.y+direction.y, position.z+direction.z);

        return Mat4.lookAt(position, temp, up);
    }

    public void moveForward(float speed) {
        if(isFreeFlight) getRoot().moveViewDirection(speed);
        else moveForward(speed);
    }

    public void moveBackward(float speed) {
        if(isFreeFlight) getRoot().moveViewDirection(-speed);
        else moveForward(-speed);
    }

    public boolean isFreeFlight() {
        return isFreeFlight;
    }

    public void setFreeFlight(boolean freeFlight) {
        Vec3 position = getRoot().getPosition();

        isFreeFlight = freeFlight;
        getRoot().setPosition(new Vec3(position.x, 2f, position.z));
        System.out.println("Main Camera: Set freeflight-mode to "+freeFlight);
    }
}
