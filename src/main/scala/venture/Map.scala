package venture

import org.lwjgl.BufferUtils
import com.badlogic.gdx.math.MathUtils
import scala.Predef._
import scala.Some

object MapSettings{
	val chunkSize = 16
	val tileSize = 16
}

import MapSettings._

object Foreground extends       Map( 2, 1.0,  new Generator( (x:Int,y:Int) => noise.noise3(x.toDouble/chunkSize, y.toDouble/chunkSize, 50.5) ))
object DirectBackground extends Map( 2, 0.65, new Generator( (x:Int,y:Int) => noise.noise3(x.toDouble/chunkSize, y.toDouble/chunkSize, 50.5) ))
object Background extends       Map( 1, 0.5,  new Generator( (x:Int,y:Int) => noise.noise3(x.toDouble/chunkSize, y.toDouble/chunkSize, 100.5) ))


object Intersection {

  trait RayCastCallback{
    def apply(x:Float,y:Float,nX:Float,nY:Float)
  }

  def apply(r1:Ray, r2:Ray) {

    /*
    val lenVec1 = r1.lenVec
    val lenVec2 = r2.lenVec

    val lenX1 = r1.lenX
    val lenY1 = r1.lenY
    val lenX2 = r2.lenX
    val lenY2 = r2.lenY

    r1.v1 + x * r1.lenVec = r2.v1 + y * r2.lenVec
    r1.x1 + x * r1.lenX = r2.x1 + y * r2.lenX
    r1.y1 + x * r1.lenY = r2.y1 + y * r2.lenY

    val y = (r1.x1 + (r2.y1 - r1.y1 + y * lenY2) / r1.lenY * lenX1) / lenX2 - r2.x1
    val x = (r2.y1 - r1.y1 + y * lenY2) / lenY1
    */
  }

  def apply(r:Ray,q:Quad, callback:RayCastCallback) {
    if( !(r.aabb intersects q.aabb) ) {
      return
    }
  }
}


final case class Vec2(x:Float, y:Float){
	def +(that:Vec2)   = new Vec2(x+that.x,y+that.y)
	def -(that:Vec2)   = new Vec2(x-that.x,y-that.y)
	def *(that:Vec2)   = new Vec2(x*that.x,y*that.y)
	def *(factor:Float)= new Vec2(x*factor,y*factor)
	def /(that:Vec2)   = new Vec2(x/that.x,y/that.y)
	def /(that:Float)  = new Vec2(x/that,  y/that)
	def dot(that:Vec2) = x*that.x + y*that.y
	def min(that:Vec2) = new Vec2(x min that.x, y min that.y)
	def min = x min y
	def max = x max y
	def max(that:Vec2) = new Vec2(x max that.x, y max that.y)
}

import math._

final class AABB (
	x1:Float, y1:Float,
	x2:Float, y2:Float
){
	val minX = min(x1,x2)
	val minY = min(y1,y2)
	val maxX = max(x1,x2)
	val maxY = max(y1,y2)
	
	def this(v1:Vec2,v2:Vec2) = this( v1.x,v1.y, v2.x, v2.y )
	
	def intersects (that:AABB) =
		that.maxX < minX ||  that.maxY < minY || maxX < that.minX || maxY < that.minY		

}


final class Ray(
	val x1:Float, val y1:Float,
	val x2:Float, val y2:Float
){
	def v1 = Vec2(x1,y1)
	def v2 = Vec2(x2,y2)
	def lenX = x2 - x1
	def lenY = y2 - y1
	def lenVec = Vec2(lenX,lenY)
	def lenSq = lenX*lenX+lenY*lenY
	def len = scala.math.sqrt(lenSq).toFloat
	def dir = MathUtils.atan2(lenY, lenX)
	def minX = min(x1,x2)
	def minY = min(y1,y2)
	def maxX = max(x1,x2)
	def maxY = max(y1,y2)
	def minVec = Vec2(minX,minY)
	def maxVec = Vec2(maxY,maxY)
	def aabb = new AABB(x1,y1,x2,y2) 
}

