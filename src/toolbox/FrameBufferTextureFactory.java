package toolbox;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureSRGB;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT2;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

public class FrameBufferTextureFactory {
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

    public static int setupPostProcessTextureBuffer(int width, int height) {
        int texbuf = glGenTextures();
        glBindTexture( GL_TEXTURE_2D, texbuf );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        glTexImage2D( GL_TEXTURE_2D, 0, EXTTextureSRGB.GL_SRGB_EXT, width, height, //EXTTextureSRGB.GL_SRGB_EXT -> linear intensisity as pixel values
                0, GL_RGB, GL_FLOAT, (ByteBuffer)null );
        glBindTexture( GL_TEXTURE_2D, 0 );
        return texbuf;
    }

    public static int[] setupGeometryTextures(int width, int height) {
        // - Position color buffer
        int positionTexID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, positionTexID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, width, height, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture( GL_TEXTURE_2D, 0 );

        // - Normal color buffer
        int normalTexID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, normalTexID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, width, height, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture( GL_TEXTURE_2D, 0 );

        // - Color + Specular color buffer
        int colorSpecTexID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, colorSpecTexID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (FloatBuffer) null);
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture( GL_TEXTURE_2D, 0 );

        int specTexID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, specTexID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, width, height, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture( GL_TEXTURE_2D, 0 );

        int[] ret = {colorSpecTexID, normalTexID, positionTexID, specTexID};
        return ret;
    }

    private static FloatBuffer createFloatBuffer(float[] floats) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(floats.length);
        fb.put(floats,0 , floats.length);
        fb.flip();
        return fb;
    }

    public static IntBuffer createIntBuffer(int[] ints) {
        IntBuffer ib = BufferUtils.createIntBuffer(ints.length);
        ib.put(ints,0, ints.length);
        ib.flip();
        return ib;
    }
}
