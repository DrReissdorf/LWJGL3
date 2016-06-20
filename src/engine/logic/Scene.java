package engine.logic;

import engine.*;
import math.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import singleton.HolderSingleton;
import toolbox.ObjLoader;
import util.Texture;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Scene {
	private HolderSingleton holder;
	private boolean isPressed = false;
	private long window;


	private Renderer renderer;
	private NewRenderer newRenderer;
	private Camera mainCamera;

	float mainCameraFov = 70.0f;
	float mainCameraNear   = 0.01f;
	float mainCameraFar    = 500.0f;



	private boolean isLightMoving = false;

	public Scene(long window, int width, int height)	{
		this.window = window;
		renderer = new Renderer(width,height);
		newRenderer = new NewRenderer(width,height);

		holder = HolderSingleton.getInstance();

		mainCamera = new Camera(new Vec3(0.0f, 2.0f, -3.0f), mainCameraFov,width,height,mainCameraNear,mainCameraFar,3f);

		createLight();
		createMeshes();
		createTextures();
		createModelTextures();
        createModels();

		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
	}

	public void draw() {
		newRenderer.renderScene(mainCamera);
		for(Light light : holder.getLights()) if(isLightMoving) light.moveAroundCenter();
	}

	private void createMeshes() {
		holder.addMesh(new ObjLoader().loadObj("Meshes/monkey.obj").get(0)); //0
		holder.addMesh(new ObjLoader().loadObj("Meshes/dragon.obj").get(0)); //1
		holder.addMesh(new ObjLoader().loadObj("Meshes/screen.obj").get(0)); //2
		holder.addMesh(new ObjLoader().loadObj("Meshes/cam.obj").get(0)); //3
		holder.addMesh(new ObjLoader().loadObj("Meshes/ground_plane.obj").get(0)); //4
		holder.addMesh(new ObjLoader().loadObj("Meshes/monkey_scene.obj").get(0)); //5
		holder.addMesh(new ObjLoader().loadObj("Meshes/cube.obj").get(0));//6
	}

	private void createTextures() {
		holder.addTexture(new Texture("Textures/dragon.png"));
		holder.addTexture(new Texture("Textures/WoodPlanks.jpg"));
		holder.addTexture(new Texture("Textures/rock.png"));
		holder.addTexture(new Texture("Textures/Stone.jpg"));
	}

	private void createModelTextures() {
		holder.addModelTexture(new ModelTexture(holder.getTexture(0), 0.5f, 32));	// dragon
		holder.addModelTexture(new ModelTexture(holder.getTexture(1), 0.1f, 32));	// woodplanks
		holder.addModelTexture(new ModelTexture(holder.getTexture(2), 0.5f, 32));	// rock
		holder.addModelTexture(new ModelTexture(holder.getTexture(3), 0.5f, 32));	// stone
	}

	private void createModels() {
		holder.addModel(new Model(new Vec3(0,1,0),holder.getMesh(6), null, 1)); // cube
		//holder.addModel(new Model(new Vec3(0,1,0),holder.getMesh(0), holder.getModelTexture(3), 1)); // monkey
		holder.addModel(new Model(new Vec3(-2,0,1),holder.getMesh(1), holder.getModelTexture(0), 1)); // dragon
		holder.addModel(new Model(new Vec3(0,0,0),holder.getMesh(4), holder.getModelTexture(1), 5)); // ground_plane
		//holder.addModel(new Model(new Vec3(0,0,0),holder.getMesh(4), null, 5)); // ground_plane


		/*
		for(int x=-2 ; x<=2 ; x++) {
			for (int z=-2; z<=2; z++) {
				holder.addModel(new Model(new Vec3(x*3,1,z*3),holder.getMesh(0), holder.getModelTexture(3),1)); // monkey
			}
		}
		*/

	}

	private void createLight() {
/*
		for(int x=-3 ; x<=3 ; x++) {
			for (int z=-3; z<=3; z++) {
				holder.addLight(new Light(new Vec3(x*3, 5, z*3), new Vec3(1f, 1f, 1f), 100f, 0.01f, 5));
			}
		}
*/
		holder.addLight(new Light(new Vec3(-5,4,3), new Vec3(1f,1f,1f),15f,0.01f,5,0));
		holder.addLight(new Light(new Vec3(5,4,3), new Vec3(1f,1f,1f),15f,0.01f,5,180));
    }

	public Camera getMainCamera() {
		return mainCamera;
	}
}
