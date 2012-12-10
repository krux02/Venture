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
import org.lwjgl.BufferUtils

object AnimationShader {
	val vertexShaderSrc = """
#version 330
in vec2 in_position;
in int in_tileID;
		
uniform vec2 u_offset;
// scaling to fit for the resolution
uniform vec2 u_scale;
		
flat out float v_tileID;

void main()
{
	v_tileID = in_tileID;
    gl_Position = vec4((in_position+u_offset)*u_scale,0,1);
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

class Animation(texture:Texture, animationsteps:Int) {
	var bufferData = BufferUtils.createByteBuffer(2*4+1*2);
	val buffer = glGenBuffers()
	var counter = 0;
	
	def draw(posX:Double, posY:Double, frame:Short) {
		bufferData = util.bufferExtend(bufferData)
		
		bufferData putFloat posX.toFloat
		bufferData putFloat posY.toFloat
		bufferData putShort frame
		
		counter += 1
	}
	
	def draw {
		bufferData.rewind()
		glUseProgram(AnimationShader.theProgram);
		
		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		glBufferData(GL_ARRAY_BUFFER, bufferData, GL_STATIC_DRAW)

		glEnableVertexAttribArray(AnimationShader.tileIdLoc)
		glVertexAttribIPointer(AnimationShader.tileIdLoc, 1, GL_SHORT, 2*4+1*2, 2*4);
		
		glEnableVertexAttribArray(AnimationShader.positionLoc);
		glVertexAttribPointer(AnimationShader.positionLoc, 2, GL_FLOAT, true, 2*4+1*2, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		
		glUniform1i(AnimationShader.arrayTextureLoc, 1);
		glUniform2f(AnimationShader.offsetLoc, 0, 0)
		val f = MapSettings.tileSize * Foreground.tileScale * 2.0f
		glUniform2f(AnimationShader.scaleLoc, f / Main.app.width , f / Main.app.height)
		glUniform1f(AnimationShader.fadeLoc, 1.0f )
		
		glDrawArrays(GL_POINTS, 0, counter)
		
		glDisableVertexAttribArray(0);
		
		counter = 0;
		glUseProgram(0);
	}
}

object Player {
	val animation = new Animation(Texture.playerTexture, 2)
	
	var posX,posY = 0.0
	var dX,dY = 0.0
	
	var onGround = false;
	var groundSpeed = 0.0
	
	def update{
		posX = posX + dX
		posY = posY + dY
		
		dY -= 0.02;
		
		dX *= 0.95;
		dY *= 0.95
	}
	
	def draw(offsetX:Double, offsetY:Double) {
		animation.draw(posX-offsetX,posY-offsetY,0)
	}
}
