package eu.joaocosta.wodersky

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.graphics.image._
import eu.joaocosta.minart.graphics.pure._
import eu.joaocosta.minart.runtime._
import eu.joaocosta.wodersky.Constants._

object Rendering {

  val font  = Image.loadQoiImage(Resource("assets/font.qoi")).get
  val tiles = Image.loadQoiImage(Resource("assets/tiles.qoi")).get

  def writeChar(x: Int, y: Int, char: Char): MSurfaceIO[Unit] = {
    val (cx, cy) =
      if (char >= 'a' && char <= 'z') (fontWidth * (char - 'a').toInt, 0)
      else if (char >= '0' && char <= '9') (fontWidth * (char - '0').toInt, fontHeight)
      else if (char == '\b') (fontWidth * 10, fontHeight)
      else if (char == '\r') (fontWidth * 11, fontHeight)
      else if (char == ' ') (fontWidth * 12, fontHeight)
      else (25 * fontWidth, fontHeight)
    MSurfaceIO.blitWithMask(font, Color(255, 255, 255))(x, y, cx, cy, fontWidth, fontHeight)
  }

  def writeString(x: Int, y: Int, padding: Int, string: String): MSurfaceIO[Unit] = {
    val spacing = fontWidth + padding
    MSurfaceIO.foreach(string.toLowerCase().zipWithIndex) { case (char, idx) => writeChar(x + idx * spacing, y, char) }
  }

  def drawTile(x: Int, y: Int, state: GameState.TileState, char: String): MSurfaceIO[Unit] = {
    val cx = state match {
      case GameState.TileState.Empty   => 0
      case GameState.TileState.Wrong   => tileSize
      case GameState.TileState.Almost  => 2 * tileSize
      case GameState.TileState.Correct => 3 * tileSize
    }
    MSurfaceIO
      .blitWithMask(tiles, Color(255, 255, 255))(x, y, cx, 0, tileSize, tileSize)
      .andThen(writeString(x + 5, y + 5, 0, char))
  }

  def drawTiles(x: Int, y: Int, tiles: List[List[(Option[Char], GameState.TileState)]]): MSurfaceIO[Unit] = {
    val spacing = tileSize + tileSpacing
    val offsets = for {
      (line, yy)          <- tiles.zipWithIndex
      ((char, state), xx) <- line.zipWithIndex
    } yield (x + xx * spacing, y + yy * spacing, char.mkString(""), state)
    MSurfaceIO.foreach(offsets) { case (x, y, char, state) =>
      drawTile(x, y, state, char)
    }
  }

  def drawTime(x: Int, y: Int, spacing: Int, currentDay: Int) = {
    val nextDayStart        = (currentDay + 1 + firstPuzzle).toLong * puzzleInterval
    val remainingMillis     = nextDayStart - System.currentTimeMillis()
    val remainingHours      = math.max(remainingMillis.toDouble / (1000 * 60 * 60), 0)
    val remainingIntHours   = remainingHours.toInt
    val remainingIntMinutes = ((remainingHours - remainingIntHours) * 60).toInt
    writeString(x, y, spacing, s"Next in ${remainingIntHours}H${remainingIntMinutes}M")
  }

  def printShare(guesses: List[List[GameState.TileState]], currentDay: Int): Unit = {
    val fullGuesses = guesses.filter(_ != List.fill(5)(GameState.TileState.Empty))
    val numGuesses =
      Option
        .when(fullGuesses.last == List.fill(5)(GameState.TileState.Correct))(fullGuesses.size.toString)
        .getOrElse("X")
    val strings =
      s"Wodersky #$currentDay: $numGuesses/6" ::
        fullGuesses.map(_.map(tileEmoji).mkString)
    println(strings.mkString("\n"))
  }
}
