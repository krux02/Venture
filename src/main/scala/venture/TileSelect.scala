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

object TileSelectShader{
	val vertexShaderSrc = """
#version 330
in vec2 in_position;
in int in_tileID;
		
flat out float v_tileID;

void main()
{
	v_tileID = in_tileID;
    gl_Position = vec4(in_position,0,1);
}
"""
	
	val fragmentShaderSrc = """
#version 330
flat in float v_tileID;
		
uniform sampler2DArray u_arrayTexture;
		
out vec4 outputColor;
		
void main()
{
    outputColor = texture( u_arrayTexture, vec3(gl_PointCoord, v_tileID ) );
}
"""
	import Shader.{createProgram, createShader}
	
	val shaderList = List(createShader(GL_VERTEX_SHADER, vertexShaderSrc), createShader(GL_FRAGMENT_SHADER, fragmentShaderSrc))
    val theProgram = createProgram(shaderList);
	shaderList foreach glDeleteShader
	
	val arrayTextureLoc = glGetUniformLocation(theProgram, "u_arrayTexture");
	val tileIdLoc       = glGetAttribLocation(theProgram, "in_tileID");
	val positionLoc     = glGetAttribLocation(theProgram, "in_position");
	
}

/*
object TileSelect {
	
	def draw {
		glUseProgram(TileSelectShader.theProgram);
		glBindBuffer(GL_ARRAY_BUFFER, Chunk.positionBuffer);
		glEnableVertexAttribArray(ChunkShader.positionLoc);
		glVertexAttribPointer(ChunkShader.positionLoc, 4, GL_FLOAT, true, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		if(changed)
			glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
		glEnableVertexAttribArray(ChunkShader.tileIdLoc)
		glVertexAttribIPointer(ChunkShader.tileIdLoc, 1, GL_SHORT, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		glUniform1i(TileSelectShader.arrayTextureLoc, 0);
		
		glDrawArrays(GL_POINTS, 0, 16*16 );
		
		glDisableVertexAttribArray(0);
		glUseProgram(0);
		
	}
}
*/