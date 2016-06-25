package engine;

import math.Mat4;
import math.Vec3;

public class Camera extends GameObjectRoot {
    private Mat4 projectionMatrix;
    private long lastTime;
    private boolean isFreeFlight = true;
    private float moveSpeed;
    private final boolean printPosition = false;

    public Camera(Vec3 position, float fov, int windowWidth, int windowHeight, float near, float far, float moveSpeed) {
        super(position);
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
        Vec3 position = getPosition();
        Vec3 direction = getDirection();
        Vec3 up = getUp();

        if(printPosition) {
            if((System.currentTimeMillis()-lastTime) > 1000) {
                System.out.println("Main Camera: Position - x:"+position.x+" y:"+position.y+" z:"+position.z+"  Rotation - x:"+getRotX()+" y:"+getRotY());
                lastTime = System.currentTimeMillis();
            }
        }

        Vec3 temp = new Vec3(position.x+direction.x, position.y+direction.y, position.z+direction.z);

        return Mat4.lookAt(position, temp, up);
    }

    public void moveForward(float speed) {
        if(isFreeFlight) super.moveViewDirection(speed);
        else super.moveForward(speed);
    }

    public void moveBackward(float speed) {
        if(isFreeFlight) super.moveViewDirection(-speed);
        else super.moveForward(-speed);
    }

    public boolean isFreeFlight() {
        return isFreeFlight;
    }

    public void setFreeFlight(boolean freeFlight) {
        Vec3 position = getPosition();

        isFreeFlight = freeFlight;
        setPosition(new Vec3(position.x, 2f, position.z));
        System.out.println("Main Camera: Set freeflight-mode to "+freeFlight);
    }
}
