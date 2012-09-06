package venture

import org.jbox2d.dynamics._
import org.jbox2d.collision._
import org.jbox2d.collision.shapes._
import org.jbox2d.common.MathUtils._
import org.jbox2d.common._
import org.jbox2d.callbacks.DebugDraw
import org.lwjgl.BufferUtils

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL21._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL31._
import org.lwjgl.opengl.GL32._
import org.lwjgl.opengl.GL33._

object DebugDrawShader {
	val vertexShaderSrc = """
#version 330
in vec2 in_position;
in vec4 in_color;

out vec4 v_color;
		
uniform vec2 u_offset;
// scaling to fit for the resolution
uniform vec2 u_scale;

void main()
{
	v_color = in_color;
    gl_Position = vec4((in_position+u_offset)*u_scale,0,1);
}
"""
	
	val fragmentShaderSrc = """
#version 330
in vec4 v_color;

out vec4 outputColor;
		
void main()
{
    outputColor = v_color;
}
"""
	import Shader.{createProgram, createShader}
	
	val shaderList = List(createShader(GL_VERTEX_SHADER, vertexShaderSrc), createShader(GL_FRAGMENT_SHADER, fragmentShaderSrc))
    val theProgram = createProgram(shaderList);
	shaderList foreach glDeleteShader
	
	val offsetLoc       = glGetUniformLocation(theProgram, "u_offset");
	val scaleLoc        = glGetUniformLocation(theProgram, "u_scale");
	val colorLoc        = glGetAttribLocation(theProgram, "in_color");
	val positionLoc     = glGetAttribLocation(theProgram, "in_position");
	
}


class GlDebugDraw(vptf:IViewportTransform) extends DebugDraw(vptf) {
	// Data x:Float y:Float r:Byte g:Byte b:Byte a:Byte
	var pointCounter = 0
	var pointBuffer = BufferUtils.createByteBuffer(12)
	val glPointBuffer = glGenBuffers()
	
	// Data x1:Float y1:Float r1:Byte g1:Byte b1:Byte a1:Byte x2:Float y2:Float r2:Byte g2:Byte b2:Byte a2:Byte
	var lineCounter = 0
	var lineBuffer = BufferUtils.createByteBuffer(12*2)
	val glLineBuffer = glGenBuffers()
	
	val circleBuffer = new Array[Float](18*4)
	for(i <- 0 until 18) {
		circleBuffer(4*i+0) = math.cos((20.0*i   ).toRadians).toFloat
		circleBuffer(4*i+1) = math.sin((20.0*i   ).toRadians).toFloat
		circleBuffer(4*i+2) = math.cos((20.0*i+20).toRadians).toFloat
		circleBuffer(4*i+3) = math.sin((20.0*i+20).toRadians).toFloat
	}
	
	def drawPoint( argPoint:Vec2, argRadiusOnScreen:Float, argColor:Color3f){
		pointBuffer = util.bufferExtend(pointBuffer)
		
		pointBuffer putFloat argPoint.x
		pointBuffer putFloat argPoint.y
		
		pointBuffer put (argColor.x * 256).toByte
		pointBuffer put (argColor.y * 256).toByte
		pointBuffer put (argColor.z * 256).toByte
		pointBuffer put  255.toByte
		
		pointCounter += 1
	}
	
	
	def drawSolidPolygon(vertices:Array[Vec2], vertexCount:Int, color:Color3f){
		drawPolygon(vertices,vertexCount,color)
	}
	
	def drawCircle(center:Vec2, radius:Float, color:Color3f) {
		
		val offsetX = center.x
		val offsetY = center.y
		
		val r = (color.x * 255).toByte
		val g = (color.y * 255).toByte
		val b = (color.z * 255).toByte
		val a = 255.toByte
		
		lineBuffer = util.bufferExtend(lineBuffer,circleBuffer.length*12)
		
		for( i <- 0 until circleBuffer.length/2 ) {
			lineBuffer putFloat circleBuffer(2*i+0)*radius + offsetX
			lineBuffer putFloat circleBuffer(2*i+1)*radius + offsetY
			
			lineBuffer put r
			lineBuffer put g
			lineBuffer put b
			lineBuffer put a
		}
		
		lineCounter += circleBuffer.length/2
	}
	
