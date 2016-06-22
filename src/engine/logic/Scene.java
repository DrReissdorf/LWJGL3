package engine.logic;

import engine.*;
import engine.Light.Sun;
import engine.Light.Light;
import engine.Light.PointLight;
import math.Vec3;
import singleton.HolderSingleton;
import toolbox.ObjLoader;
import util.Texture;

public class Scene {
	private HolderSingleton holder;
	private long window;

	private Renderer newRenderer;
	private Camera mainCamera;

	float mainCameraFov = 70.0f;
	float mainCameraNear   = 0.01f;
	float mainCameraFar    = 500.0f;


	private boolean isLightMoving = true;

	public Scene(long window, int width, int height)	{
		this.window = window;
		newRenderer = new Renderer(width,height);

		holder = HolderSingleton.getInstance();

		mainCamera = new Camera(new Vec3(0.0f, 2.0f, -3.0f), mainCameraFov,width,height,mainCameraNear,mainCameraFar,3f);

		createLight();
		createMeshes();
		createTextures();
		createModelTextures();
        createModels();
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
		holder.addMesh(new ObjLoader().loadObj("Meshes/new_csie_b1.obj").get(0));//7
	}

	private void createTextures() {
		holder.addTexture(new Texture("Textures/dragon.png")); //0
		holder.addTexture(new Texture("Textures/WoodPlanks.jpg"));//1
		holder.addTexture(new Texture("Textures/rock.png"));  //2
		holder.addTexture(new Texture("Textures/Stone.jpg"));  //3
		holder.addTexture(new Texture("Textures/Green.jpg")); //4
	}

	private void createModelTextures() {
		holder.addModelTexture(new ModelTexture(holder.getTexture(0), 0.3f, 20));	// dragon
		holder.addModelTexture(new ModelTexture(holder.getTexture(1), 0f, 0f));	// woodplanks
		holder.addModelTexture(new ModelTexture(holder.getTexture(2), 0.5f, 32));	// rock
		holder.addModelTexture(new ModelTexture(holder.getTexture(3), 0.5f, 32));	// stone
		holder.addModelTexture(new ModelTexture(holder.getTexture(3), 0.5f, 32));	// stone
		holder.addModelTexture(new ModelTexture(null, 1f, 32));	// tree
	}

	private void createModels() {
		holder.addModel(new Model(new Vec3(2,0.5f,0),holder.getMesh(6), null, 1)); // cube
		//holder.addModel(new Model(new Vec3(0,1,0),holder.getMesh(0), holder.getModelTexture(3), 1)); // monkey
		holder.addModel(new Model(new Vec3(-2,0,1),holder.getMesh(1), holder.getModelTexture(0), 1)); // dragon

		//holder.addModel(new Model(new Vec3(0,0.2f,0),holder.getMesh(7), null, 1f,1f)); // complex house
		//holder.addModel(new Model(new Vec3(0,0,0),holder.getMesh(4), null, 5)); // ground_plane

		int value = 4;
		for(int x=-value ; x<=value ; x++) {
			for (int z=-value; z<=value; z++) {
				int multi = 15;
				holder.addModel(new Model(new Vec3(x*multi,0,z*multi),holder.getMesh(4), holder.getModelTexture(1), 20, 1)); // ground_plane
			}
		}

		value = 1;
		for(int x=-value ; x<value ; x++) {
			for (int z=-value; z<value; z++) {
				holder.addModel(new Model(new Vec3(x*5,1,z*5),holder.getMesh(0), holder.getModelTexture(3),1)); // monkey
			}
		}


	}

	private void createLight() {
/*
		for(int x=-3 ; x<=3 ; x++) {
			for (int z=-3; z<=3; z++) {
				holder.addLight(new Light(new Vec3(x*3, 5, z*3), new Vec3(1f, 1f, 1f), 100f, 0.01f, 5));
			}
		}
*/
		holder.setSun(new Sun(new Vec3(3,3,3), new Vec3(1f,1f,1f),1,0.01f,5,0));

		holder.addLight(new PointLight(new Vec3(2,3,-2), new Vec3(0f,1f,0f),5,0.01f,5,0));
		holder.addLight(new PointLight(new Vec3(-2,3,-2), new Vec3(1f,0f,0f),5,0.01f,5,45));
		holder.addLight(new PointLight(new Vec3(2,3,-2), new Vec3(0f,0f,1f),5,0.01f,5,90));
		holder.addLight(new PointLight(new Vec3(-2,3,-2), new Vec3(0f,1f,0f),5,0.01f,5,135));
		holder.addLight(new PointLight(new Vec3(2,3,-2), new Vec3(1f,0f,0f),5,0.01f,5,180));
		holder.addLight(new PointLight(new Vec3(-2,3,-2), new Vec3(0f,0f,1f),5,0.01f,5,225));
    }

	public Camera getMainCamera() {
		return mainCamera;
	}

	public void setLightMoving(boolean lightMoving) {
		isLightMoving = lightMoving;
	}

	public boolean isLightMoving() {
		return isLightMoving;
	}
}
