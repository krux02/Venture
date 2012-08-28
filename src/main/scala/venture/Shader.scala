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

object Shader {
	def createShader(eShaderType:Int, strShaderFile:String) = {
	    val shader = glCreateShader(eShaderType);
	    glShaderSource(shader, strShaderFile);
	    glCompileShader(shader);
	    
	    val status = glGetShader(shader, GL_COMPILE_STATUS);
	    if (status == GL_FALSE)
	    {
	        val infoLogLength = glGetShader(shader, GL_INFO_LOG_LENGTH)
	        val strInfoLog = glGetShaderInfoLog(shader, infoLogLength)
	        val strShaderType = eShaderType match {
		        case GL_VERTEX_SHADER   => "vertex"
		        case GL_GEOMETRY_SHADER => "geometry"
		        case GL_FRAGMENT_SHADER => "fragment"
	        }
	        
	        printf("Compile failure in %s shader:\n%s\n", strShaderType, strInfoLog);
	    }
	
		shader
	}
	
	def createProgram(shaderList: Seq[Int]) = {
	    val program = glCreateProgram();
	    
	    for(shader <- shaderList)
	    	glAttachShader(program, shader);
	    
	    glLinkProgram(program);
	    
	    
	    val status = glGetProgram(program, GL_LINK_STATUS);
	    if (status == GL_FALSE)
	    {
	        val infoLogLength = glGetProgram(program, GL_INFO_LOG_LENGTH);
	        val strInfoLog = glGetProgramInfoLog(program, infoLogLength);
	        printf("Linker failure: %s\n", strInfoLog);
	    }
	    
	    for(shader <- shaderList)
	        glDetachShader(program, shader);
	
	    program
	}
}
