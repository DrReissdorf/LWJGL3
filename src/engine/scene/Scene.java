package engine.scene;

import engine.gameobjects.GameObjectRoot;
import engine.gameobjects.Light.Light;
import engine.gameobjects.Light.PointLightTest;
import engine.gameobjects.Light.Sun;
import engine.gameobjects.Model;
import engine.gameobjects.ModelTexture;
import engine.graphics.Renderer;
import math.Vec3;
import singleton.HolderSingleton;
import toolbox.ObjLoader;
import util.Texture;

public class Scene {
	private HolderSingleton holder;

	private Renderer newRenderer;

	private float dayTime = 40;
	private float dayTimeIncrease = 5f; //0.005f;
	private Vec3 backgroundColor = RgbToFloat(54,155,255);
	private Vec3 originalbackgroundColor = RgbToFloat(54,155,255);

	private boolean isLightMoving = false;

	public Scene()	{
		newRenderer = new Renderer();

		holder = HolderSingleton.getInstance();

		createLight();
		createMeshes();
		createTextures();
		createModelTextures();
		createGameObjects();
	}

	public void update(float deltaTime) {
		for (GameObjectRoot gameObjectRoot : holder.getGameObjectRootTests()) {
			for(Light light : gameObjectRoot.getAllLights()) {
				if(light != null) {
					if(isLightMoving) light.moveAroundCenter();
				}
			}
		}

		if(dayTime > 360) dayTime = (dayTime-360)*deltaTime;
		if(isLightMoving) dayTime += dayTimeIncrease*deltaTime;
		fadeBackgroundColor(dayTime,dayTimeIncrease*deltaTime);
		holder.getSun().dayNightCycle(dayTime,dayTimeIncrease*deltaTime);
	}

	public void render(float deltaTime) {
		newRenderer.renderScene(dayTime,backgroundColor);
	}

	private void createMeshes() {
		ObjLoader objLoader = new ObjLoader();
		holder.addMesh(objLoader.loadObj("Meshes/monkey.obj").get(0)); //0
		holder.addMesh(objLoader.loadObj("Meshes/dragon.obj").get(0)); //1
		holder.addMesh(objLoader.loadObj("Meshes/screen.obj").get(0)); //2
		holder.addMesh(objLoader.loadObj("Meshes/cam.obj").get(0)); //3
		holder.addMesh(objLoader.loadObj("Meshes/ground_plane.obj").get(0)); //4
		holder.addMesh(objLoader.loadObj("Meshes/monkey_scene.obj").get(0)); //5
		holder.addMesh(objLoader.loadObj("Meshes/cube.obj").get(0));//6
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

	private void createGameObjects() {
	/*	GameObjectRoot monkey = new GameObjectRoot(new Vec3(-2,1,2), 1.5f);
		monkey.setModel(holder.getModel(3));
		holder.addGameObjectRoot(monkey);

		GameObjectRoot cube = new GameObjectRoot(new Vec3(1,0.5f,0),1);
		cube.setModel(holder.getModel(0));
		holder.addGameObjectRoot(cube);

		int value = 4;
		for(int x=-value ; x<=value ; x++) {
			for (int z=-value; z<=value; z++) {
				float multi = 15.7f;
				GameObjectRoot plane = new GameObjectRoot(new Vec3(x*multi,0,z*multi),2);
				plane.setModel(holder.getModel(2));
				holder.addGameObjectRoot(plane);
			}
		} */

		GameObjectRoot monkey = new GameObjectRoot(new Vec3(-2,1,2));
		new Model(monkey,holder.getMesh(0), null, 1, 1);
		holder.getGameObjectRootTests().add(monkey);

		GameObjectRoot cube = new GameObjectRoot(new Vec3(1,0.5f,0));
		new Model(cube,holder.getMesh(6), null, 1, 1);
		holder.getGameObjectRootTests().add(cube);

		int value = 4;
		for(int x=-value ; x<=value ; x++) {
			for (int z=-value; z<=value; z++) {
				float multi = 15.7f;
				GameObjectRoot plane = new GameObjectRoot(new Vec3(x*multi,0,z*multi));
				new Model(plane,holder.getMesh(4), null, 5, 1); //holder.getModelTexture(1)
				holder.getGameObjectRootTests().add(plane);
			}
		}


		/* SUN */
		Sun sun = new Sun(new Vec3(0,3,-3), new Vec3(2f,2f,2f));
		holder.setSun(sun);

		/* LIGHT GAMEOBJECTS */
		GameObjectRoot lightGameObject = new GameObjectRoot(new Vec3(-3,4,1));
		new Model(lightGameObject,holder.getMesh(6), null, 1, 0.3f);
		new PointLightTest(lightGameObject,new Vec3(0,0.2f,0),RgbToFloat(156,42,0),1,15,0.01f,5,90);
		holder.getGameObjectRootTests().add(lightGameObject);

	/*	lightGameObject = new GameObjectRoot(new Vec3(3,4,1),1);
		lightGameObject.setModel(holder.getModel(0));
		lightGameObject.setLight(holder.getLight(1));
		lightGameObject.getLight().setGameObjectRoot(lightGameObject);
		//holder.addGameObjectRoot(lightGameObject);

		lightGameObject = new GameObjectRoot(new Vec3(0,4,-1),1);
		lightGameObject.setModel(holder.getModel(0));
		lightGameObject.setLight(holder.getLight(2));
		lightGameObject.getLight().setGameObjectRoot(lightGameObject);
		//holder.addGameObjectRoot(lightGameObject); */
	}

	private void createLight() {
		//holder.setSun(new Sun(new Vec3(1f,1f,1f),1)); //new Vec3(1f,1f,1f)

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
