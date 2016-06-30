package engine.gameobjects.Light;

import math.Mat4;
import math.Vec3;

public class Sun {
    private Mat4 viewMatrix;
    private Mat4 projectionMatrix;

    private Vec3 color;
    private Vec3 originalColor;
    private Vec3 position;

    public Sun(Vec3 position, Vec3 color) {
        this.color = color;
        this.position = position;
        originalColor = new Vec3(color);
        projectionMatrix = Mat4.orthographic(10,-10,10,-10,-10,20);
    }

    public Vec3 getColor() {
        return color;
    }

    public void setColor(Vec3 color) {
        this.color = color;
    }

    public void dayNightCycle(float dayTime, float dayTimeIncrease) {
        position = new Vec3(0 + (float)Math.cos(Math.toRadians(dayTime)) * 5, 0 + (float)Math.sin(Math.toRadians(dayTime)) * 5, position.z);

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

    public Mat4 getViewMatrix() {
        viewMatrix = Mat4.lookAt(position, new Vec3(), new Vec3(0, 1, 0));
        return viewMatrix;
    }

    public Mat4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public Vec3 getPosition() {
        return position;
    }
}