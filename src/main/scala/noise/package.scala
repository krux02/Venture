import org.lwjgl.BufferUtils
package object util {
	
	@inline def fastfloor(x:Double) = x.floor.toInt
	def fade(t:Double) = t * t * t * (t * (t * 6 - 15) + 10)
	def hash(k:Int) = ((((k*34)+1)*k)%289+289)%289
	def lerp(t:Double, a:Double, b:Double) = a + t * (b - a)
	
	def bufferExtend(bufferData:java.nio.ByteBuffer, capacity:Int = 1) = {
		if( bufferData.capacity < bufferData.position + capacity ){
			val newCapacity = bufferData.capacity * (1+(capacity+bufferData.capacity-1)/bufferData.capacity)
			val newBufferData = BufferUtils createByteBuffer newCapacity
			newBufferData put bufferData
			newBufferData
		}
		else
			bufferData
	}
	
	def noise3(x:Double, y:Double, z:Double):Double = {
		def grad(hash:Int, x:Double, y:Double, z:Double) = {
			val h = hash & 15
			val u = if(h<8) x else y
			val v = if(h<4) y else {if(h==12 || h==14) x else z}
			(if((h&1) == 0) u else -u) + (if((h&2) == 0) v else -v)
		}
		
		val X = fastfloor(x)
		val Y = fastfloor(y)
		val Z = fastfloor(z)

		val relx = x - X
		val rely = y - Y
		val relz = z - Z

		val u = fade(relx)
		val v = fade(rely)
		val w = fade(relz)
		
		val A = hash(X  )+Y; val AA = hash(A)+Z; val AB = hash(A+1)+Z		// HASH COORDINATES OF
		val	B = hash(X+1)+Y; val BA = hash(B)+Z; val BB = hash(B+1)+Z		// THE 8 CUBE CORNERS,

		lerp(w,	lerp(v,	lerp(u, grad(hash(AA  ), relx  , rely  , relz	),  // AND ADD
								grad(hash(BA  ), relx-1, rely  , relz	)), // BLENDED
						lerp(u, grad(hash(AB  ), relx  , rely-1, relz	),  // RESULTS
								grad(hash(BB  ), relx-1, rely-1, relz	))),// FROM  8
				lerp(v, lerp(u, grad(hash(AA+1), relx  , rely  , relz-1 ),  // CORNERS
								grad(hash(BA+1), relx-1, rely  , relz-1 )), // OF CUBE
						lerp(u, grad(hash(AB+1), relx  , rely-1, relz-1 ),
								grad(hash(BB+1), relx-1, rely-1, relz-1 ))))
	}
}