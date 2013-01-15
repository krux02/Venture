package venture

import org.lwjgl.opengl.Display
import org.lwjgl.input.{Mouse, Keyboard}
import MapSettings._
import com.badlogic.gdx
import gdx.{InputMultiplexer, Gdx, ApplicationListener}
import etc.EmptyInputProcessor
import gdx.Input.Keys._
import gdx.math.{Vector3, Vector2}
import rendering.{GdxRenderer, Camera}

object Main extends App{
	val preferences = new gdx.backends.lwjgl.LwjglApplicationConfiguration{
		useGL20 = true
		title = "tiling prototype"
		resizable = true
		width = 640
		height = 480
		vSyncEnabled = true
	}
	
	val app     = new Game
	val backend = new gdx.backends.lwjgl.LwjglApplication(app, preferences)
}

class Game extends ApplicationListener {
	
	override def create() {
		Gdx.input.setInputProcessor(inputProcessor)
	}
	
	override def resize (width: Int, height:Int) {
		println("rezize")

    Camera.viewportWidth = width.toFloat
    Camera.viewportHeight = height.toFloat
    Camera.near = -1.0f
    Camera.far  =  1.0f
    Camera.zoom =  1.0f / GdxRenderer.tiledMap.tileWidth.toFloat
    Camera.update()
	}
	
	def width:Float = Gdx.graphics.getWidth
	def height:Float = Gdx.graphics.getHeight
		
	val sb = new StringBuilder
	var current = 0
	
	val inputProcessor = new EmptyInputProcessor {

    override def scrolled(amount:Int) = {
			current += amount
			true
		}
		
		override def keyDown(keyCode: Int) = {
			var b = true
			keyCode match {
				
				case ALT_LEFT | ALT_RIGHT => sb.clear()
				case NUM_1     => sb += '1'
				case NUM_2     => sb += '2'
				case NUM_3     => sb += '3'
				case NUM_4     => sb += '4'
				case NUM_5     => sb += '5'
				case NUM_6     => sb += '6'
				case NUM_7     => sb += '7'
				case NUM_8     => sb += '8'
				case NUM_9     => sb += '9'
				case NUM_0     => sb += '0'
				case _     =>
					println(keyCode)
					b = false  
			}
			b
		}
		
		override def keyUp(keyCode: Int) = {
			keyCode match {
				case ALT_LEFT | ALT_RIGHT => 
					if(!sb.isEmpty) {
						try {
							current = sb.result().toShort
							println(current)
						} catch {
							case x:Exception => println(x.getMessage)
						}
					}
					true
				case _ => 
					false
			}
		}
		
		override def touchDown(x:Int, y:Int, pointer:Int, button:Int):Boolean = {
			val (mouseX,mouseY) = mouseWordPos(x,y)
      println(mouseX,mouseY)

			val tileX = mouseX.floor.toInt
			val tileY = mouseY.floor.toInt

      val layer = GdxRenderer.tiledMap.layers.get(0)

      if (tileX < layer.getWidth && tileY < layer.getHeight && 0 <= tileX && 0 <= tileY)
        println( layer.tiles.apply(tileY).apply(tileX) )

			
			button match {
				case 0 =>
					if( Gdx.input.isKeyPressed(gdx.Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(gdx.Input.Keys.CONTROL_RIGHT) )
						DirectBackground(tileX, tileY) = current.toShort
					else
						Foreground(tileX, tileY) = current.toShort
				case 1 => current = Foreground(tileX,tileY)
				case 2 => 
					currentOutline = (for( Array(x,y) <- Foreground.outlineAt(tileX, tileY).grouped(2) ) yield new Vector2(x,y)).toArray
					print("outline vertices: ")
					println(currentOutline.length)
					println(currentOutline.mkString)
				case _ => return false
			}
			
			true
		}
	}

  val buffer = new Array[Long](20)
  var i = 0

	override def render() {
		
		if(Display.isCloseRequested || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			Gdx.app.exit()

    import gdx.Input.Keys

		val move = if( Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT) ) 1 else 0.25f
		
		if( Gdx.input.isKeyPressed(Keys.I) )
			Camera.position.x -= move
		if( Gdx.input.isKeyPressed(Keys.E) )
			Camera.position.x += move
		if( Gdx.input.isKeyPressed(Keys.A) )
      Camera.position.y -= move
		if( Gdx.input.isKeyPressed(Keys.L) )
			Camera.position.y += move

    if( Mouse.isButtonDown(2) ) {
      val x = Mouse.getDX.toFloat / GdxRenderer.tiledMap.tileWidth
      val y = Mouse.getDY.toFloat / GdxRenderer.tiledMap.tileHeight

      Camera.position.sub(x,y,0)
    }

    Camera.update()

    if( Gdx.input.isKeyPressed(Keys.N) )
      Player.posX -= move
    if( Gdx.input.isKeyPressed(Keys.T) )
      Player.posX += move
    if( Gdx.input.isKeyPressed(Keys.R) )
      Player.posY -= move
    if( Gdx.input.isKeyPressed(Keys.G) )
      Player.posY += move

    Player.update()
		
		//Physics.update



		rendering.LwjglRenderer.render(Camera)
//    rendering.GdxRenderer.render(Camera)



		
		Display.swapBuffers()
	}
	
	override def pause() {
		println("pause")
	}
	
	override def resume() {
		println("resume")
	}
	
	override def dispose() {
		println("dispose")
		Display.destroy()
	}
	
	def mouseWordPos(mouseX:Int, mouseY:Int) = {
    val tmp = new Vector3(mouseX,mouseY,0.5f)
    Camera.unproject(tmp, 0,0, Camera.viewportWidth, Camera.viewportHeight)
		val x = Camera.position.x + (mouseX -  width*0.5f) / (tileSize * Foreground.tileScale)
		val y = Camera.position.y + (height*0.5f - mouseY) / (tileSize * Foreground.tileScale)
		(x,y)
	}
	
	var currentOutline = Array[Vector2]()
}
