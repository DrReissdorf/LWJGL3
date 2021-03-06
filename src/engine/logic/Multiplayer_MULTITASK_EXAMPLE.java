/*
 * Copyright LWJGL. All rights reserved.
 * License terms: http://lwjgl.org/license.php
 */
package engine.logic;

import com.esotericsoftware.kryonet.Client;
import engine.networking.data.NetworkingDataSingleton;
import engine.scene.Scene;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.Platform;
import org.lwjgl.system.macosx.ObjCRuntime;

import java.io.File;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeCocoa.glfwGetCocoaWindow;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.macosx.ObjCRuntime.sel_getUid;

public class Multiplayer_MULTITASK_EXAMPLE {
    private Client client;
    private NetworkingDataSingleton networkingDataSingleton;

    private Scene scene;
    private InputHandler inputHandler;
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWFramebufferSizeCallback fsCallback;
    private Callback debugProc;

    public static long window;
    public static int WIDTH = 1280;
    public static int HEIGHT = 720;
    private Object lock = new Object();
    private boolean destroyed;

    public Multiplayer_MULTITASK_EXAMPLE(Client client) {
        this.client = client;
        networkingDataSingleton = NetworkingDataSingleton.getInstance();
    }

    public void run() {
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

    private void init() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        if ( Platform.get() == Platform.MACOSX ) {
            System.out.println("OSX detected! Changing to hdpi mode");
            long cocoaWindow = glfwGetCocoaWindow(window);

            long objc_msgSend = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend");
            long contentView = invokePPP(objc_msgSend, cocoaWindow, sel_getUid("contentView"));

            invokePPV(objc_msgSend, contentView, sel_getUid("setWantsBestResolutionOpenGLSurface:"), false);

            boolean bool = invokePPZ(objc_msgSend, contentView, sel_getUid("wantsBestResolutionOpenGLSurface"));
            System.out.println("wantsBestResolutionOpenGLSurface = " + bool);
        }

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
            }
        });
        glfwSetFramebufferSizeCallback(window, fsCallback = new GLFWFramebufferSizeCallback() {
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    WIDTH = w;
                    HEIGHT = h;
                }

                System.out.println("glfwSetFramebufferSizeCallback: newWidth:"+ WIDTH +" newHeight:"+ HEIGHT);
            }
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - WIDTH) / 2, (vidmode.height() - HEIGHT) / 2);
        glfwShowWindow(window);
    }

    private void renderLoop() {
        glfwMakeContextCurrent(window);

        GL.createCapabilities();

        glfwSwapInterval(0); //disable vsync
        debugProc = GLUtil.setupDebugMessageCallback();

        scene = new Scene();
        inputHandler = new InputHandler(window,scene);

        float   deltaTime;
        long    lastNanoTime  = 0;

        long lastFpsNanoTime = 0;
        int fps = 0;
        long tempTime;

        while (!destroyed) {
            long time = System.nanoTime();
            deltaTime = (float)(time - lastNanoTime) * 1e-9f;
            lastNanoTime  = time;

            inputHandler.updateInput(deltaTime);
            scene.update(deltaTime);
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

    private void winProcLoop() {
        new Thread(() -> {
            renderLoop();
        }).start();

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
}