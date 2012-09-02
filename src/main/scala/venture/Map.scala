package venture
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
import MapSettings._
import scala.util.Random
import scala.collection.mutable.HashMap
import simplex3d.math.Vec2i

object MapSettings{
	val chunkSize = 16;
	val tileSize = 16;
}

object ChunkShader{
	val vertexShaderSrc = """
#version 330
in vec4 in_position;
in int in_tileID;
		
uniform vec2 u_offset;
// scaling to fit for the resolution
uniform vec4 u_scale;
		
flat out float v_tileID;

void main()
{
	v_tileID = in_tileID;
    gl_Position = (in_position+vec4(u_offset,0,0))*u_scale;
}
"""
	
	val fragmentShaderSrc = """
#version 330
flat in float v_tileID;
		
uniform sampler2DArray u_arrayTexture;
uniform float u_fade;
		
out vec4 outputColor;
		
void main()
{
    outputColor = vec4(vec3(u_fade),1) * texture(u_arrayTexture, vec3(gl_PointCoord, v_tileID ));
}
"""
	import Shader.{createProgram, createShader}
	
	val shaderList = List(createShader(GL_VERTEX_SHADER, vertexShaderSrc), createShader(GL_FRAGMENT_SHADER, fragmentShaderSrc))
    val theProgram = createProgram(shaderList);
	shaderList foreach glDeleteShader
	
	val arrayTextureLoc = glGetUniformLocation(theProgram, "u_arrayTexture");
	val offsetLoc       = glGetUniformLocation(theProgram, "u_offset");
	val scaleLoc        = glGetUniformLocation(theProgram, "u_scale");
	val fadeLoc         = glGetUniformLocation(theProgram, "u_fade");
	val tileIdLoc       = glGetAttribLocation(theProgram, "in_tileID");
	val positionLoc     = glGetAttribLocation(theProgram, "in_position");
	
}

object Foreground extends       Map( 2, 1.0,  new Generator( (x:Int,y:Int) => noise.noise3(x.toDouble/chunkSize, y.toDouble/chunkSize, 50.5) ))
object DirectBackground extends Map( 2, 0.65, new Generator( (x:Int,y:Int) => noise.noise3(x.toDouble/chunkSize, y.toDouble/chunkSize, 50.5) ))
object Background extends       Map( 1, 0.5,  new Generator( (x:Int,y:Int) => noise.noise3(x.toDouble/chunkSize, y.toDouble/chunkSize, 100.5) ))

class Map(val tileScale:Int, val fade:Double, val generator:Generator) {
	
	def div(x:Int,y:Int) = 
		if(x>=0) x/y else (x-y+1)/y
	def mod(x:Int,y:Int) =
		(x%y+y)%y
	
	val hashMap = new HashMap[(Int,Int),Chunk]
	
	def get(x:Int,y:Int) = {
		hashMap.getOrElseUpdate((x,y),generator(x,y))
	}
	
	def drawRect(centerX:Double, centerY:Double, halfWidth:Double, halfHeight:Double) = {
		val left = (centerX-halfWidth).floor.toInt
		val right= (centerX+halfWidth).ceil.toInt
		val bottom=(centerY-halfHeight).floor.toInt
		val top  = (centerY+halfHeight).ceil.toInt
		
		glPointSize(tileSize*tileScale)
		
		for(x <- div(left,chunkSize) to div(right,chunkSize); y <- div(bottom,chunkSize) to div(top,chunkSize)) {
			get(x,y).draw(x*chunkSize-centerX, y*chunkSize-centerY, tileScale, fade)
		}
	}
	
	def apply(x:Int,y:Int):Short         = get(div(x,chunkSize), div(y,chunkSize)).apply(mod(x,chunkSize),mod(y,chunkSize))
	def update(x:Int,y:Int,v:Short):Unit = get(div(x,chunkSize), div(y,chunkSize)).update(mod(x,chunkSize),mod(y,chunkSize), v)	
}

