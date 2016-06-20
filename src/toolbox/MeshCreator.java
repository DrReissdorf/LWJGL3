package toolbox;

import util.Mesh;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class MeshCreator {
    public Mesh createQuad() {
        float[] positions = {
                -1.0f, -1.0f, 0.0f,	//0	lower-left
                -1.0f, 1.0f, 0.0f,	//1 upper-left
                1.0f, 1.0f,0.0f,	//2 upper right
                1.0f, -1.0f,0.0f};	//3 lower right

        int[] indices = {
                0,3,2,
                2,1,0};

        float[] textureCoords = {
                0,0,0,
                0,1,0,
                1,1,0,
                1,0,0};

        Mesh mesh = new Mesh( GL_STATIC_DRAW );
        mesh.setAttribute( 0, positions, 3 );
        mesh.setAttribute( 2, textureCoords, 3 );
        mesh.setIndices( indices );

        return mesh;
    }
}
