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

  val dictionary = Resource("assets/dictionary.txt").withSource(source =>
    source.getLines().map(_.toLowerCase()).toVector
  ).map(scala.util.Random(0).shuffle).get

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

  println(s"Loaded ${dictionary.size} words")

  val day = ((System.currentTimeMillis() / (1000 * 60 * 60 * 24)) % dictionary.size).toInt

  println(s"Word of the day: ${dictionary(day)}")

  type State = GameState
  val loopRunner     = LoopRunner()
  val canvasSettings = Canvas.Settings(width = screenWidth, height = screenHeight, scale = 1, clearColor = Color(255, 255, 255))
  val canvasManager  = CanvasManager()
  val initialState   = GameState(List("scala", "circe", "spray"), "teste", dictionary(day))
  val frameRate      = LoopFrequency.hz60
  val terminateWhen  = (_: State) => false

  val keyOrder = List(
    "qwertyuiop",
    "asdfghjkl",
    "zxcvbnm",
  ).map(_.toList)

  val renderFrame = (state: GameState) => for {
    _ <- CanvasIO.redraw
    input <- CanvasIO.getKeyboardInput
    _ <- CanvasIO.clear()
    _ <- writeString(titleX, titleY, titleSpacing, title)
    _ <- drawTiles(tilesPadding, tilesY, state.tiles)
    _ <- drawTiles(keyboardPadding, keyboardY, keyOrder.map(_.map(k => Some(k) -> state.keys(k))))
  } yield state
}
