package rendering

import org.lwjgl.opengl.GL11.{glGetInteger => _, _}
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._
import org.lwjgl.BufferUtils
import venture.{Main, Foreground, MapSettings}

class Animation(texture:rendering.Texture, animationSteps:Int) {
	var bufferData = BufferUtils.createByteBuffer(2*4+1*2)
	val buffer = glGenBuffers()
	var counter = 0
	
	def draw(posX:Double, posY:Double, frame:Short) {
		bufferData = noise.bufferExtend(bufferData)
		
		bufferData putFloat posX.toFloat
		bufferData putFloat posY.toFloat
		bufferData putShort frame
		
		counter += 1
	}
	
	def draw() {
		bufferData.rewind()
		glUseProgram(AnimationShader.theProgram)
		
		glBindBuffer(GL_ARRAY_BUFFER, buffer)
		glBufferData(GL_ARRAY_BUFFER, bufferData, GL_STATIC_DRAW)

		glEnableVertexAttribArray(AnimationShader.tileIdLoc)
		glVertexAttribIPointer(AnimationShader.tileIdLoc, 1, GL_SHORT, 2*4+1*2, 2*4)
		
		glEnableVertexAttribArray(AnimationShader.positionLoc)
		glVertexAttribPointer(AnimationShader.positionLoc, 2, GL_FLOAT, true, 2*4+1*2, 0)
		
		glBindBuffer(GL_ARRAY_BUFFER, 0)
		
		
		glUniform1i(AnimationShader.arrayTextureLoc, 1)
		glUniform2f(AnimationShader.offsetLoc, 0, 0)
		val f = MapSettings.tileSize * Foreground.tileScale * 2.0f
		glUniform2f(AnimationShader.scaleLoc, f / Main.app.width , f / Main.app.height)
		glUniform1f(AnimationShader.fadeLoc, 1.0f )
		
		glDrawArrays(GL_POINTS, 0, counter)
		
		glDisableVertexAttribArray(0)
		
		counter = 0
		glUseProgram(0)
	}
}