final class Quad(
	val x1:Float, val y1:Float,
	val x2:Float, val y2:Float,
	val x3:Float, val y3:Float,
	val x4:Float, val y4:Float
){
	def v1 = Vec2(x1,y1)
	def v2 = Vec2(x2,y2)
	def v3 = Vec2(x3,y3)
	def v4 = Vec2(x4,y4)
	def floats   = Array(x1,y1,x2,y2,x3,y3,x4,y4)
	def vertices = Array(v1,v2,v3,v4)
	
	def minX = min(min(x1,x2), min(x3,x4))
	def minY = min(min(y1,y2), min(y3,y4))
	def maxX = max(max(x1,x2), max(x3,x4))
	def maxY = max(max(y1,y2), max(y3,y4))
	def minVec = Vec2(minX,minY)
	def maxVec = Vec2(maxX,maxY)
	def aabb = new AABB(minX,minY,maxX,maxY)
	
	def contains(x:Float, y:Float):Boolean = {
		var intersects = 0
		
		if (((y1 <= y && y < y2) || (y2 <= y && y < y1)) && x < ((x2 - x1) / (y2 - y1) * (y - y1) + x1)) 
			intersects += 1
		
		if (((y2 <= y && y < y3) || (y3 <= y && y < y2)) && x < ((x3 - x2) / (y3 - y2) * (y - y2) + x2)) 
			intersects += 1
		
		if (((y3 <= y && y < y4) || (y4 <= y && y < y3)) && x < ((x4 - x3) / (y4 - y3) * (y - y3) + x3)) 
			intersects += 1
		
		if (((y4 <= y && y < y1) || (y1 <= y && y < y4)) && x < ((x1 - x4) / (y1 - y4) * (y - y4) + x4)) 
			intersects += 1
		
		(intersects & 1) == 1
	}
}

class Map(val tileScale:Int, val fade:Double, val generator:Generator) {
	
	def div(x:Int,y:Int) = 
		if(x>=0) x/y else (x-y+1)/y
	def mod(x:Int,y:Int) =
		(x%y+y)%y
	
	val hashMap = new collection.mutable.HashMap[(Int,Int),Chunk]
	
	def get(x:Int,y:Int) = {
		hashMap.getOrElseUpdate((x,y),generator(x,y))
	}
	
	def rayTrace(startX:Double, startY:Double, dirX:Double, dirY:Double) {
		var posX = startX.floor.toInt
		var posY = startY.floor.toInt
		
		var stepX,stepY = 0
		var tMaxX = 0.0
		var tMaxY = 0.0
		
		stepX = if( dirX > 0 ) 1 else -1
		if(stepX == 1)
			tMaxX = (startX.ceil-startX)/dirX.abs
		else
			tMaxX = (startX-startX.floor)/dirX.abs
		
		
		stepY = if( dirY > 0 ) 1 else -1
		if(stepX == 1)
			tMaxY = (startY.ceil-startY)/dirY.abs
		else
			tMaxY = (startY-startY.floor)/dirY.abs
		
		val tDeltaX = (1/dirX).abs
		val tDeltaY = (1/dirY).abs
		
		var current = get(posX,posY)
		val i = 0
		while( current == 0 && i < 20 ){
			if(tMaxX < tMaxY){
				posX += stepX
				tMaxY += tDeltaX
			}
			else {
				posY += stepY
				tMaxY += tDeltaY
			}
			current = get(posX,posY)
		}
		if( current == 0 )
			None
		else
			Some( (posX,posY,current) )
	}
	
	def apply(x:Int,y:Int):Short         = get(div(x,chunkSize), div(y,chunkSize)).apply(mod(x,chunkSize),mod(y,chunkSize))
	def update(x:Int,y:Int,v:Short){ get(div(x,chunkSize), div(y,chunkSize)).update(mod(x,chunkSize),mod(y,chunkSize), v )}
	def outlineAt(x:Int,y:Int) = {
		val outline = get(div(x,chunkSize), div(y,chunkSize)).outlineAt(mod(x,chunkSize),mod(y,chunkSize),'right)
		val offsetX = x - mod(x,chunkSize)
		val offsetY = y - mod(y,chunkSize)
		for(i <- 0 until outline.length by 2){
			outline(i) += offsetX
			outline(i+1) += offsetY
		}
		outline
	}
	
}

class Chunk {
	val data = BufferUtils.createShortBuffer(chunkSize*chunkSize)
	
	var changed = false
	
	override def clone = {
		val c = new Chunk
		c.data.put(data)
		c.data.clear
		data.clear
		c.changed = true
		c
	}
	
	def apply(x:Int,y:Int)               = data.get(x*chunkSize+y)
	def update(x:Int,y:Int,v:Short) {
    data.put(x*chunkSize+y, v); changed = true
  }

