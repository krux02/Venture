package rendering

import javax.imageio.ImageIO
import java.io.File
import java.awt.image.Raster
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.{glGetInteger => _, _}
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL21._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL31._
import org.lwjgl.opengl.GL32._
import org.lwjgl.opengl.GL33._

class Texture(val id:Int, val target:Int = GL_TEXTURE_2D ) {
	def bind() = glBindTexture(target, id)
}

object Texture {
	val tiles = tile(16,16,"simple.png")
	val playerraster = getRaster("player.png")
	val playerTexture = tile(0,0,16,32,2,2,vertical = false, data = playerraster)
	
	private def getRaster(filename:String):Raster = try{ 
		ImageIO.read(new File("textures",filename)).getData 
	} catch {
		case x:Any =>
			println(new File("textures",filename))
			throw x
	}
	
	private def tile(w:Int,h:Int,filename:String):Texture = {
		val image = ImageIO.read(new File("textures",filename))
		val data:Raster = image.getData
		val count    = (data.getHeight/h) * (data.getWidth/w)
		val rowLimit = data.getHeight/h
		tile(0,0,w,h,25,rowLimit,vertical = true, data = data)
	}
	
	private def tile(x:Int,y:Int,w:Int,h:Int,count:Int,rowLimit:Int,vertical:Boolean,data:Raster) = {
		
		val dataArray = new Array[Int](w*h*4)
		val newData = BufferUtils.createByteBuffer(w*h*count*4)
		for(i <- 0 until count) {
			val ix = x + (if(vertical) i/rowLimit else i%rowLimit)
			val iy = y + (if(vertical) i%rowLimit else i/rowLimit)
			
			data.getPixels(ix*w, iy*h, w, h, dataArray)
			for(i <- 0 until w*h) {
				newData put dataArray(i*4+3).toByte
				newData put dataArray(i*4+2).toByte
				newData put dataArray(i*4+1).toByte
				newData put dataArray(i*4+0).toByte
			}
		}
		newData.flip

		val texture = glGenTextures()
		assert(texture != 0)
		glBindTexture(GL_TEXTURE_2D_ARRAY, texture)
		glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_WRAP_S,GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_WRAP_T,GL_REPEAT);
		glTexImage3D(GL_TEXTURE_2D_ARRAY,0,4,w,h,count,0,GL_RGBA,GL_UNSIGNED_INT_8_8_8_8,newData);
		
		new Texture(id = texture, target = GL_TEXTURE_2D_ARRAY)
	}
	private def loadImage(filename:String):Texture = loadImage(getRaster(filename))
	private def loadImage(raster:Raster):Texture = {
		val width = raster.getWidth
		val height = raster.getHeight
		assert( ((width-1) & width) == 0)
		assert( ((height-1) & height) == 0)
		
		val data = BufferUtils.createByteBuffer(width*height*4)
		
		val dataArray = raster.getPixels(0,0,width,height,new Array[Int](width*height*3))
		
		for(i <- 0 until width*height){
			data put (-1).toByte
			data put dataArray(i*3+2).toByte
			data put dataArray(i*3+1).toByte
			data put dataArray(i*3+0).toByte
		}
		
		data.flip
		
		val texture = glGenTextures()
		assert(texture != 0)
		
		glBindTexture(GL_TEXTURE_2D, texture)
		glTexImage2D(GL_TEXTURE_2D,0,4,width,height,0,GL_RGBA,GL_UNSIGNED_INT_8_8_8_8,data)
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0)
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 8)
	    glGenerateMipmap(GL_TEXTURE_2D)
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_NEAREST);
		
		new Texture(id = texture, target = GL_TEXTURE_2D)
	}
	
	private def loadSkyBox(filename:String):Texture = {
		val raster = ImageIO.read(new File("textures",filename)).getData
		val dataWidth = raster.getWidth
		val dataHeight = raster.getHeight
		
		val width = dataWidth/4
		val height = dataHeight/2
		
		val dataArray = new Array[Int](width*height*4)
		val allData =
		for( (x,y,rotation) <- Vector((0,0,1), (2,0,3), (1,0,2), (3,0,0), (0,1,2), (1,1,0)) ) yield {
			val data = BufferUtils.createByteBuffer(width*height*4)
			raster.getPixels(x*width, y*width, width, height, dataArray)
			
			def map(x:Int,y:Int) = rotation match{
				case 0 => x + width * y
				case 1 => width-y-1 + width * x
				case 2 => width-x-1 + width * (width-y-1)
				case 3 => y + width*(width-x-1)
			}
			
			for( y <- 0 until height; x <- 0 until width) {
				val index = map(x,y)*3
				data put dataArray(index+2).toByte
				data put dataArray(index+1).toByte
				data put dataArray(index+0).toByte
				data put 255.toByte
			}
			data.flip
			data
		}
		
		val textureID = glGenTextures
		glBindTexture(GL_TEXTURE_CUBE_MAP, textureID)
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
		//Define all 6 faces
		glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, allData(0) )
		glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, allData(1) )
		glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, allData(2) )
		glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, allData(3) )
		glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, allData(4) )
		glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, allData(5) )
		
		new Texture(id = textureID, target = GL_TEXTURE_CUBE_MAP)
	}
}

