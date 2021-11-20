package eu.joaocosta.spacegame

import eu.joaocosta.spacegame.Constants._

case class GameState(
  player: Entity.Player = Entity.Player(0),
  enemies: List[Entity.Enemy] = List.range(0, 301, 150).map { startX =>
    Entity.Enemy(startX, 0, startX, startX + screenWidth - Constants.enemyW - 300)
  },
  lasers: List[Entity.Laser] = Nil,
  bombs: List[Entity.Bomb] = Nil
) {

  def movePlayer(dx: Int) =
    copy(player = player.move(dx))

  def moveEnemies =
    copy(enemies = enemies.map(_.move))

  def updateLasers =
    copy(lasers = lasers.map(_.move).filter(_.y >= 0))

  def updateBombs =
    copy(bombs = bombs.map(_.move).filter(_.y < screenHeight))

  def shootLaser =
    copy(lasers = Entity.Laser(player.x, player.y) :: Entity.Laser(player.x + player.w - Constants.laserW, player.y) :: lasers)

  def shootBomb =
    copy(bombs = enemies.map(enemy => Entity.Bomb(enemy.x + (enemy.w + Constants.bombW) / 2, enemy.y + Constants.bombH)) ++ bombs)

  def checkCollisions = {
    val (collidedLasers, freeLasers) = lasers.partition { laser =>
      enemies.exists(_.collidesWith(laser))
    }
    val (collidedBombs, freeBombs) = bombs.partition { bomb =>
      player.collidesWith(bomb)
    }
    val playerHit = collidedBombs.nonEmpty
    val updatedEnemies = enemies.map { enemy =>
      val isHit = collidedLasers.exists { laser =>
        enemy.collidesWith(laser)
      }
      enemy.setHit(isHit)
    }
    copy(
      player = player.setHit(playerHit),
      enemies = updatedEnemies,
      lasers = freeLasers,
      bombs = freeBombs
    )
  }
}
