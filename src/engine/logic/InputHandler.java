package engine.logic;

import engine.Camera;
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
    private boolean is_COMMA_pressed = false;
    private Camera mainCamera;
    private Scene scene;

    private final float movementMulti = 4;

    private float lastFrameMouseX=0, lastFrameMouseY=0;
    private DoubleBuffer b1 = BufferUtils.createDoubleBuffer(1);
    private DoubleBuffer b2 = BufferUtils.createDoubleBuffer(1);

    public InputHandler(long window, Scene scene) {
        this.scene = scene;
        this.window = window;
        mainCamera = scene.getMainCamera();

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void updateInput() {
        float deltaTime = 0.05f;
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
            mainCamera.moveForward(mainCamera.getMoveSpeed()*deltaTime*movementMulti);
        }
        if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            mainCamera.moveLeft(mainCamera.getMoveSpeed()*deltaTime*movementMulti);
        }
        if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            mainCamera.moveBackward(mainCamera.getMoveSpeed()*deltaTime*movementMulti);
        }
        if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            mainCamera.moveRight(mainCamera.getMoveSpeed()*deltaTime*movementMulti);
        }
        if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            mainCamera.moveUp(mainCamera.getMoveSpeed()*deltaTime*movementMulti);
        }
        if(glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            mainCamera.moveDown(mainCamera.getMoveSpeed()*deltaTime*movementMulti);
        }

        glfwGetCursorPos(window, b1, b2);

        float rotationScale = 0.003f;
        float deltaX = (float) b1.get(0)-lastFrameMouseX;
        float deltaY = (float) b2.get(0)-lastFrameMouseY;
        lastFrameMouseX = (float) b1.get(0);
        lastFrameMouseY = (float) b2.get(0);
        mainCamera.addRotations(deltaX * rotationScale, -deltaY * rotationScale);
    }

    private class KeyboardHandler extends GLFWKeyCallback {
        public boolean[] keys = new boolean[65536];


        // The GLFWKeyCallback class is an abstract method that
        // can't be instantiated by itself and must instead be extended
        //
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            keys[key] = action != GLFW_RELEASE;
        }

        // boolean method that returns true if a given key
        // is pressed.
        public boolean isKeyDown(int keycode) {
            return keys[keycode];
        }
    }
}
