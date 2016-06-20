package toolbox;

import math.Mat4;
import math.Vec3;

/**
 *
 * @author svenmaster
 */
public class Transformation {
    private static final Mat4 einheitsMatrix = new Mat4();

    public static Mat4 createTransformationMatrix(Vec3 translation, float rx, float ry, float rz, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);
        matrix = Mat4.mul(Mat4.rotation(new Vec3(1,0,0), rx), matrix);
        matrix = Mat4.mul(Mat4.rotation(new Vec3(0,1,0), ry), matrix);
        matrix = Mat4.mul(Mat4.rotation(new Vec3(0,0,1), rz), matrix);
        matrix = Mat4.mul(Mat4.translation(translation), matrix);
        return matrix;
    }

    public static Mat4 createTransMat(Mat4 modelMatrix, float posX, float posY, float posZ, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);
        matrix = Mat4.mul(Mat4.translation(posX, posY, posZ), matrix);        
        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method        
        return matrix;
    }
    public static Mat4 createTransMat(Mat4 modelMatrix, Vec3 vec, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);
        matrix = Mat4.mul(Mat4.translation(vec.x, vec.y, vec.z), matrix);        
        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method        
        return matrix;
    }
    public static Mat4 createTransMat(Mat4 modelMatrix, float posX, float posY, float posZ, String rotAxis, float radiant, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);
        
        if(rotAxis.equals("x")) matrix = Mat4.mul(Mat4.rotation(new Vec3(1,0,0), radiant), matrix); 
        if(rotAxis.equals("y")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,1,0), radiant), matrix); 
        if(rotAxis.equals("z")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,0,1), radiant), matrix); 
        
        matrix = Mat4.mul(Mat4.translation(posX, posY, posZ), matrix);
        
        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method        
        return matrix;
    }
    public static Mat4 createTransMat(Mat4 modelMatrix, Vec3 vec, String rotAxis, float radiant, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);
        
        if(rotAxis.equals("x")) matrix = Mat4.mul(Mat4.rotation(new Vec3(1,0,0), radiant), matrix); 
        if(rotAxis.equals("y")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,1,0), radiant), matrix); 
        if(rotAxis.equals("z")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,0,1), radiant), matrix); 
        
        matrix = Mat4.mul(Mat4.translation(vec.x, vec.y, vec.z), matrix);
        
        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method        
        return matrix;
    }
    public static Mat4 createTransMat(Mat4 modelMatrix, Vec3 vec, String rotAxis1, float radiant1, String rotAxis2, float radiant2, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);
        
        if(rotAxis1.equals("x")) matrix = Mat4.mul(Mat4.rotation(new Vec3(1,0,0), radiant1), matrix); 
        if(rotAxis1.equals("y")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,1,0), radiant1), matrix); 
        if(rotAxis1.equals("z")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,0,1), radiant1), matrix); 
        
        if(rotAxis2.equals("x")) matrix = Mat4.mul(Mat4.rotation(new Vec3(1,0,0), radiant2), matrix); 
        if(rotAxis2.equals("y")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,1,0), radiant2), matrix); 
        if(rotAxis2.equals("z")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,0,1), radiant2), matrix); 
        
        matrix = Mat4.mul(Mat4.translation(vec.x, vec.y, vec.z), matrix);
        
        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method        
        return matrix;
    }
    public static Mat4 createTransMat(Mat4 modelMatrix, float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);

        matrix = Mat4.mul(Mat4.rotation(new Vec3(1,0,0), rotX), matrix);
        matrix = Mat4.mul(Mat4.rotation(new Vec3(0,1,0), rotY), matrix);
        matrix = Mat4.mul(Mat4.rotation(new Vec3(0,0,1), rotZ), matrix);

        matrix = Mat4.mul(Mat4.translation(posX, posY, posZ), matrix);

        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method
        return matrix;
    }
    public static Mat4 createTransMat(Mat4 modelMatrix, Vec3 position, float rotX, float rotY, float rotZ, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);

        if(rotX != 0f) matrix = Mat4.mul(Mat4.rotation(new Vec3(1,0,0), rotX), matrix);
        if(rotY != 0f) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,1,0), rotY), matrix);
        if(rotZ != 0f) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,0,1), rotZ), matrix);

        matrix = Mat4.mul(Mat4.translation(position), matrix);

        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method
        return matrix;
    }
}
