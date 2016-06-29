package engine.networking.entity;

import math.Vec3;

public class Player {
    public String name;
    public long id;
    public Vec3 position;
    public float rotX;
    public float rotY;
    public float rotZ;

    public Player() {
        id = 0;
        name = null;
        position = null;
        rotX = 0;
        rotY = 0;
        rotZ = 0;
    }

    public Player(String name, Vec3 position, float rotX, float rotY, float rotZ) {
        this.name = name;
        this.id = -1;
        this.position = position;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
    }
}
