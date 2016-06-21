package toolbox;

import org.lwjgl.BufferUtils;
import util.Texture;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;

public class FrameBufferFactory {
    public static int setup_Gbuffer(int width, int height, Texture colorSpecTex, Texture normalTex, Texture positionTex, Texture specTex ) {
        int gBufferID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, gBufferID);

        int attachment1 = GL_COLOR_ATTACHMENT0;
        int attachment2 = GL_COLOR_ATTACHMENT1;
        int attachment3 = GL_COLOR_ATTACHMENT2;
        int attachment4 = GL_COLOR_ATTACHMENT3;

        // - Position color buffer
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment1, GL_TEXTURE_2D, colorSpecTex.getID(), 0);

        // - Normal color buffer
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment2, GL_TEXTURE_2D, normalTex.getID(), 0);

        // - Color
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment3, GL_TEXTURE_2D, positionTex.getID(), 0);

        // specvalues
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment4, GL_TEXTURE_2D, specTex.getID(), 0);

        int[] attachments = { attachment1, attachment2, attachment3, attachment4 };
        IntBuffer attribData = BufferUtils.createIntBuffer( attachments.length );
        attribData.put( attachments, 0, attachments.length );
        attribData.flip();

        glDrawBuffers(attribData);

        int err = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if( err != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Frame buffer is not complete. Error: " + err);
            System.exit(-1);
        }

        int depthrenderbuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthrenderbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthrenderbuffer);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return gBufferID;
    }

    public static int setupPostProcessFrameBuffer(int texbuf, int width, int height) {
        int framebuf = glGenFramebuffers();
        glBindFramebuffer( GL_FRAMEBUFFER, framebuf );
        glFramebufferTexture2D( GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texbuf, 0 );

        int depthrenderbuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthrenderbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthrenderbuffer);

        int err = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if( err != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Frame buffer is not complete. Error: " + err);
            System.exit(-1);
        }

        glBindFramebuffer( GL_FRAMEBUFFER, 0 );
        return framebuf;
    }

    public static int setupShadowFrameBuffer(int shadowTextureID) {
        int shadowFrameBufferID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBufferID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowTextureID, 0);
        glReadBuffer(GL_NONE);
        glDrawBuffer(GL_NONE);

        int err = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if( err != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Frame buffer is not complete. Error: " + err);
            System.exit(-1);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return shadowFrameBufferID;
    }
}
