package etc

import com.badlogic.gdx.InputProcessor

trait EmptyInputProcessor extends InputProcessor {
	override def keyDown(keyCode: Int) = {
		false
	}

	override def keyUp(keyCode: Int) = {
		false
	}

	override def keyTyped(character: Char) = {
		false
	}

	override def touchDown(x: Int, y: Int, pointer: Int, button: Int) = {
		false
	}

	override def touchUp(x: Int, y: Int, pointer: Int, button: Int) = {
		false
	}

	override def touchDragged(x: Int, y: Int, pointer: Int) = {
		false
	}

	override def mouseMoved(x: Int, y: Int) = {
		false
	}

	override def scrolled(amount: Int) = {
		false
	}
}