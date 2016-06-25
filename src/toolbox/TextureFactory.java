package toolbox;

import org.lwjgl.BufferUtils;
import util.Texture;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL21.GL_SRGB;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;

public class TextureFactory {
    public static int generateCubeTexture(int width, int height) {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID );
        glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer) null );
        glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer) null );
        glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer) null );
        glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer) null );
        glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer) null );
        glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer) null );
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        glBindTexture(GL_TEXTURE_2D, 0);
        return textureID;
    }

    public static int setupShadowMapTextureBuffer(int width, int height) {
        int shadowTextureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, shadowTextureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (FloatBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

        //Create floatbuffer to represent whitecolor for border_color
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, createFloatBuffer(new float[]{1,1,1,1}));

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        glBindTexture(GL_TEXTURE_2D, 0);

        return shadowTextureID;
    }

    public static Texture createRGB16F_Texture(int width, int height) {
        int texbuf = glGenTextures();
        glBindTexture( GL_TEXTURE_2D, texbuf );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        glTexImage2D( GL_TEXTURE_2D, 0, GL_RGB16F, width, height, //EXTTextureSRGB.GL_SRGB_EXT -> linear intensisity as pixel values
                0, GL_RGB, GL_FLOAT, (ByteBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );

        return new Texture(texbuf);
    }

    public static Texture createSRGB_Texture(int width, int height) {
        int texbuf = glGenTextures();
        glBindTexture( GL_TEXTURE_2D, texbuf );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        glTexImage2D( GL_TEXTURE_2D, 0, GL_SRGB, width, height, //EXTTextureSRGB.GL_SRGB_EXT -> linear intensisity as pixel values
                0, GL_RGB, GL_FLOAT, (ByteBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );

        return new Texture(texbuf);
    }

    private static FloatBuffer createFloatBuffer(float[] floats) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(floats.length);
        fb.put(floats,0 , floats.length);
        fb.flip();
        return fb;
    }
}
