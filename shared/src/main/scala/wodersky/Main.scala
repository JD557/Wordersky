package eu.joaocosta.wodersky

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.graphics.image._
import eu.joaocosta.minart.graphics.pure._
import eu.joaocosta.minart.input.{KeyboardInput, PointerInput}
import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.runtime.pure._
import eu.joaocosta.wodersky.Constants._
import eu.joaocosta.wodersky.Rendering._

object Main extends MinartApp[GameState, LowLevelCanvas] {

  val loopRunner      = LoopRunner()
  val createSubsystem = () => LowLevelCanvas.create()
  val canvasSettings =
    Canvas.Settings(width = screenWidth, height = screenHeight, scale = Some(1), clearColor = Color(255, 255, 255))

  val day = (System.currentTimeMillis() / puzzleInterval).toInt - firstPuzzle
  val answers = Resource("assets/answers.txt")
    .withSource(source => source.getLines().map(_.toLowerCase()).toList)
    .map(scala.util.Random(day).shuffle)
    .get
  val guesses = Resource("assets/guesses.txt")
    .withSource(source => source.getLines().map(_.toLowerCase()).toList)
    .get
  val dictionary = answers ++ guesses

  val initialState = GameState.InGame(dictionary = dictionary)

  def mouseClick(input: PointerInput): Option[Char] = {
    def absDistance(x1: Int, y1: Int, x2: Int, y2: Int) =
      math.max(math.abs(x2 - x1), math.abs(y2 - y1))
    def buttonCenter(x: Int, y: Int): (Int, Int) = (
      (keyboardPadding + x * (tileSize + tileSpacing) + tileSize / 2),
      (keyboardY + y * (tileSize + tileSpacing) + tileSize / 2)
    )
    (for {
      clickPos   <- input.pointsPressed.to(LazyList)
      releasePos <- input.pointsReleased.to(LazyList)
      (line, y)  <- keyOrder.zipWithIndex
      (char, x)  <- line.zipWithIndex
      (centerX, centerY) = buttonCenter(x, y)
      distanceClick      = absDistance(centerX, centerY, clickPos.x, clickPos.y)
      distanceRelease    = absDistance(centerX, centerY, releasePos.x, releasePos.y)
      if (distanceClick < tileSize / 2 && distanceRelease < tileSize / 2)
    } yield char).headOption
  }

  def nextState(state: GameState, keyboard: KeyboardInput, mouse: Option[PointerInput]): GameState = state match {
    case prevState: GameState.InGame =>
      lazy val mouseInput = mouse.flatMap(mouseClick _)
      val st              = prevState.updateDrift
      if (st.finalState)
        printShare(st.tiles.map(_.map(_._2)), day)
        GameState.Results(st.guesses, st.solution)
      else if (mouseInput.isDefined)
        mouseInput.fold(st) {
          case '\b' => st.backspace
          case '\r' => st.enterGuess
          case char => st.addChar(char)
        }
      else if (keyboard.keysPressed(KeyboardInput.Key.Backspace))
        st.backspace
      else if (keyboard.keysPressed(KeyboardInput.Key.Enter))
        st.enterGuess
      else keyboard.keysPressed.flatMap(key => keyToChar.get(key)).headOption.fold(st)(char => st.addChar(char))
    case _ => state
  }

  val appLoop = AppLoop
    .statefulRenderLoop((state: GameState) =>
      for {
        _        <- CanvasIO.redraw
        keyboard <- CanvasIO.getKeyboardInput
        _        <- CanvasIO.clear(Set(Canvas.Buffer.KeyboardBuffer))
        mouse    <- CanvasIO.getPointerInput.map(m => Option.when(!m.isPressed)(m))
        _        <- CanvasIO.when(mouse.isDefined)(CanvasIO.clear(Set(Canvas.Buffer.PointerBuffer)))
        _        <- CanvasIO.clear(Set(Canvas.Buffer.Backbuffer))
        _        <- writeString(titleX, titleY, titleSpacing, title)
        _        <- drawTiles(tilesPadding, tilesY - state.lineDrift, state.tiles)
        _ <- state match {
          case st: GameState.InGame =>
            drawTiles(
              keyboardPadding,
              keyboardY,
              keyOrder.map(_.map(k => Some(k) -> st.keys.getOrElse(k, GameState.TileState.Empty)))
            )
          case st: GameState.Results =>
            drawTime(keyboardPadding, keyboardY, titleSpacing, day)
        }
      } yield nextState(state, keyboard, mouse)
    )
    .configure(canvasSettings, LoopFrequency.hz60, initialState)
}
