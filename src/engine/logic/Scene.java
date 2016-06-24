package engine.logic;

import engine.*;
import engine.Light.DirectionalLight;
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

	private float mainCameraFov = 70.0f;
	private float mainCameraNear   = 0.01f;
	private float mainCameraFar    = 500.0f;

	private float dayTime = 40;
	private float dayTimeIncrease = 0.1f; //0.005f;
	private Vec3 backgroundColor = RgbToFloat(54,155,255);
	private Vec3 originalbackgroundColor = RgbToFloat(54,155,255);

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
		createGameObjects();
	}

	public void draw(float deltaTime) {
		newRenderer.renderScene(dayTime,backgroundColor,mainCamera);

		for (GameObjectRoot gameObjectRoot : holder.getGameObjectRoots()) {
			Light light = gameObjectRoot.getLight();
			if(light != null) {
				if(isLightMoving) light.moveAroundCenter();
			}
		}

		if(dayTime > 360) dayTime = dayTime-360;
		if(isLightMoving) dayTime += dayTimeIncrease;
		fadeBackgroundColor(dayTime,dayTimeIncrease);
		holder.getSun().dayNightCycle(dayTime,dayTimeIncrease);
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
		holder.addTexture(new Texture("Textures/dragon.png")); //0
		holder.addTexture(new Texture("Textures/WoodPlanks.jpg"));//1
		holder.addTexture(new Texture("Textures/rock.png"));  //2
		holder.addTexture(new Texture("Textures/Stone.jpg"));  //3
	}

	private void createModelTextures() {
		holder.addModelTexture(new ModelTexture(holder.getTexture(0), 0.3f, 20));	// dragon
		holder.addModelTexture(new ModelTexture(holder.getTexture(1), 0f, 0f));	// woodplanks
		holder.addModelTexture(new ModelTexture(holder.getTexture(2), 0.5f, 32));	// rock
		holder.addModelTexture(new ModelTexture(holder.getTexture(3), 0.5f, 32));	// stone
		holder.addModelTexture(new ModelTexture(holder.getTexture(3), 0.5f, 32));	// stone
		holder.addModelTexture(new ModelTexture(null, 1f, 200));
	}

	private void createModels() {
		holder.addModel(new Model(holder.getMesh(6), null, 1, 2)); // cube
		holder.addModel(new Model(holder.getMesh(1), holder.getModelTexture(0), 1, 1)); // dragon
		holder.addModel(new Model(holder.getMesh(4), holder.getModelTexture(1), 5, 1)); // ground_plane
		holder.addModel(new Model(holder.getMesh(0), null, 1, 1)); // monkey
		//holder.addModel(new Model(holder.getMesh(7), null, 1f,1f)); // complex house
	}

	private void createGameObjects() {
	//	GameObjectRoot monkey = new GameObjectRoot(new Vec3(0,0,0));
	//	monkey.setModel(holder.getModel(3));
	//	holder.addGameObjectRoot(monkey);

		GameObjectRoot cube = new GameObjectRoot(new Vec3(0,1f,0));
		cube.setModel(holder.getModel(0));
		holder.addGameObjectRoot(cube);

		int value = 4;
		for(int x=-value ; x<=value ; x++) {
			for (int z=-value; z<=value; z++) {
				float multi = 15.7f;
				GameObjectRoot plane = new GameObjectRoot(new Vec3(x*multi,0,z*multi));
				plane.setModel(holder.getModel(2));
				holder.addGameObjectRoot(plane);
			}
		}

		/*value = 1;
		for(int x=-value ; x<=value ; x++) {
			for (int z=-value; z<=value; z++) {
				GameObjectRoot monkey = new GameObjectRoot(new Vec3(x*5,1,z*5));
				monkey.setModel(holder.getModel(3));
				holder.addGameObjectRoot(monkey);
			}
		}*/

		/* SUN */
		GameObjectRoot lightGameObject = new GameObjectRoot(new Vec3(0,3,-1));
		lightGameObject.setModel(holder.getModel(0));
		lightGameObject.setSun(holder.getSun());
		lightGameObject.getSun().setGameObjectRoot(lightGameObject);
		holder.addGameObjectRoot(lightGameObject);

		/* LIGHT GAMEOBJECTS */
		lightGameObject = new GameObjectRoot(new Vec3(-3,4,1));
		lightGameObject.setModel(holder.getModel(0));
		lightGameObject.setLight(holder.getLight(0));
		lightGameObject.getLight().setGameObjectRoot(lightGameObject);
		holder.addGameObjectRoot(lightGameObject);

		lightGameObject = new GameObjectRoot(new Vec3(3,4,1));
		lightGameObject.setModel(holder.getModel(0));
		lightGameObject.setLight(holder.getLight(1));
		lightGameObject.getLight().setGameObjectRoot(lightGameObject);
		//holder.addGameObjectRoot(lightGameObject);

		lightGameObject = new GameObjectRoot(new Vec3(0,4,-1));
		lightGameObject.setModel(holder.getModel(0));
		lightGameObject.setLight(holder.getLight(2));
		lightGameObject.getLight().setGameObjectRoot(lightGameObject);
		//holder.addGameObjectRoot(lightGameObject);
	}

	private void createLight() {
		holder.setSun(new Sun(new Vec3(1f,1f,1f),1)); //new Vec3(1f,1f,1f)
		holder.setDirectionalLight(new DirectionalLight(new Vec3(1f,1f,1f),new Vec3(0,1,0),1));

		holder.addLight(new PointLight(new Vec3(1f,1f,1f),10,0.01f,5,90));
		holder.addLight(new PointLight(new Vec3(3f,0f,0f),10,0.01f,5,180));
		holder.addLight(new PointLight(new Vec3(3f,3f,3f),10,0.01f,5,270));
		holder.addLight(new PointLight(new Vec3(0f,2,0f),10,0.01f,5,135));
		holder.addLight(new PointLight(new Vec3(2,0f,0f),10,0.01f,5,180));
		holder.addLight(new PointLight(new Vec3(0f,0f,10f),10,0.01f,5,225));
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

	public void fadeBackgroundColor(float dayTime, float dayTimeIncrease) {
		float fadeSpeed = dayTimeIncrease/5;
		if(dayTime>180) {
			if(backgroundColor.x > originalbackgroundColor.x/100) backgroundColor.x *= (1-fadeSpeed);
			if(backgroundColor.y > originalbackgroundColor.y/100) backgroundColor.y *= (1-fadeSpeed);
			if(backgroundColor.z > originalbackgroundColor.z/100) backgroundColor.z *= (1-fadeSpeed);

		} else {
			if(backgroundColor.x < originalbackgroundColor.x) backgroundColor.x *= (1+fadeSpeed);
			if(backgroundColor.y < originalbackgroundColor.y) backgroundColor.y *= (1+fadeSpeed);
			if(backgroundColor.z < originalbackgroundColor.z) backgroundColor.z *= (1+fadeSpeed);
		}
	}

	public Vec3 RgbToFloat(int r, int g, int b) {
		return new Vec3(r/255f,g/255f,b/255f);
	}
}