	def outlineAt(x:Int,y:Int, startDir:Symbol) = {
		val vertices = collection.mutable.ArrayBuilder.make[Int]
		
		var xx = x
		var yy = y
		var dir = startDir
		
		def get(offsetX:Int,offsetY:Int):Short = {
			val x = xx+offsetX
			val y = yy+offsetY
			if(x<0 || y < 0 || x >= chunkSize || y >= chunkSize) 0 else data.get(x*chunkSize+y)
		}
		
		var counter = 1024
		do {
			dir match {
				case 'up =>
					if( get(0,1) == 0 ){
						dir = 'right
						vertices += xx
						vertices += yy+1
					}
					else if( get(0,1) != 0 && get(-1,1) == 0 ){
						yy += 1
					}
					else if( get(0,1) != 0 && get(-1,1) != 0 ){
						dir = 'left
						vertices += xx
						vertices += yy+1
						yy += 1
						xx -= 1
					}
				case 'down =>
					if( get(0,-1) == 0 ){
						dir = 'left
						vertices += xx + 1
						vertices += yy
					}
					else if( get(0,-1) != 0 && get(1,-1) == 0 ){
						yy -= 1
					}
					else if( get(0,-1) != 0 && get(1,-1) != 0 ){
						dir = 'right
						vertices += xx + 1
						vertices += yy
						xx += 1
						yy -= 1
					}
				case 'left =>
					if( get(-1,0) == 0 ) {
						dir = 'up
						vertices += xx
						vertices += yy
					}
					else if( get(-1,0) != 0 && get(-1,-1) == 0 ){
						xx -= 1
					}
					else if( get(-1,0) != 0 && get(-1,-1) != 0 ){
						dir = 'down
						vertices += xx
						vertices += yy
						xx -= 1
						yy -= 1
					}
				case 'right => 
					if( get(1,0) == 0 ) {
						dir = 'down
						vertices += xx+1
						vertices += yy+1
					}
					else if( get(1,0) != 0 && get(1,1) == 0 ){
						xx += 1
					}
					else if( get(1,0) != 0 && get(1,1) != 0 ){
						dir = 'up
						vertices += xx+1
						vertices += yy+1
						xx += 1
						yy += 1
					}
			}
			counter -= 1
		}
		while ( (xx != x || yy != y  || dir != startDir) && counter != 0)
		if(counter == 0) 
			Array[Int]()
		else
			vertices.result()
	}
	
	def outline:Array[Int] = {
		def get(x:Int,y:Int):Short = {
			if(x<0 || y < 0 || x >= chunkSize || y >= chunkSize) 0 else data.get(x*chunkSize+y)
		}
		
		for(x <- 0 until chunkSize; y <- 0 until chunkSize){
			if( get(x,y) == 1 ){
				if( get(x-1,y) == 0 )
					return outlineAt(x,y, 'up)
				if( get(x,y-1) == 0 )
					return outlineAt(x,y, 'left)
				if( get(x+1,y) == 0 )
					return outlineAt(x,y, 'down)
				if( get(x,y+1) == 0 )
					return outlineAt(x,y, 'right)
			}
		}

    Array[Int]()
	}
}


case class Cond( offX:Int, offY:Int, value:Int )
case class Replace( value:Short, conditions:Cond* )

class Generator(val worldFunction: (Int,Int) => Double) {
	
	val replacements = Seq( 
			Replace( 6, Cond(0,0,1), Cond(-1,0,1), Cond(1,0,1), Cond(-2,0,0), Cond(2,0,0), Cond(-1,1,0), Cond(1,1,0), Cond(0,1,0) ), //half
			
			Replace( 8, Cond(0,0,1), Cond(-1,0,0), Cond(0,1,0), Cond(1,0,1), Cond(1,1,0) ), // 50% slope up (lower part)
			Replace( 4, Cond(0,0,1), Cond(1,0,0), Cond(0,1,0), Cond(-1,0,1), Cond(-1,1,0) ), // 50% slope down (lower part)
			
			Replace( 9, Cond(0,0,1), Cond(-1,0,1), Cond(-2,0,0), Cond(-1,1,0), Cond(0,1,0) ), // 50% slope up (upper part)
			Replace( 3, Cond(0,0,1), Cond(1,0,1), Cond(2,0,0), Cond(1,1,0), Cond(0,1,0) ), // 50% slope down (upper part)

			Replace( 5, Cond(0,0,1), Cond(-1,0,1), Cond(0,1,0), Cond(1,0,0) ),
			Replace( 7, Cond(0,0,1), Cond(-1,0,0), Cond(0,1,0), Cond(1,0,1) ),
			Replace( 2, Cond(0,0,1), Cond(0,1,0) )
	)
	
	def apply(posX:Int,posY:Int) = {
		val offsetX = posX*chunkSize
		val offsetY = posY*chunkSize
		
		def depth(x:Int,y:Int):Short = 
			if( 0 < worldFunction(offsetX+x,offsetY+y) ) 0 else 1
		
		val chunk = new Chunk
		
		
		for(x <- 0 until chunkSize; y <- 0 until chunkSize) {
			val replacement = replacements.find( _.conditions.find( cond => depth(x+cond.offX, y+cond.offY) != cond.value ) == None )
			replacement match {
				case Some( r ) => chunk(x,y) = r.value
        case None => chunk(x,y) = depth(x,y)
			}
		}

		chunk
		
	}
}

