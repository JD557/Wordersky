package eu.joaocosta.spacegame

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics.image._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.graphics.pure._
import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.runtime.pure._
import eu.joaocosta.minart.input.KeyboardInput
import eu.joaocosta.spacegame.Constants._

import scala.util.chaining._

object Main extends MinartApp {

  def updateState(state: GameState, input: KeyboardInput): GameState = {
    val updatePlayer: GameState => GameState =
      if (input.isDown(KeyboardInput.Key.Left)) _.movePlayer(-playerSpeed)
      else if (input.isDown(KeyboardInput.Key.Right)) _.movePlayer(playerSpeed)
      else identity

    val updateEnemies: GameState => GameState = _.moveEnemies

    val updateLasers: GameState => GameState =
      if (input.keysPressed(KeyboardInput.Key.Space)) _.shootLaser.updateLasers
      else _.updateLasers

    val updateBombs: GameState => GameState =
      if (state.enemies.exists(enemy => math.abs(enemy.x-state.player.x) < 3)) _.shootBomb.updateBombs
      else _.updateBombs

    state.pipe(updatePlayer).pipe(updateEnemies).pipe(updateLasers).pipe(updateBombs).pipe(_.checkCollisions)
  }

  type State = GameState
  val loopRunner     = LoopRunner()
  val canvasSettings = Canvas.Settings(width = screenWidth, height = screenHeight, scale = 1)
  val canvasManager  = CanvasManager()
  val initialState   = GameState()
  val frameRate      = LoopFrequency.hz60
  val terminateWhen  = (_: State) => false

  val backgroundImage = Image.loadBmpImage(Resource("assets/background.bmp")).get
  val shipImage = Image.loadPpmImage(Resource("assets/ship.ppm")).get
  val enemyImage = Image.loadPpmImage(Resource("assets/enemy.ppm")).get
  val hitShipImage = Image.invert(shipImage)
  val hitEnemyImage = Image.invert(enemyImage)
  val laserImage = Image.loadPpmImage(Resource("assets/laser.ppm")).get
  val bombImage = Image.loadBmpImage(Resource("assets/bomb.bmp")).get

  var lastTime = System.currentTimeMillis()
  var frame = 0
  def frameTime() = {
    frame += 1
    if (frame % 10 == 0) {
      println("FPS:" + 10000.0 / (System.currentTimeMillis - lastTime))
      lastTime = System.currentTimeMillis()
    }
  }

  val renderFrame = (state: GameState) => for {
    _ <- CanvasIO.redraw
    _ = frameTime()
    input <- CanvasIO.getKeyboardInput
    newState = updateState(state, input)
    _ <- CanvasIO.clear()
    _ <- CanvasIO.blit(backgroundImage)(0, 0)
    _ <- CanvasIO.foreach(newState.lasers) { laser => 
      CanvasIO.blitWithMask(laserImage, Color(0, 0, 0))(laser.x, laser.y)
    }
    _ <- CanvasIO.foreach(newState.bombs) { bomb => 
      CanvasIO.blitWithMask(bombImage, Color(0, 0, 0))(bomb.x, bomb.y)
    }
    _ <- if (newState.player.isHit)
        CanvasIO.blitWithMask(hitShipImage, Color(255, 255, 255))(newState.player.x, newState.player.y)
        else CanvasIO.blitWithMask(shipImage, Color(0, 0, 0))(newState.player.x, newState.player.y)
    _ <- CanvasIO.traverse(newState.enemies) { enemy => if (enemy.isHit)
        CanvasIO.blitWithMask(hitEnemyImage, Color(255, 255, 255))(enemy.x, enemy.y)
        else CanvasIO.blitWithMask(enemyImage, Color(0, 0, 0))(enemy.x, enemy.y)
    }
  } yield newState
}
