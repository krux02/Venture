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
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.input.Keyboard
import simplex3d.math.double._
import java.nio.ByteBuffer
import org.lwjgl.util.glu.GLU
import Tools._
import org.lwjgl.BufferUtils
import scala.util.Random
import org.lwjgl.input.Mouse
import MapSettings._
import simplex3d.math.Vec2i

object Tools {
	def buffer(vertices: Vec4*) = {
		val b = ByteBuffer.allocateDirect(vertices.size*4*4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer()
		for( v <- vertices ) {
			b put v.x.toFloat
			b put v.y.toFloat
			b put v.z.toFloat
			b put v.w.toFloat
		}
		b.flip()
		b
	}
	
	def error{
		val err = glGetError()
		if( err != GL_NO_ERROR )
			println(GLU.gluErrorString(err))
	}
}

object LwjglApp { 
	var running = true;
	
	val width  = 1024+512
	val height = 1024
	
	var posX,posY = 0.0
	
	def mouseWordPos = {
		val x = posX + (Mouse.getX -  width*0.5) / (tileSize * Foreground.tileScale)
		val y = posY + (Mouse.getY - height*0.5) / (tileSize * Foreground.tileScale)
		Vec2(x,y)
	}
	
	var currentOutline = Array[org.jbox2d.common.Vec2]()
	
	def main(args: Array[String]){
		Display.setDisplayMode(new DisplayMode(width,height))
		Display.setVSyncEnabled(true)
		Display.create
		
		glEnable(GL_POINT_SPRITE)
		
		glEnable(GL_BLEND)
		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA)
		
		glActiveTexture(GL_TEXTURE0)
		Texture.tiles.bind
		glActiveTexture(GL_TEXTURE1)
		Texture.playerTexture.bind
		
		var current:Short = 0;
		val sb = new StringBuilder
		
		while(running) {
			Display.update
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			if(Display.isCloseRequested || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
				running = false
				
			val mousePos = mouseWordPos
			val mouseX = mousePos.x.floor.toInt
			val mouseY = mousePos.y.floor.toInt
			
			val offsetX = (width*0.5 / tileSize)
			val offsetY = (height*0.5 / tileSize)

			import Keyboard._
			val move = if( isKeyDown(KEY_LCONTROL) || isKeyDown(KEY_RCONTROL) ) 1 else 0.25
			
			if( isKeyDown(KEY_I) )
				posX -= move
			if( isKeyDown(KEY_E) )
				posX += move
			if( isKeyDown(KEY_A) )
				posY -= move
			if( isKeyDown(KEY_L) )
				posY += move
			
			if( isKeyDown(KEY_N) )
				Player.posX -= move
			if( isKeyDown(KEY_T) )
				Player.posX += move
			if( isKeyDown(KEY_R) )
				Player.posY -= move
			if( isKeyDown(KEY_G) )
				Player.posY += move
				
			// event Keyboard
			while( Keyboard.next ) {
				// eventKey 
				val key = getEventKey
				val state = getEventKeyState
				
				if( state ) {
					key match {
						case KEY_LMENU | KEY_RMENU => sb.clear()
						case KEY_1 | KEY_NUMPAD1 => sb += '1'
						case KEY_2 | KEY_NUMPAD2 => sb += '2'
						case KEY_3 | KEY_NUMPAD3 => sb += '3'
						case KEY_4 | KEY_NUMPAD4 => sb += '4'
						case KEY_5 | KEY_NUMPAD5 => sb += '5'
						case KEY_6 | KEY_NUMPAD6 => sb += '6'
						case KEY_7 | KEY_NUMPAD7 => sb += '7'
						case KEY_8 | KEY_NUMPAD8 => sb += '8'
						case KEY_9 | KEY_NUMPAD9 => sb += '9'
						case KEY_0 | KEY_NUMPAD0 => sb += '0'
						case _     => 
					}
				}
				else {
					key match {
						case KEY_LMENU | KEY_RMENU => 
							if(! sb.isEmpty)
							try {
								current = sb.result.toShort
							} catch {
								case x => println(x.getMessage)
							}
						case _ =>
					}
				}
			}
			
			while( Mouse.next ) {
				import Mouse._
				if(getEventButtonState) {
					getEventButton match {
						case 0 => 
							if( isKeyDown(KEY_LCONTROL) || isKeyDown(KEY_RCONTROL) )
								DirectBackground(mouseX,mouseY) = current
							else
								Foreground(mouseX, mouseY) = current;
						case 1 => current = Foreground(mouseX,mouseY)
						case 2 => 
							currentOutline = Foreground.outlineAt(mouseX,mouseY).grouped(2).map{case Array(x,y) => new org.jbox2d.common.Vec2(x,y)}.toArray
							print("outline vertices: ")
							println(currentOutline.length)
							println(currentOutline.mkString)
							//Physics.addGroundPolygon(currentOutline,new org.jbox2d.common.Vec2(0,0))
						case _ => 
					}
				}	
			}
			current = (current + Mouse.getDWheel / 120).toShort
			
			/*
			if( Mouse.isButtonDown(2) ){
				val x = (Mouse.getX -  width*0.5) / (tileSize*Foreground.tileScale)
				val y = (Mouse.getY - height*0.5) / (tileSize*Foreground.tileScale)
				posX += x * 0.1
				posY += y * 0.1
			}
			*/
			
			Physics.update
			
			Background.drawRect(posX,posY,offsetX,offsetY)
			DirectBackground.drawRect(posX,posY,offsetX,offsetY)
			Foreground.drawRect(posX,posY,offsetX,offsetY)
			Player.draw(posX,posY)
			
			val white = new org.jbox2d.common.Color3f(1,1,1)
			Physics.debugDrawer.drawPolygon(currentOutline,currentOutline.length, white)
			Physics.debugDrawer.draw
			
			Physics.draw
			
			Player.animation.draw
			
			Display.swapBuffers()
		}
		
		Display.destroy
	}
}