package engine.Light;

import engine.Light.Light;
import math.Mat4;
import math.Vec3;

public class Sun extends Light {
    public Sun(Vec3 position, Vec3 color, float range) {
        super(position,color,range);
        setProjectionMatrix(Mat4.orthographic(10,-10,10,-10,-10,20));
    }

    public Sun(Vec3 position, Vec3 color, float range, float circleMoveSpeed, float distanceToOrigin, float circleMoveAngle) {
        super(position,color,range,circleMoveSpeed,distanceToOrigin,circleMoveAngle);
        setProjectionMatrix(Mat4.orthographic(10,-10,10,-10,-10,20));
    }
}
