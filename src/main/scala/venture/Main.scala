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
import org.lwjgl.input.Keyboard
import org.lwjgl.util.glu.GLU
import MapSettings._
import com.badlogic.gdx
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ApplicationListener
import etc.EmptyInputProcessor
import gdx.Input.Keys._
import gdx.math.Vector2
import rendering.Camera

object Tools {
	def error() {
		val err = glGetError()
		if( err != GL_NO_ERROR )
			println(GLU.gluErrorString(err))
	}
}

object Main extends App{
	val preferences = new gdx.backends.lwjgl.LwjglApplicationConfiguration{
		useGL20 = true
		title = "tiling prototype"
		resizable = true
		width = 640
		height = 480
		vSyncEnabled = true
	}
	
	val app     = new LwjglApp
	val backend = new gdx.backends.lwjgl.LwjglApplication(app, preferences)
}

class LwjglApp extends ApplicationListener {
	
	override def create() {
		glEnable(GL_POINT_SPRITE)
		glEnable(GL_BLEND)
		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA)
		
		glActiveTexture(GL_TEXTURE0)
		Texture.tiles.bind
		glActiveTexture(GL_TEXTURE1)
		Texture.playerTexture.bind
		
		Gdx.input.setInputProcessor(inputProcessor)
	}
	
	override def resize (width: Int, height:Int) {
		println("rezize")
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
					currentOutline = Foreground.outlineAt(tileX, tileY).grouped(2).map{case Array(x,y) => new Vector2(x,y)}.toArray
					print("outline vertices: ")
					println(currentOutline.length)
					println(currentOutline.mkString)
					//Physics.addGroundPolygon(currentOutline,new org.jbox2d.common.Vec2(0,0))
				case _ => return false
			}
			
			true
		}
	}
	
	override def render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
		
		if(Display.isCloseRequested || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			Gdx.app.exit()
		
		val offsetX = (width*0.5f / tileSize)
		val offsetY = (height*0.5f / tileSize)


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

    if( Gdx.input.isKeyPressed(Keys.N) )
      Player.posX -= move
    if( Gdx.input.isKeyPressed(Keys.T) )
      Player.posX += move
    if( Gdx.input.isKeyPressed(Keys.R) )
      Player.posY -= move
    if( Gdx.input.isKeyPressed(Keys.G) )
      Player.posY += move

		/*
		if( Mouse.isButtonDown(2) ) {
			val x = (Mouse.getX -  width*0.5) / (tileSize*Foreground.tileScale)
			val y = (Mouse.getY - height*0.5) / (tileSize*Foreground.tileScale)
			posX += x * 0.1
			posY += y * 0.1
		}
		*/
		
		//Physics.update
		
		Background.drawRect(Camera.position.x,Camera.position.y,offsetX,offsetY)
		DirectBackground.drawRect(Camera.position.x,Camera.position.y,offsetX,offsetY)
		Foreground.drawRect(Camera.position.x,Camera.position.y,offsetX,offsetY)
		Player.draw(Camera.position.x,Camera.position.y)

		/*
		Physics.debugDrawer.drawPolygon(currentOutline,currentOutline.length, white)
		Physics.debugDrawer.draw
		
		Physics.draw
		*/
		
		Player.update
		Player.animation.draw
		
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
