package engine.shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import math.Mat3;
import math.Mat4;
import math.Vec2;
import math.Vec3;
import math.Vec4;
import org.lwjgl.BufferUtils;
import util.FileIO;
import util.Texture;

public class MyShaderProgram {
    private final  int maxActiveTextures  = 8;
    private static int activeTexture = 0;

    private HashMap<String, Integer> m_UniformLocations;
    private int                      m_Program;

    public MyShaderProgram(String vertexFile, String fragmentFile) {
        m_UniformLocations = new HashMap<>();

        String vertexSource   = FileIO.readTXT(vertexFile);
        String fragmentSource = FileIO.readTXT( fragmentFile );

        this.createProgram( vertexSource, fragmentSource );
    }


    private void createProgram( String vertexSource, String fragmentSource ) {
        m_Program = glCreateProgram();

        int vertexShader   = this.createShader( vertexSource, GL_VERTEX_SHADER );
        int fragmentShader = this.createShader( fragmentSource, GL_FRAGMENT_SHADER );

        glAttachShader( m_Program, vertexShader );
        glAttachShader( m_Program, fragmentShader );
        glLinkProgram(  m_Program );

        glDeleteShader( vertexShader );
        glDeleteShader( fragmentShader );

        if ( glGetProgrami(m_Program, GL_LINK_STATUS) == GL_FALSE )
        {
            MyShaderProgram.printLog( m_Program );
        }

        glValidateProgram( m_Program );

        if ( glGetProgrami(m_Program, GL_VALIDATE_STATUS) == GL_FALSE )
        {
            MyShaderProgram.printLog( m_Program );
        }
    }


    private int createShader( String shaderSource, int type ) {
        int shader = glCreateShader( type );

        glShaderSource( shader, shaderSource );
        glCompileShader( shader );

        if ( glGetShaderi( shader, GL_COMPILE_STATUS ) == GL_FALSE )
        {
            MyShaderProgram.printLog( shader );
        }

        return shader;
    }

    private int getUniformLocation( String uniformName ) {
        Integer cachedLocation = m_UniformLocations.get( uniformName );

        if( cachedLocation == null )
        {
            int location = glGetUniformLocation( m_Program, uniformName );
            m_UniformLocations.put( uniformName, location );

            return location;
        }

        return cachedLocation;
    }

    private static void printLog( int obj ) {
        IntBuffer iVal = BufferUtils.createIntBuffer( 1 );
        glGetShaderiv( obj, GL_INFO_LOG_LENGTH, iVal );

        int length = iVal.get();

        if (length > 1)
        {
            ByteBuffer infoLog = BufferUtils.createByteBuffer( length );
            iVal.flip();

            glGetShaderInfoLog( obj, iVal, infoLog );

            byte[] infoBytes = new byte[length];
            infoLog.get( infoBytes );

            String out = new String( infoBytes );
            System.out.println( "Shader log:\n" + out );
        }
    }

    public void useProgram()
    {
        glUseProgram( m_Program );
    }


    public void setUniform( String uniformName, int value )
    {
        glUniform1i( this.getUniformLocation(uniformName), value );
    }


    public void setUniform( String uniformName, float value )
    {
        glUniform1f( this.getUniformLocation(uniformName), value );
    }


    public void setUniform( String uniformName, Vec2 vec )
    {
        glUniform2f( this.getUniformLocation(uniformName), vec.x, vec.y );
    }


    public void setUniform( String uniformName, Vec3 vec )
    {
        glUniform3f( this.getUniformLocation(uniformName), vec.x, vec.y, vec.z );
    }


    public void setUniform( String uniformName, Vec4 vec ) {
        glUniform4f( this.getUniformLocation(uniformName), vec.x, vec.y, vec.z, vec.w );
    }


    public void setUniform( String uniformName, Mat3 mat ) {
        glUniformMatrix3fv( this.getUniformLocation(uniformName), false, mat.toFloatBuffer() );
    }


    public void setUniform( String uniformName, Mat4 mat ) {
        glUniformMatrix4fv( this.getUniformLocation(uniformName), false, mat.toFloatBuffer() );
    }

    public void setUniform( String uniformName, Texture texture ) {
        int textureSlot = GL_TEXTURE0 + activeTexture;
        int textureID   = texture.getID();

        glActiveTexture( textureSlot );
        glBindTexture( GL_TEXTURE_2D, textureID );
        glUniform1i( this.getUniformLocation(uniformName), activeTexture );

        activeTexture = (activeTexture + 1) % maxActiveTextures;
    }

    public void setUniform( String uniformName, Vec3[] vecArray ) {
        float[] temp = new float[vecArray.length*3];

        for(int i=0 ; i<vecArray.length ; i++) {
            temp[i*3] = vecArray[i].x;
            temp[i*3+1] = vecArray[i].y;
            temp[i*3+2] = vecArray[i].z;
        }

        glUniform3fv(this.getUniformLocation(uniformName), temp);
    }

    public void setUniform( String uniformName, Vec4[] vecArray ) {
        float[] temp = new float[vecArray.length*4];

        for(int i=0 ; i<vecArray.length ; i++) {
            temp[i*3] = vecArray[i].x;
            temp[i*3+1] = vecArray[i].y;
            temp[i*3+2] = vecArray[i].z;
            temp[i*3+3] = vecArray[i].w;
        }

        glUniform4fv(this.getUniformLocation(uniformName), temp);
    }

    public void setUniform( String uniformName, Mat4[] matArray ) {
        float[] temp = new float[matArray.length*16];

        for(int i=0 ; i<matArray.length ; i++) {
            temp[i*16] = matArray[i].m00;
            temp[i*16+1] = matArray[i].m10;
            temp[i*16+2] = matArray[i].m20;
            temp[i*16+3] = matArray[i].m30;

            temp[i*16+4] = matArray[i].m01;
            temp[i*16+5] = matArray[i].m11;
            temp[i*16+6] = matArray[i].m21;
            temp[i*16+7] = matArray[i].m31;

            temp[i*16+8] = matArray[i].m02;
            temp[i*16+9] = matArray[i].m12;
            temp[i*16+10] = matArray[i].m22;
            temp[i*16+11] = matArray[i].m32;

            temp[i*16+12] = matArray[i].m03;
            temp[i*16+13] = matArray[i].m13;
            temp[i*16+14] = matArray[i].m23;
            temp[i*16+15] = matArray[i].m33;
        }

        glUniformMatrix4fv(this.getUniformLocation(uniformName), false, temp);
    }

    public void setUniform( String uniformName, Mat3[] matArray ) {
        float[] temp = new float[matArray.length*9];

        for(int i=0 ; i<matArray.length ; i++) {
            temp[i*9] = matArray[i].m00;
            temp[i*9+1] = matArray[i].m10;
            temp[i*9+2] = matArray[i].m20;

            temp[i*9+3] = matArray[i].m01;
            temp[i*9+4] = matArray[i].m11;
            temp[i*9+5] = matArray[i].m21;

            temp[i*9+6] = matArray[i].m02;
            temp[i*9+7] = matArray[i].m12;
            temp[i*9+8] = matArray[i].m22;
        }

        glUniformMatrix3fv(this.getUniformLocation(uniformName), false, temp);
    }

    public void setUniform(String uniformName, float[] floats) {
        glUniform1fv( this.getUniformLocation(uniformName), floats);
    }
}
