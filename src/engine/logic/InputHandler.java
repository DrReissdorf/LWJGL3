package engine.logic;

import engine.gameobjects.Camera;
import engine.scene.Scene;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWKeyCallback;
import singleton.HolderSingleton;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;

public class InputHandler {
    private long window;
    private boolean is_L_pressed = false;
    private boolean is_M_pressed = false;
    private boolean isGrabbed = false;
    private boolean is_COMMA_pressed = false;
    private Camera mainCamera;
    private Scene scene;

    private final float movementMulti = 2f;

    private float lastFrameMouseX=0, lastFrameMouseY=0;
    private DoubleBuffer b1 = BufferUtils.createDoubleBuffer(1);
    private DoubleBuffer b2 = BufferUtils.createDoubleBuffer(1);

    public InputHandler(long window, Scene scene) {
        this.scene = scene;
        this.window = window;
        mainCamera = HolderSingleton.getInstance().getMainCamera();
    }

    public void updateInput(float deltaTime) {
        if(glfwGetKey(window, GLFW_KEY_M) == GLFW_PRESS) {
            if(!is_M_pressed) {
                is_M_pressed = true;
                if(!isGrabbed) glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                else glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                isGrabbed = !isGrabbed;
            }
        }
        if(glfwGetKey(window, GLFW_KEY_M) == GLFW_RELEASE) {
            if(is_M_pressed) {
                is_M_pressed = false;
            }
        }

        if (glfwGetKey(window, GLFW_KEY_L) == GLFW_PRESS) {
            if(!is_L_pressed) {
                is_L_pressed = true;
                scene.setLightMoving(!scene.isLightMoving());
            }
        }
        if (glfwGetKey(window, GLFW_KEY_L) == GLFW_RELEASE) {
            if(is_L_pressed) {
                is_L_pressed = false;
            }
        }

        if(glfwGetKey(window, GLFW_KEY_COMMA) == GLFW_PRESS) {
            if(!is_COMMA_pressed) {
                is_COMMA_pressed = true;
                mainCamera.setFreeFlight(!mainCamera.isFreeFlight());
            }
        }
        if(glfwGetKey(window, GLFW_KEY_COMMA) == GLFW_RELEASE) {
            if(is_COMMA_pressed) {
                is_COMMA_pressed = false;
            }
        }

        if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            mainCamera.moveForward(mainCamera.getMoveSpeed()*movementMulti*deltaTime);
        }
        if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            mainCamera.getRoot().moveLeft(mainCamera.getMoveSpeed()*movementMulti*deltaTime);
        }
        if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            mainCamera.moveBackward(mainCamera.getMoveSpeed()*movementMulti*deltaTime);
        }
        if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            mainCamera.getRoot().moveRight(mainCamera.getMoveSpeed()*movementMulti*deltaTime);
        }
        if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            mainCamera.getRoot().moveUp(mainCamera.getMoveSpeed()*movementMulti*deltaTime);
        }
        if(glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            mainCamera.getRoot().moveDown(mainCamera.getMoveSpeed()*movementMulti*deltaTime);
        }

        glfwGetCursorPos(window, b1, b2);

        float rotationScale = 0.003f;
        float deltaX = (float) b1.get(0)-lastFrameMouseX;
        float deltaY = (float) b2.get(0)-lastFrameMouseY;
        lastFrameMouseX = (float) b1.get(0);
        lastFrameMouseY = (float) b2.get(0);

        if(mainCamera.getRoot().getRotY()-(deltaY*rotationScale)>=-1.5f && mainCamera.getRoot().getRotY()-(deltaY*rotationScale)<=1.5f) mainCamera.getRoot().addRotY(-deltaY*rotationScale);
        mainCamera.getRoot().addRotX(deltaX*rotationScale);
    }
}