object Chunk {
	val positionBuffer = {
		val data = BufferUtils.createFloatBuffer(chunkSize*chunkSize*4)
		for(x <- 0 until chunkSize; y <- 0 until chunkSize){
			data put x+0.5f
			data put y+0.5f
			data put 0
			data put 1
		}
		data.flip()
		
		val glBuffer = glGenBuffers()
		glBindBuffer(GL_ARRAY_BUFFER, glBuffer)
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
		glBindBuffer(GL_ARRAY_BUFFER, 0)
		glBuffer
	}
}

class Chunk {
	val data = BufferUtils.createShortBuffer(chunkSize*chunkSize)
	
	val buffer = glGenBuffers()
	glBindBuffer(GL_ARRAY_BUFFER, buffer)
	glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
	glBindBuffer(GL_ARRAY_BUFFER, 0)
	
	var changed = false
	
	def apply(x:Int,y:Int)               = data.get(x*chunkSize+y)
	def update(x:Int,y:Int,v:Short):Unit = {data.put(x*chunkSize+y, v); changed = true}
	
	def draw(posX:Double,posY:Double,tileScale:Int,fade:Double) {
		glUseProgram(ChunkShader.theProgram);
		glBindBuffer(GL_ARRAY_BUFFER, Chunk.positionBuffer);
		glEnableVertexAttribArray(ChunkShader.positionLoc);
		glVertexAttribPointer(ChunkShader.positionLoc, 4, GL_FLOAT, true, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		if(changed)
			glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
		glEnableVertexAttribArray(ChunkShader.tileIdLoc)
		glVertexAttribIPointer(ChunkShader.tileIdLoc, 1, GL_SHORT, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		glUniform1i(ChunkShader.arrayTextureLoc, 0);
		glUniform2f(ChunkShader.offsetLoc, posX.toFloat, posY.toFloat)
		glUniform4f(ChunkShader.scaleLoc, tileSize * tileScale * 2.0f / LwjglApp.width , tileSize * tileScale * 2.0f / LwjglApp.height , 1, 1)
		glUniform1f(ChunkShader.fadeLoc, fade.toFloat)
		
		glDrawArrays(GL_POINTS, 0, chunkSize*chunkSize);
		
		glDisableVertexAttribArray(0);
		glUseProgram(0);
	}
}


case class Cond( offX:Int, offY:Int, value:Int )
case class Repl( value:Short, conditions:Cond* )

class Generator(val worldfunction: (Int,Int) => Double) {
	
	val replacements = Seq( 
			Repl( 8, Cond(0,0,1), Cond(-1,0,0), Cond(0,1,0), Cond(1,0,1), Cond(1,1,0) ),
			Repl( 9, Cond(0,0,1), Cond(-1,0,1), Cond(-2,0,0), Cond(-1,1,0), Cond(0,1,0) ),
			
			Repl( 4, Cond(0,0,1), Cond(1,0,0), Cond(0,1,0), Cond(-1,0,1), Cond(-1,1,0) ),
			Repl( 3, Cond(0,0,1), Cond(1,0,1), Cond(2,0,0), Cond(1,1,0), Cond(0,1,0) ),
			
			Repl( 5, Cond(0,0,1), Cond(-1,0,1), Cond(0,1,0), Cond(1,0,0) ),
			Repl( 7, Cond(0,0,1), Cond(-1,0,0), Cond(0,1,0), Cond(1,0,1) ),
			Repl( 2, Cond(0,0,1), Cond(0,1,0) )
	)
	
	def apply(posX:Int,posY:Int) = {
		val offsetX = posX*chunkSize
		val offsetY = posY*chunkSize
		
		def depth(x:Int,y:Int):Short = 
			if( 0 < worldfunction(offsetX+x,offsetY+y) ) 0 else 1
		
		val chunk = new Chunk
		for(x <- 0 until chunkSize; y <- 0 until chunkSize) {
			val replacement = replacements.find( _.conditions.find( cond => depth(x+cond.offX, y+cond.offY) != cond.value ) == None )
			replacement match {
				case None => chunk(x,y) = depth(x,y)
				case Some( r ) => chunk(x,y) = r.value
			}
		}
		
		chunk
	}
}

