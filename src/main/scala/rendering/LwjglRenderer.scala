package rendering

import venture._
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import MapSettings._
import com.badlogic.gdx.Gdx
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL13._

object LwjglRenderer {

  def width:Float = Gdx.graphics.getWidth
  def height:Float = Gdx.graphics.getHeight

  def renderMapChunk(chunk:Chunk, posX:Double,posY:Double,tileScale:Int,fade:Double) {
    glUseProgram(rendering.ChunkShader.theProgram)
    glBindBuffer(GL_ARRAY_BUFFER, Chunk.positionBuffer)
    glEnableVertexAttribArray(rendering.ChunkShader.positionLoc)
    glVertexAttribPointer(rendering.ChunkShader.positionLoc, 2, GL_FLOAT, true, 0, 0)
    glBindBuffer(GL_ARRAY_BUFFER, chunk.buffer)
    if(chunk.changed)
      glBufferData(GL_ARRAY_BUFFER, chunk.data, GL_STATIC_DRAW)
    glEnableVertexAttribArray(rendering.ChunkShader.tileIdLoc)
    glVertexAttribIPointer(rendering.ChunkShader.tileIdLoc, 1, GL_SHORT, 0, 0)
    glBindBuffer(GL_ARRAY_BUFFER, 0)

    glUniform1i(rendering.ChunkShader.arrayTextureLoc, 0)
    glUniform2f(rendering.ChunkShader.offsetLoc, posX.toFloat, posY.toFloat)
    glUniform2f(rendering.ChunkShader.scaleLoc, tileSize * tileScale * 2.0f / Main.app.width , tileSize * tileScale * 2.0f / Main.app.height)
    glUniform1f(rendering.ChunkShader.fadeLoc, fade.toFloat)

    glDrawArrays(GL_POINTS, 0, chunkSize*chunkSize)

    glDisableVertexAttribArray(0)
    glUseProgram(0)
  }

  def drawMapRect(map:Map, halfWidth:Double, halfHeight:Double) {
    val centerX = Camera.position.x
    val centerY = Camera.position.y
    val left = (centerX-halfWidth).floor.toInt
    val right= (centerX+halfWidth).ceil.toInt
    val bottom=(centerY-halfHeight).floor.toInt
    val top  = (centerY+halfHeight).ceil.toInt

    glPointSize(tileSize*map.tileScale)

    for(x <- map.div(left,chunkSize) to map.div(right,chunkSize); y <- map.div(bottom,chunkSize) to map.div(top,chunkSize)) {
      renderMapChunk(map.get(x,y), x*chunkSize-centerX, y*chunkSize-centerY, map.tileScale, map.fade)
    }
  }

  def renderPlayer() {
    Player.animation.draw(Player.posX-Camera.position.x, Player.posY-Camera.position.y,0)
  }

  val box2dDebugRenderer = new com.badlogic.gdx.physics.box2d.Box2DDebugRenderer()

  def renderPhysicsDebugInfo() {
    box2dDebugRenderer setDrawBodies true
    box2dDebugRenderer setDrawJoints true
    box2dDebugRenderer setDrawAABBs true
    box2dDebugRenderer setDrawInactiveBodies true
    box2dDebugRenderer setDrawVelocities true
    box2dDebugRenderer.render(Physics.world, Camera.view)
  }

  def render() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    glEnable(GL_POINT_SPRITE)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA)

    glActiveTexture(GL_TEXTURE0)
    rendering.Texture.tiles.bind()
    glActiveTexture(GL_TEXTURE1)
    rendering.Texture.playerTexture.bind()

    val offsetX = (width*0.5f / tileSize)
    val offsetY = (height*0.5f / tileSize)

    drawMapRect(Background, offsetX,offsetY)
    drawMapRect(DirectBackground, offsetX,offsetY)
    drawMapRect(Foreground, offsetX,offsetY)

    renderPhysicsDebugInfo()

    renderPlayer()

    // render current Outline
    /*
    val renderer = new ShapeRenderer()
    renderer.setColor(1,1,1,1)
    if(! currentOutline.isEmpty) {
      var last = currentOutline.last
      renderer.begin(ShapeRenderer.ShapeType.Line)
      for ( v:Vector2 <- currentOutline ){
        renderer.line(v.x,v.y,last.x,last.y)
        last = v
      }
      renderer.end()
    }
    */
  }
}