	def drawSolidCircle(center:Vec2, radius:Float, axis:Vec2, color:Color3f){
		drawCircle(center,radius,color)
	}
	
	def drawSegment(p1:Vec2, p2:Vec2, color:Color3f) {
		lineBuffer = util.bufferExtend(lineBuffer)
		
		lineBuffer putFloat p1.x
		lineBuffer putFloat p1.y
		
		lineBuffer put (color.x * 255).toByte
		lineBuffer put (color.y * 255).toByte
		lineBuffer put (color.z * 255).toByte
		lineBuffer put  255.toByte
		
		lineBuffer putFloat p2.x
		lineBuffer putFloat p2.y
		
		lineBuffer put (color.x * 256).toByte
		lineBuffer put (color.y * 256).toByte
		lineBuffer put (color.z * 256).toByte
		lineBuffer put  255.toByte
		
		lineCounter += 2
	}
	
	def drawTransform(xf:Transform){
		println(xf)
	}
	
	def drawString(x:Float, y:Float, s:String, color:Color3f){
		println(s)
	}
	
	def draw {
		pointBuffer.clear
		lineBuffer.clear
		
		glBindBuffer(GL_ARRAY_BUFFER,glPointBuffer)
		glBufferData(GL_ARRAY_BUFFER, pointBuffer, GL_STATIC_DRAW)
		
		glUseProgram(DebugDrawShader.theProgram)
		glEnableVertexAttribArray(DebugDrawShader.positionLoc)
		glVertexAttribPointer(DebugDrawShader.positionLoc, 2, GL_FLOAT, true, 12, 0);
		glEnableVertexAttribArray(DebugDrawShader.colorLoc)
		glVertexAttribPointer(DebugDrawShader.colorLoc, 4, GL_UNSIGNED_BYTE, false, 12, 8);
		
		
		glUniform2f( DebugDrawShader.offsetLoc, -LwjglApp.posX.toFloat, -LwjglApp.posY.toFloat)
		val f = MapSettings.tileSize * Foreground.tileScale * 2.0f
		glUniform2f(DebugDrawShader.scaleLoc, f / LwjglApp.width , f / LwjglApp.height)
		
		glDrawArrays(GL_POINTS,0,pointCounter)
		glBufferData(GL_ARRAY_BUFFER, lineBuffer, GL_STATIC_DRAW)
		glDrawArrays(GL_LINES, 0, lineCounter)
		
		glBindBuffer(GL_ARRAY_BUFFER, 0)
		glUseProgram(0)
		pointCounter = 0
		lineCounter = 0
	}
	
	
}


object Physics {
	val environment = new AABB();
	environment.lowerBound.set(-100.0f, -100.0f);
	environment.upperBound.set( 100.0f,  100.0f);
	
	val gravity = new Vec2(0.0f,-10.0f)
	val doSleep = true;
	
	val world = new World(gravity, doSleep)
	
	val bodyDef = new BodyDef
	bodyDef.`type` = BodyType.DYNAMIC
	bodyDef.position.set(0, 4);
	val body = world.createBody(bodyDef)
	val dynamicBox = new PolygonShape
	dynamicBox.setAsBox(1,1)
	val fixtureDef = new FixtureDef
	fixtureDef.shape = dynamicBox
	fixtureDef.density = 1
	fixtureDef.friction = 0.3f
	body.createFixture(fixtureDef)
	
	
	val timeStep = 1.0f/60.0f
	val velocityIterations = 6
	val positionIterations = 2
	
	def addGroundPolygon(vertices:Array[Vec2], pos:Vec2){
		val groundBodyDef = new BodyDef
		groundBodyDef.position.set(pos)
		val groundBody = world.createBody(groundBodyDef);
		val groundBox = new PolygonShape();
		groundBox.set(vertices, vertices.length)
		groundBody.createFixture(groundBox, 0)
	}
	
	def update {
		world.step(timeStep, velocityIterations, positionIterations)
	}
	
	def debugDraw {
		world.drawDebugData()
	}
	
	val debugDrawer = new GlDebugDraw(null)
	debugDrawer.setFlags(DebugDraw.e_shapeBit /*| DebugDraw.e_aabbBit*/)
	world.setDebugDraw(debugDrawer)
	
	def draw {
		val position = body.getPosition();
	    val angle = body.getAngle();
	    world.drawDebugData()
	    debugDrawer.draw
	}
}
