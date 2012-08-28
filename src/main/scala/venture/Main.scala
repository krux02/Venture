package venture

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
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.input.Keyboard
import simplex3d.math.double._
import java.nio.ByteBuffer
import org.lwjgl.util.glu.GLU
import Tools._
import org.lwjgl.BufferUtils
import scala.util.Random
import org.lwjgl.input.Mouse
import MapSettings.tileSize
import simplex3d.math.Vec2i

object Tools {
	def buffer(vertices: Vec4*) = {
		val b = ByteBuffer.allocateDirect(vertices.size*4*4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer()
		for( v <- vertices ) {
			b put v.x.toFloat
			b put v.y.toFloat
			b put v.z.toFloat
			b put v.w.toFloat
		}
		b.flip()
		b
	}
	
	def error{
		val err = glGetError()
		if( err != GL_NO_ERROR )
			println(GLU.gluErrorString(err))
	}
}


object TestShader {
	val vertexShaderSrc = """
#version 330
layout(location = 0) in vec4 position;

void main()
{
    gl_Position = position*vec4(1.0/20.0, 1.0/15.0, 1, 1)*4-vec4(0,0,0,0);
}
"""
	
	val fragmentShaderSrc = """
#version 330
uniform sampler2DArray arrayTexture;
		
out vec4 outputColor;
void main()
{
    outputColor = texture(arrayTexture, vec3(gl_PointCoord, gl_PrimitiveID % 100 ));
}
"""
	import Shader.{createProgram, createShader}
	
	val shaderList = List(createShader(GL_VERTEX_SHADER, vertexShaderSrc), createShader(GL_FRAGMENT_SHADER, fragmentShaderSrc))
    val theProgram = createProgram(shaderList);
	shaderList foreach glDeleteShader
	
	val texLoc = glGetUniformLocation(theProgram, "arrayTexture");
}

object LwjglApp { 
	var running = true;
	
	val width  = 640
	val height = 480
	
	var posX,posY = 0.0
	
	def mouseWordPos = {
		val x = posX + (Mouse.getX -  width*0.5) / tileSize
		val y = posY + (Mouse.getY - height*0.5) / tileSize
		Vec2(x,y)
	}
	
	def main(args: Array[String]){
		Display.setDisplayMode(new DisplayMode(width,height))
		Display.create
		
		glEnable(GL_POINT_SPRITE)
		glPointSize(tileSize)
		
		glEnable(GL_BLEND)
		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA)
		
		glActiveTexture(GL_TEXTURE0)
		Texture.tiles.bind
		
		var current:Short = 0;
		
		while(running) {
			Display.update
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			if(Display.isCloseRequested || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
				running = false
				
			val mousePos = mouseWordPos
			val mouseX = mousePos.x.floor.toInt
			val mouseY = mousePos.y.floor.toInt
			
			val offsetX = (width*0.5 / tileSize)
			val offsetY = (height*0.5 / tileSize)
			
			posX += (mousePos.x-posX)*0.001;
			posY += (mousePos.y-posY)*0.001;

			
			Map.drawRect(posX,posY,offsetX,offsetY)
			if(Mouse.isButtonDown(0))
				Map(mouseX, mouseY) = current;
			if(Mouse.isButtonDown(1))
				current = Map(mouseX,mouseY)
			
			
			Display.swapBuffers()
		}
		
		Display.destroy
	}
}