package engine.Light;

import math.Mat4;
import math.Vec3;
import singleton.HolderSingleton;

public class PointLight extends Light{
    private float fov = 80f;
    private HolderSingleton holder;

    public PointLight(Vec3 position, Vec3 color, float range) {
        super(position,color,range);
        holder = HolderSingleton.getInstance();
        setProjectionMatrix(Mat4.perspective(fov, holder.getShadowMapSize(), holder.getShadowMapSize(), 0.1f, range));
    }

    public PointLight(Vec3 position, Vec3 color, float range, float circleMoveSpeed, float distanceToOrigin, float circleMoveAngle) {
        super(position,color,range,circleMoveSpeed,distanceToOrigin,circleMoveAngle);
        holder = HolderSingleton.getInstance();
        setProjectionMatrix(Mat4.perspective(fov, holder.getShadowMapSize(), holder.getShadowMapSize(), 0.1f, range));
    }
}
