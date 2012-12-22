package rendering

import org.lwjgl.opengl.GL20._

/**
 * Created with IntelliJ IDEA.
 * User: arne
 * Date: 22.12.12
 * Time: 18:29
 */

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
  import rendering.Shader.{createProgram, createShader}

  val shaderList = List(createShader(GL_VERTEX_SHADER, vertexShaderSrc), createShader(GL_FRAGMENT_SHADER, fragmentShaderSrc))
  val theProgram = createProgram(shaderList)
  shaderList foreach glDeleteShader

  val arrayTextureLoc = glGetUniformLocation(theProgram, "u_arrayTexture")
  val offsetLoc       = glGetUniformLocation(theProgram, "u_offset")
  val scaleLoc        = glGetUniformLocation(theProgram, "u_scale")
  val fadeLoc         = glGetUniformLocation(theProgram, "u_fade")
  val tileIdLoc       = glGetAttribLocation(theProgram, "in_tileID")
  val positionLoc     = glGetAttribLocation(theProgram, "in_position")

}