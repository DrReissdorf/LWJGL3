package engine.gameobjects;

import engine.gameobjects.Light.Light;
import math.Mat4;
import math.Vec3;
import toolbox.Transformation;

import java.util.ArrayList;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static math.MathUtil.PI;

public class GameObjectRoot {
    private Mat4 tranformationMatrix;
    private float scale;
    private Vec3 position, direction, right, forward, up;
    private float rotX=0, rotY=0, rotZ=0;

    private ArrayList<GameObject> newGameObjectArrayList;

    public GameObjectRoot(Vec3 position) {
        this.position = position;
        this.scale = 1;
        this.tranformationMatrix = Transformation.createTransformationMatrix(position,rotX,rotY,rotZ,scale);
        newGameObjectArrayList = new ArrayList<>();

        updateVectors();
    }

    private void updateVectors() {
        calcForward();
        calcRight();
        calcDirection();
        calcUp();
    }

    public float getScale() {
        return scale;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
        updateTranformationMatrix();
    }

    public Mat4 getTranformationMatrix() {
        return tranformationMatrix;
    }

    public void updateTranformationMatrix() {
        tranformationMatrix = Transformation.createTransMat(new Mat4(),position,rotX,rotY,rotZ,scale);
    }

    public void increaseRotation(float dx, float dy, float dz) {
        rotX += dx;
        rotY += dy;
        rotZ += dz;

        if(rotX >= 360) rotX -= 360;
        if(rotY >= 360) rotY -= 360;
        if(rotZ >= 360) rotZ -= 360;

        updateTranformationMatrix();
    }

    public Vec3 getDirection() {
        return direction;
    }

    public Vec3 getRight() {
        return right;
    }

    public Vec3 getUp() {
        return up;
    }

    private void calcDirection() {
        float x = (float)(cos(rotY) * sin(rotX));
        float y = (float)(sin(rotY));
        float z = (float)(cos(rotY) * cos(rotX));

        direction = new Vec3(x, y, z);
    }

    private void calcUp() {
        up = Vec3.cross(right, direction);
    }

    private void calcForward() {
        float x = (float)(sin(rotX));
        float z = (float)(cos(rotX));
        forward = new Vec3(x, 0, z);
    }

    private void calcRight() {
        float x = (float)(sin(rotX - PI/2.0f));
        float z = (float)(cos(rotX - PI/2.0f));
        right = new Vec3(x, 0, z);
    }

    public void moveForward(float speed) {
        updateVectors();

        Vec3 newPosition = new Vec3(position.x+(forward.x*speed),position.y,position.z+(forward.z*speed));
        Vec3 X = new Vec3(newPosition.x,position.y,position.z);
        Vec3 Z = new Vec3(position.x,position.y,newPosition.z);

        position.x = X.x;
        position.z = Z.z;

        updateTranformationMatrix();
    }

    public void moveViewDirection(float speed) {
        updateVectors();

        Vec3 newPosition = new Vec3(position.x+(direction.x*speed),position.y+(direction.y*speed),position.z+(direction.z*speed));
        position = newPosition;

        updateTranformationMatrix();
    }

    public void moveBackward(float speed) {
        updateVectors();

        Vec3 newPosition = new Vec3(position.x-(forward.x*speed),position.y-(forward.y*speed),position.z-(forward.z*speed));
        position = newPosition;

        updateTranformationMatrix();
    }

    public void moveRight(float speed) {
        updateVectors();

        Vec3 newPosition = new Vec3(position.x+(right.x*speed),position.y,position.z+(right.z*speed));
        position = newPosition;

        updateTranformationMatrix();
    }

    public void moveLeft(float speed) {
        updateVectors();

        Vec3 newPosition = new Vec3(position.x-(right.x*speed),position.y,position.z-(right.z*speed));
        position = newPosition;

        updateTranformationMatrix();
    }

    public void moveDown(float speed) {
        updateVectors();
        position.y -=  speed;
    }

    public void moveUp(float speed) {
        updateVectors();
        position.y +=  speed;
    }

    public void addRotX(float rotX) {
        updateVectors();
        this.rotX -= rotX;
        updateTranformationMatrix();
    }

    public void addRotY(float rotY) {
        updateVectors();
        this.rotY += rotY;
        updateTranformationMatrix();
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }

    public void addRotations(float rotX, float rotY) {
        updateVectors();
        this.rotX -= rotX;
        this.rotY += rotY;
        updateTranformationMatrix();
    }

    public float getRotZ() {
        return rotZ;
    }

    public float getRotX() {
        return rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public ArrayList<GameObject> getNewGameObjectArrayList() {
        return newGameObjectArrayList;
    }

    public void addNewGameObject(GameObject newGameObject) {
        newGameObjectArrayList.add(newGameObject);
    }

    public ArrayList<Model> getAllModels() {
        ArrayList<Model> modelTests = new ArrayList<>();

        for(GameObject newGameObject : newGameObjectArrayList) {
            if(newGameObject instanceof Model) modelTests.add((Model)newGameObject);
        }

        return modelTests;
    }

    public ArrayList<Light> getAllLights() {
        ArrayList<Light> lightTests = new ArrayList<>();

        for(GameObject newGameObject : newGameObjectArrayList) {
            if(newGameObject instanceof Light) lightTests.add((Light)newGameObject);
        }

        return lightTests;
    }

    public void setScale(float scale) {
        this.scale = scale;
        updateTranformationMatrix();
    }
}
