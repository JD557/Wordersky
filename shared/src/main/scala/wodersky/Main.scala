package eu.joaocosta.wodersky

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics.image._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.graphics.pure._
import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.runtime.pure._
import eu.joaocosta.minart.input.KeyboardInput
import eu.joaocosta.wodersky.Constants._

object Main extends MinartApp {

  val day = (System.currentTimeMillis() / puzzleInterval).toInt - firstPuzzle
  val dictionary = Resource("assets/dictionary.txt").withSource(source =>
    source.getLines().map(_.toLowerCase()).toList
  ).map(scala.util.Random(day).shuffle).get

  val font = Image.loadQoiImage(Resource("assets/font.qoi")).get
  val tiles = Image.loadQoiImage(Resource("assets/tiles.qoi")).get

  def writeChar(x: Int, y: Int, char: Char): MSurfaceIO[Unit] = {
    val (cx, cy) =
      if (char >= 'a' && char <= 'z') (fontWidth * (char - 'a').toInt, 0)
      else if (char >= '0' && char <= '9') (fontWidth * (char - '0').toInt, fontHeight)
      else (25 * fontWidth, fontHeight)
    MSurfaceIO.blitWithMask(font, Color(255, 255, 255))(x, y, cx, cy, fontWidth, fontHeight)
  }

  def writeString(x: Int, y: Int, padding: Int, string: String): MSurfaceIO[Unit] = {
    val spacing = fontWidth + padding
    MSurfaceIO.foreach(string.toLowerCase().zipWithIndex) { case (char, idx) => writeChar(x + idx * spacing, y, char) }
  }

  def drawTile(x: Int, y: Int, state: GameState.TileState, char: String): MSurfaceIO[Unit] = {
    val cx = state match {
      case GameState.TileState.Empty => 0
      case GameState.TileState.Wrong => tileSize
      case GameState.TileState.Almost => 2 * tileSize
      case GameState.TileState.Correct => 3 * tileSize
    }
    MSurfaceIO.blitWithMask(tiles, Color(255, 255, 255))(x, y, cx, 0, tileSize, tileSize)
      .andThen(writeString(x + 5, y + 5, 0, char))
  }

  def drawTiles(x: Int, y: Int, tiles: List[List[(Option[Char], GameState.TileState)]]): MSurfaceIO[Unit] = {
    val spacing = tileSize + tileSpacing
    val offsets = for {
     (line, yy) <- tiles.zipWithIndex
     ((char, state), xx) <- line.zipWithIndex 
    } yield (x + xx * spacing, y + yy * spacing, char.mkString(""), state)
    MSurfaceIO.foreach(offsets) { case (x, y, char, state) =>
      drawTile(x, y, state, char)
    }
  }

  def drawTime(x: Int, y: Int, spacing: Int) = {
    val nextDayStart = (day + 1 + firstPuzzle).toLong * puzzleInterval
    val remainingMillis = nextDayStart - System.currentTimeMillis()
    val remainingHours = math.max(remainingMillis / (1000 * 60 * 60), 0)
    writeString(x, y, spacing, s"Next in $remainingHours H")
  }

  type State = GameState
  val loopRunner     = LoopRunner()
  val canvasSettings = Canvas.Settings(width = screenWidth, height = screenHeight, scale = 1, clearColor = Color(255, 255, 255))
  val canvasManager  = CanvasManager()
  val initialState   = GameState.InGame(dictionary = dictionary)
  val frameRate      = LoopFrequency.hz60
  val terminateWhen  = (_: State) => false

  val keyOrder = List(
    "qwertyuiop",
    "asdfghjkl",
    "zxcvbnm",
  ).map(_.toList)

  val keyToChar: Map[KeyboardInput.Key, Char] = {
    import eu.joaocosta.minart.input.KeyboardInput.Key._
    Map(
      A -> 'a',
      B -> 'b',
      C -> 'c',
      D -> 'd',
      E -> 'e',
      F -> 'f',
      G -> 'g',
      H -> 'h',
      I -> 'i',
      J -> 'j',
      K -> 'k',
      L -> 'l',
      M -> 'm',
      N -> 'n',
      O -> 'o',
      P -> 'p',
      Q -> 'q',
      R -> 'r',
      S -> 's',
      T -> 't',
      U -> 'u',
      V -> 'v',
      W -> 'w',
      X -> 'x',
      Y -> 'y',
      Z -> 'z'
    )
  }

  val tileEmoji: GameState.TileState => String = {
    case GameState.TileState.Empty => "⬜"
    case GameState.TileState.Wrong => "⬜"
    case GameState.TileState.Almost => "🟨"
    case GameState.TileState.Correct => "🟩"
  }

  def printShare(guesses: List[List[GameState.TileState]]): Unit = {
    val fullGuesses = guesses.filter(_ != List.fill(5)(GameState.TileState.Empty))
    val numGuesses =
      Option.when(fullGuesses.last == List.fill(5)(GameState.TileState.Correct))(
        fullGuesses.size.toString).getOrElse("X")
    println(s"Wodersky #$day: $numGuesses/6")
    fullGuesses.map(_.map(tileEmoji).mkString).foreach(println)
  }

  def nextState(state: GameState, input: KeyboardInput): GameState = state match {
    case st: GameState.InGame =>
      if (st.finalState) {
        printShare(st.tiles.map(_.map(_._2)))
        GameState.Results(st.guesses, st.solution)
      }
      else if (input.keysPressed(KeyboardInput.Key.Backspace))
        st.backspace
      else if (input.keysPressed(KeyboardInput.Key.Enter))
        st.enterGuess
      else input.keysPressed.flatMap(key => keyToChar.get(key)).headOption.fold(st)(char => st.addChar(char))
    case _ => state
  }

  val renderFrame = (state: GameState) => for {
    _ <- CanvasIO.redraw
    input <- CanvasIO.getKeyboardInput
    _ <- CanvasIO.clear()
    _ <- writeString(titleX, titleY, titleSpacing, title)
    _ <- drawTiles(tilesPadding, tilesY, state.tiles)
    _ <- state match {
      case st: GameState.InGame =>
        drawTiles(keyboardPadding, keyboardY, keyOrder.map(_.map(k => Some(k) -> st.keys(k))))
      case st: GameState.Results =>
        drawTime(keyboardPadding, keyboardY, titleSpacing)
    }
  } yield nextState(state, input)
}
