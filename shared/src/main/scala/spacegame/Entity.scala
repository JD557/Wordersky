package eu.joaocosta.spacegame

trait Entity {
  def x: Int
  def y: Int
  def w: Int
  def h: Int
  
  def collidesWith(that: Entity): Boolean = {
    (this.x + this.w >= that.x) &&
    (this.x <= that.x + that.w) &&
    (this.y + this.h >= that.y) &&
    (this.y <= that.y + that.h)
  }
}

object Entity {
  case class Player(x: Int, isHit: Boolean = false) extends Entity {
    val y = Constants.playerY
    val w = Constants.playerW
    val h = Constants.playerH

    def move(dx: Int) =
      copy(x = math.min(math.max(0, x + dx), Constants.screenWidth - w))

    def setHit(hit: Boolean) =
      copy(isHit = hit)
  }

  case class Enemy(x: Int, y: Int, minX: Int, maxX: Int, speed: Int = Constants.enemySpeed, isHit: Boolean = false) extends Entity {
    val w = Constants.enemyW
    val h = Constants.enemyH

    def move = {
      val nextX = x + speed
      if (nextX < minX || nextX > maxX) changeDirection
      else copy(x = nextX)
    }

    def changeDirection =
      copy(speed = -speed)

    def setHit(hit: Boolean) =
      copy(isHit = hit)
  }

  case class Laser(x: Int, y: Int) extends Entity {
    val w = Constants.laserW
    val h = Constants.laserH

    def move = copy(y = y - Constants.laserSpeed)
  }

  case class Bomb(x: Int, y: Int) extends Entity {
    val w = Constants.bombW
    val h = Constants.bombH

    def move = copy(y = y + Constants.bombSpeed)
  }

}
