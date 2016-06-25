package engine.Light;

import math.Mat4;
import math.Vec3;
import singleton.HolderSingleton;

public class PointLight extends Light {
    private float fov = 80f;
    private HolderSingleton holder;

    public PointLight(Vec3 color, float intensity, float range) {
        super(color,intensity,range);
        holder = HolderSingleton.getInstance();
        setProjectionMatrix(Mat4.perspective(fov, holder.getShadowMapSize(), holder.getShadowMapSize(), 0.1f, range));
    }

    public PointLight(Vec3 color,float intensity, float range, float circleMoveSpeed, float distanceToOrigin, float circleMoveAngle) {
        super(color,intensity,range,circleMoveSpeed,distanceToOrigin,circleMoveAngle);
        holder = HolderSingleton.getInstance();
        setProjectionMatrix(Mat4.perspective(fov, holder.getShadowMapSize(), holder.getShadowMapSize(), 0.1f, range));
    }
}
