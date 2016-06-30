package engine.gameobjects;

import engine.gameobjects.GameObjectRoot;

public class GameObject {
    private GameObjectRoot root;

    public GameObject(GameObjectRoot root) {
        this.root = root;
        root.getNewGameObjectArrayList().add(this);
    }

    public GameObjectRoot getRoot() {
        return root;
    }

    public void setRoot(GameObjectRoot root) {
        this.root = root;
    }
}
