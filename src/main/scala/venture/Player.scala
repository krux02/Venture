package venture

object Player {
	val animation = new rendering.Animation(rendering.Texture.playerTexture, 2)
	
	var posX,posY = 0.0
	var dX,dY = 0.0
	
	var onGround = false
	var groundSpeed = 0.0
	
	def update() {
		posX = posX + dX
		posY = posY + dY
		
		dY -= 0.02
		
		dX *= 0.95
		dY *= 0.95
	}

}
