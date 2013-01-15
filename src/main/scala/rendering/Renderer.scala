package rendering

import com.badlogic.gdx.graphics.OrthographicCamera

/**
 * User: arne
 * Date: 15.01.13
 * Time: 03:00
 */

trait Renderer {
  def render(camera:OrthographicCamera)
}
