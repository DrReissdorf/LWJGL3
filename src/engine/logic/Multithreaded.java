/*
 * Copyright LWJGL. All rights reserved.
 * License terms: http://lwjgl.org/license.php
 */
package engine.logic;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.Callback;

import java.io.File;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Multithreaded {
    private Scene scene;
    private InputHandler inputHandler;
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWFramebufferSizeCallback fsCallback;
    private Callback debugProc;

    long window;
    int width = 1280;
    int height = 720;
    Object lock = new Object();
    boolean destroyed;

    void run() {
        System.setProperty("org.lwjgl.librarypath", new File("frameworks/lwjgl3/native").getAbsolutePath());
        try {
            init();
            winProcLoop();

            synchronized (lock) {
                destroyed = true;
                glfwDestroyWindow(window);
            }
            if (debugProc != null)
                debugProc.free();
            keyCallback.free();
            fsCallback.free();
        } finally {
            glfwTerminate();
            errorCallback.free();
        }
    }

    void init() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
            }
        });
        glfwSetFramebufferSizeCallback(window, fsCallback = new GLFWFramebufferSizeCallback() {
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
        glfwShowWindow(window);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    void updateLoop() {
        while(scene == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        inputHandler = new InputHandler(window,scene);

        while (!destroyed) {
            inputHandler.updateInput();
            scene.update();
            limitTps(60);
        }
    }

    void renderLoop() {
        glfwMakeContextCurrent(window);

        GL.createCapabilities();

        glfwSwapInterval(0); //disable vsync
        debugProc = GLUtil.setupDebugMessageCallback();

        scene = new Scene(window,width,height);

        float   deltaTime;
        long    lastNanoTime  = 0;

        long lastFpsNanoTime = 0;
        int fps = 0;
        long tempTime;

        while (!destroyed) {
            long time = System.nanoTime();
            deltaTime = (float)(time - lastNanoTime) * 1e-9f;
            lastNanoTime  = time;

            scene.render(deltaTime);
            limitFps(30);

            fps++;
            tempTime = time-lastFpsNanoTime;
            if(tempTime > 1000000000) {
                glfwSetWindowTitle(window,"FPS: "+fps+" Frametime: "+ (float)((tempTime/fps/10000))/100+"ms");
                lastFpsNanoTime = time;
                fps = 0;
            }

            synchronized (lock) {
                if (!destroyed) {
                    glfwSwapBuffers(window);
                }
            }
        }
    }

    void winProcLoop() {
        new Thread(new Runnable() {
            public void run() {
                renderLoop();
            }
        }).start();

        new Thread(() -> {
            updateLoop();
        }).start();

        //renderLoop();

        while (!glfwWindowShouldClose(window)) {
            glfwWaitEvents();
        }

        System.out.println("Shutting down...");
        System.exit(-1);
    }

    private long variableYieldTimeFPS, lastTimeFPS;
    /**
     * An accurate limitFps method that adapts automatically
     * to the system it runs on to provide reliable results.
     *
     * @param fps The desired frame rate, in frames per second
     * @author kappa (On the LWJGL Forums)
     */
    private void limitFps(int fps) {
        if (fps <= 0) return;

        long sleepTime = 1000000000 / fps; // nanoseconds to sleep this frame
        // yieldTime + remainder micro & nano seconds if smaller than sleepTime
        long yieldTime = Math.min(sleepTime, variableYieldTimeFPS + sleepTime % (1000*1000));
        long overSleep = 0; // time the limitFps goes over by

        try {
            while (true) {
                long t = System.nanoTime() - lastTimeFPS;

                if (t < sleepTime - yieldTime) {
                    Thread.sleep(1);
                }else if (t < sleepTime) {
                    // burn the last few CPU cycles to ensure accuracy
                    Thread.yield();
                }else {
                    overSleep = t - sleepTime;
                    break; // exit while loop
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            lastTimeFPS = System.nanoTime() - Math.min(overSleep, sleepTime);

            // auto tune the time limitFps should yield
            if (overSleep > variableYieldTimeFPS) {
                // increase by 200 microseconds (1/5 a ms)
                variableYieldTimeFPS = Math.min(variableYieldTimeFPS + 200*1000, sleepTime);
            }
            else if (overSleep < variableYieldTimeFPS - 200*1000) {
                // decrease by 2 microseconds
                variableYieldTimeFPS = Math.max(variableYieldTimeFPS - 2*1000, 0);
            }
        }
    }

    private long variableYieldTimeTPS, lastTimeTPS;
    /**
     * An accurate limitFps method that adapts automatically
     * to the system it runs on to provide reliable results.
     *
     * @param fps The desired frame rate, in frames per second
     * @author kappa (On the LWJGL Forums)
     */
    private void limitTps(int fps) {
        if (fps <= 0) return;

        long sleepTime = 1000000000 / fps; // nanoseconds to sleep this frame
        // yieldTime + remainder micro & nano seconds if smaller than sleepTime
        long yieldTime = Math.min(sleepTime, variableYieldTimeTPS + sleepTime % (1000*1000));
        long overSleep = 0; // time the limitFps goes over by

        try {
            while (true) {
                long t = System.nanoTime() - lastTimeTPS;

                if (t < sleepTime - yieldTime) {
                    Thread.sleep(1);
                }else if (t < sleepTime) {
                    // burn the last few CPU cycles to ensure accuracy
                    Thread.yield();
                }else {
                    overSleep = t - sleepTime;
                    break; // exit while loop
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            lastTimeTPS = System.nanoTime() - Math.min(overSleep, sleepTime);

            // auto tune the time limitFps should yield
            if (overSleep > variableYieldTimeTPS) {
                // increase by 200 microseconds (1/5 a ms)
                variableYieldTimeTPS = Math.min(variableYieldTimeTPS + 200*1000, sleepTime);
            }
            else if (overSleep < variableYieldTimeTPS - 200*1000) {
                // decrease by 2 microseconds
                variableYieldTimeTPS = Math.max(variableYieldTimeTPS - 2*1000, 0);
            }
        }
    }

    public static void main(String[] args) {
        new Multithreaded().run();
    }

}