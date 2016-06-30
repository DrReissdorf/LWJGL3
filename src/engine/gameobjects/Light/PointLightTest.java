package engine.gameobjects.Light;

import engine.gameobjects.GameObjectRoot;
import math.Mat4;
import math.Vec3;
import singleton.HolderSingleton;

public class PointLightTest extends Light {
    private float fov = 80f;
    private HolderSingleton holder;

    public PointLightTest(GameObjectRoot root, Vec3 rootPositionOffset, Vec3 color, float intensity, float range) {
        super(root,rootPositionOffset,color,intensity,range);
        holder = HolderSingleton.getInstance();
        setProjectionMatrix(Mat4.perspective(fov, holder.getShadowMapSize(), holder.getShadowMapSize(), 0.1f, range));
    }

    public PointLightTest(GameObjectRoot root, Vec3 rootPositionOffset, Vec3 color, float intensity, float range, float circleMoveSpeed, float distanceToOrigin, float circleMoveAngle) {
        super(root,rootPositionOffset,color,intensity,range,circleMoveSpeed,distanceToOrigin,circleMoveAngle);
        holder = HolderSingleton.getInstance();
        setProjectionMatrix(Mat4.perspective(fov, holder.getShadowMapSize(), holder.getShadowMapSize(), 0.1f, range));
    }
}
