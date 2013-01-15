package rendering

import venture.Map
import venture.MapSettings._
import com.badlogic.gdx
import gdx.files.FileHandle
import gdx.Gdx
import gdx.graphics.g2d.tiled.{TileMapRenderer, SimpleTileAtlas, TiledLoader, TiledMap}
import gdx.graphics.{OrthographicCamera, GL10}

object GdxRenderer extends Renderer {
  val tileMapTexture = new FileHandle("textures/simple.png")
  val tiledMapFile = new FileHandle("maps/map.tmx")
  val tiledMap = TiledLoader.createMap(tiledMapFile)
  val atlas = new SimpleTileAtlas(tiledMap,new FileHandle("textures/"))
  val tileMapRenderer = new TileMapRenderer(tiledMap,atlas,16,16,1.0f,1.0f)
  assert(tileMapTexture.exists)


  def drawMapRect(map:Map, halfWidth:Double, halfHeight:Double) {
    val centerX = Camera.position.x
    val centerY = Camera.position.y
    val left = (centerX-halfWidth).floor.toInt
    val right= (centerX+halfWidth).ceil.toInt
    val bottom=(centerY-halfHeight).floor.toInt
    val top  = (centerY+halfHeight).ceil.toInt

    for(x <- map.div(left,chunkSize) to map.div(right,chunkSize); y <- map.div(bottom,chunkSize) to map.div(top,chunkSize)) {
//      renderMapChunk(map.get(x,y), x*chunkSize-centerX, y*chunkSize-centerY, map.tileScale, map.fade)
    }
  }

  override def render(camera:OrthographicCamera) {
    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
    tileMapRenderer.render(camera)

  }




}
