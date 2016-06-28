
package util;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.EXTABGR.GL_ABGR_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL30.GL_R8;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import javax.imageio.ImageIO;



public class Texture
{
	private static HashMap<String,Integer> TextureIDs = new HashMap<String,Integer>();
	
	private int textureID;
	
	
	
	public Texture( String filename )
	{
		if( !TextureIDs.containsKey(filename) )
		//	loadTexture( filename );
			lwjglLoadTexture(filename);
		//load(filename);
		this.textureID = TextureIDs.get( filename );
	}
	
	
	public Texture( int textureID )
	{
		this.textureID = textureID;
	}
	
	
	public int getID()
	{
		return this.textureID;
	}
	
	private static void lwjglLoadTexture(String fileName) {
		String path = FileIO.pathOf( fileName );

		IntBuffer w = BufferUtils.createIntBuffer(1);
		IntBuffer h = BufferUtils.createIntBuffer(1);
		IntBuffer comp = BufferUtils.createIntBuffer(1);

		stbi_set_flip_vertically_on_load(1);
		ByteBuffer image = stbi_load(path, w, h, comp, 4);
		if (image == null) {
			throw new RuntimeException("Failed to load a texture file!"
					+ System.lineSeparator() + stbi_failure_reason());
		}


		TextureIDs.put( fileName, 0 );

		int width = w.get();
		int height = h.get();

		int textureID = glGenTextures();
		TextureIDs.put( fileName, textureID );
		glActiveTexture( GL_TEXTURE0 );
		glBindTexture( GL_TEXTURE_2D, textureID );
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR );
		glGenerateMipmap( GL_TEXTURE_2D );
		glBindTexture( GL_TEXTURE_2D, 0 );
	}

	private static void loadTexture( String filename )
	{
		try
		{
			String path = FileIO.pathOf( filename );
			BufferedImage image;
			
			if( path.endsWith(".tga") )
				image = loadTGA( new File(path) );
			else
				image = ImageIO.read( new File(path) );
			
			int width = image.getWidth();
			int height = image.getHeight();
			
			DataBuffer dataBuffer = image.getRaster().getDataBuffer();
			byte[]     data       = null;
			
			if( dataBuffer.getDataType() == DataBuffer.TYPE_BYTE )
			{
				data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			}
			else if( dataBuffer.getDataType() == DataBuffer.TYPE_INT )
			{
				int[] intData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
				
				int srcLength = intData.length;
				data = new byte[srcLength << 2];
			    
			    for( int i = 0; i < srcLength; ++i )
			    {
			        int x = intData[i];
			        int j = i << 2;
			        data[j++] = (byte) ((x >>> 0) & 0xff);           
			        data[j++] = (byte) ((x >>> 8) & 0xff);
			        data[j++] = (byte) ((x >>> 16) & 0xff);
			        data[j++] = (byte) ((x >>> 24) & 0xff);
			    }
			}
			else
			{
				System.err.println( "TexturePool : image datatype not supported!" );
				TextureIDs.put( filename, 0 );
				return;
			}
			
			
			
			ByteBuffer imageBuffer = ByteBuffer.allocateDirect( data.length );
		    imageBuffer.order( ByteOrder.nativeOrder() );
		    imageBuffer.put( data, 0, data.length );
		    imageBuffer.flip();
		    
		    int textureID = glGenTextures();
		    TextureIDs.put( filename, textureID );
		    
		    int internalFormat = GL_RGB;
		    int format         = GL_RGB;
		    
		    switch( image.getType() )
		    {
		    	case BufferedImage.TYPE_BYTE_GRAY:
		    	{
		    		internalFormat = GL_R8;
		    		format         = GL_RED;
		    		break;
		    	}
		    	case BufferedImage.TYPE_3BYTE_BGR:
		    	{
		    		internalFormat = GL_RGB8;
		    		format         = GL_BGR;
		    		break;
		    	}
		    	case BufferedImage.TYPE_4BYTE_ABGR:
		    	{
		    		internalFormat = GL_RGBA8;
		    		format         = GL_ABGR_EXT;
		    		break;
		    	}
		    	case BufferedImage.TYPE_INT_BGR:
		    	{
		    		internalFormat = GL_RGB8;
		    		format         = GL_BGR;
		    		break;
		    	}
		    	case BufferedImage.TYPE_INT_RGB:
		    	{
		    		internalFormat = GL_RGB8;
		    		format         = GL_RGB;
		    		break;
		    	}
		    	case BufferedImage.TYPE_INT_ARGB:
		    	{
		    		internalFormat = GL_RGBA8;
		    		format         = GL_RGBA;
		    		System.err.println( "ARGB Textures are not supported, please convert the folowing texture:\n\n" + filename );
		    		
		    		break;
		    	}
		    	default:
		    	{
		    		break;
		    	}
		    }
		    
		    glActiveTexture( GL_TEXTURE0 );
		    glBindTexture( GL_TEXTURE_2D, textureID );
		    
		    glTexImage2D( GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, imageBuffer );
		    glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
		    glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
		    glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
		    glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR );
			//glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, Config.getFloat("TextureAnisotropy") );
		    glGenerateMipmap( GL_TEXTURE_2D );
		    glBindTexture( GL_TEXTURE_2D, 0 );
		}
		catch( IOException e )
		{
		    TextureIDs.put( filename, 0 );
			e.printStackTrace();
		}
	}
	
	private static int offset;

    private static int btoi(byte b) {
            int a = b;
            return (a<0?256+a:a);
    }

    private static int read(byte[] buf) {
        return btoi(buf[offset++]);
    }

    public static BufferedImage loadTGA( File file ) throws IOException
    {
        byte[] buf  = new byte[(int)file.length()];
        
        BufferedInputStream bis = new BufferedInputStream( new FileInputStream(file) );
        bis.read(buf);
        bis.close();
        
        offset = 0;

        // Reading header bytes
        // buf[2]=image type code 0x02=uncompressed BGR or BGRA
        // buf[12]+[13]=WIDTH
        // buf[14]+[15]=HEIGHT
        // buf[16]=image pixel size 0x20=32bit, 0x18=24bit 
        // buf{17]=Image Descriptor Byte=0x28 (00101000)=32bit/origin upperleft/non-interleaved
        for (int i=0;i<12;i++)
                read(buf);
        int width = read(buf)+(read(buf)<<8);   // 00,04=1024
        int height = read(buf)+(read(buf)<<8);  // 40,02=576
        read(buf);
        read(buf);

        int n = width*height;
        int[] pixels = new int[n];
        int idx=0;

        if (buf[2]==0x02 && buf[16]==0x20) { // uncompressed BGRA
            while(n>0) {
                int b = read(buf);
                int g = read(buf);
                int r = read(buf);
                int a = read(buf);
                int v = (a<<24) | (r<<16) | (g<<8) | b;
                pixels[idx++] = v;
                n-=1;
            }
        } else if (buf[2]==0x02 && buf[16]==0x18) {  // uncompressed BGR
            while(n>0) {
                int b = read(buf);
                int g = read(buf);
                int r = read(buf);
                int a = 255; // opaque pixel
                int v = (a<<24) | (r<<16) | (g<<8) | b;
                pixels[idx++] = v;
                n-=1;
            }
        } else {
            // RLE compressed
            while (n>0) {
                int nb = read(buf); // num of pixels
                if ((nb&0x80)==0) { // 0x80=dec 128, bits 10000000
                    for (int i=0;i<=nb;i++) {
                        int b = read(buf);
                        int g = read(buf);
                        int r = read(buf);
                        pixels[idx++] = 0xff000000 | (r<<16) | (g<<8) | b;
                    }
                } else {
                    nb &= 0x7f;
                    int b = read(buf);
                    int g = read(buf);
                    int r = read(buf);
                    int v = 0xff000000 | (r<<16) | (g<<8) | b;
                    for (int i=0;i<=nb;i++)
                        pixels[idx++] = v;
                }
                n-=nb+1;
            }
        }

        BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        bimg.setRGB(0, 0, width,height, pixels, 0,width);
        return bimg;
    }
}






     

