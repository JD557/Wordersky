package eu.joaocosta.wodersky

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.graphics.image._
import eu.joaocosta.minart.graphics.pure._
import eu.joaocosta.minart.input.KeyboardInput
import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.runtime.pure._
import eu.joaocosta.wodersky.Constants._
import eu.joaocosta.wodersky.Rendering._

object Main extends MinartApp {

  val day = (System.currentTimeMillis() / puzzleInterval).toInt - firstPuzzle
  val dictionary = Resource("assets/dictionary.txt")
    .withSource(source => source.getLines().map(_.toLowerCase()).toList)
    .map(scala.util.Random(day).shuffle)
    .get

  type State = GameState
  val loopRunner = LoopRunner()
  val canvasSettings =
    Canvas.Settings(width = screenWidth, height = screenHeight, scale = 1, clearColor = Color(255, 255, 255))
  val canvasManager = CanvasManager()
  val initialState  = GameState.InGame(dictionary = dictionary)
  val frameRate     = LoopFrequency.hz60
  val terminateWhen = (_: State) => false

  def nextState(state: GameState, input: KeyboardInput): GameState = state match {
    case st: GameState.InGame =>
      if (st.finalState) {
        printShare(st.tiles.map(_.map(_._2)), day)
        GameState.Results(st.guesses, st.solution)
      } else if (input.keysPressed(KeyboardInput.Key.Backspace))
        st.backspace
      else if (input.keysPressed(KeyboardInput.Key.Enter))
        st.enterGuess
      else input.keysPressed.flatMap(key => keyToChar.get(key)).headOption.fold(st)(char => st.addChar(char))
    case _ => state
  }

  val renderFrame = (state: GameState) =>
    for {
      _     <- CanvasIO.redraw
      input <- CanvasIO.getKeyboardInput
      _     <- CanvasIO.clear()
      _     <- writeString(titleX, titleY, titleSpacing, title)
      _     <- drawTiles(tilesPadding, tilesY, state.tiles)
      _ <- state match {
        case st: GameState.InGame =>
          drawTiles(keyboardPadding, keyboardY, keyOrder.map(_.map(k => Some(k) -> st.keys(k))))
        case st: GameState.Results =>
          drawTime(keyboardPadding, keyboardY, titleSpacing, day)
      }
    } yield nextState(state, input)
}
