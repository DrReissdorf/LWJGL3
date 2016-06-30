package toolbox;

import util.Mesh;
import util.OBJContainer;
import util.OBJGroup;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class ObjLoader {
    public static ArrayList<Mesh> loadObj( String filename ) {
        ArrayList<Mesh> meshes = new ArrayList<>();
        OBJContainer objContainer = OBJContainer.loadFile( filename );
        ArrayList<OBJGroup> objGroups    = objContainer.getGroups();

        for( OBJGroup group : objGroups ) {
            float[] positions = group.getPositions();
            float[] normals   = group.getNormals();
            int[]   indices   = group.getIndices();
            float[] textureCoords	  = group.getTexCoords();

            Mesh mesh = new Mesh( GL_STATIC_DRAW );
            mesh.setAttribute( 0, positions, 3 );
            mesh.setAttribute( 1, normals, 3 );
            mesh.setAttribute( 2, textureCoords, 3 );
            mesh.setIndices( indices );

            meshes.add( mesh );
        }

        return meshes;
    }
}
