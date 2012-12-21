package venture

import org.lwjgl.opengl.Display
import org.lwjgl.input.Keyboard
import MapSettings._
import com.badlogic.gdx
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ApplicationListener
import etc.EmptyInputProcessor
import gdx.Input.Keys._
import gdx.math.Vector2
import rendering.Camera

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

    val f = 1.0f / MapSettings.tileSize.toFloat
    Camera.setToOrtho(false,width*f,height*f)
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
			val tileX = mouseX.floor.toInt
			val tileY = mouseY.floor.toInt
			
			button match {
				case 0 =>
					if( Gdx.input.isKeyPressed(gdx.Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(gdx.Input.Keys.CONTROL_RIGHT) )
						DirectBackground(tileX, tileY) = current.toShort
					else
						Foreground(tileX, tileY) = current.toShort
				case 1 => current = Foreground(tileX,tileY)
				case 2 => 
					currentOutline = Foreground.outlineAt(tileX, tileY).grouped(2).map{case Array(x, y) => new Vector2(x,y)}.toArray
					print("outline vertices: ")
					println(currentOutline.length)
					println(currentOutline.mkString)
				case _ => return false
			}
			
			true
		}
	}
	
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

    Camera.update()

    if( Gdx.input.isKeyPressed(Keys.N) )
      Player.posX -= move
    if( Gdx.input.isKeyPressed(Keys.T) )
      Player.posX += move
    if( Gdx.input.isKeyPressed(Keys.R) )
      Player.posY -= move
    if( Gdx.input.isKeyPressed(Keys.G) )
      Player.posY += move

    Player.update

		/*
		if( Mouse.isButtonDown(2) ) {
			val x = (Mouse.getX -  width*0.5) / (tileSize*Foreground.tileScale)
			val y = (Mouse.getY - height*0.5) / (tileSize*Foreground.tileScale)
			posX += x * 0.1
			posY += y * 0.1
		}
		*/
		
		//Physics.update
		
		rendering.LwjglRenderer.render()
		
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
		val x = Camera.position.x + (mouseX -  width*0.5f) / (tileSize * Foreground.tileScale)
		val y = Camera.position.y + (height*0.5f - mouseY) / (tileSize * Foreground.tileScale)
		(x,y)
	}
	
	var currentOutline = Array[Vector2]()
}
