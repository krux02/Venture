package venture

import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.math.Vector2

object Physics {
	val environment = new AABB(-100.0f, -100.0f, 100.0f,  100.0f)
	
	val gravity = new Vector2(0.0f,-10.0f)
	val doSleep = true
	
	val world = new World(gravity, doSleep)
	
	val bodyDef = new BodyDef
	bodyDef.`type` = BodyType.DynamicBody
	bodyDef.position.set(0, 4)
	val body = world.createBody(bodyDef)
	val dynamicBox = new PolygonShape
	dynamicBox.setAsBox(1,1)
	val fixtureDef = new FixtureDef
	fixtureDef.shape = dynamicBox
	fixtureDef.density = 1.0f
	fixtureDef.friction = 0.3f
	body.createFixture(fixtureDef)
	
	
	val timeStep = 1.0f/60.0f
	val velocityIterations = 6
	val positionIterations = 2
	
	def addGroundPolygon(vertices:Array[Vector2], pos:Vector2){
		val groundBodyDef = new BodyDef
		groundBodyDef.position.set(pos)
		val groundBody = world.createBody(groundBodyDef)
		val groundBox = new PolygonShape()
    groundBox.set(vertices)
		groundBody.createFixture(groundBox, 0)
	}
	
	def update() {
		world.step(timeStep, velocityIterations, positionIterations)
	}
}
