package eu.joaocosta.wodersky

import eu.joaocosta.wodersky.Constants._
import eu.joaocosta.wodersky.GameState._

sealed trait GameState {
  def guesses: List[String]
  def currentGuess: String
  def solution: String
  def lineDrift: Int

  lazy val tiles: List[List[(Option[Char], TileState)]] = {
    val emptyTile = (None, TileState.Empty)
    val guessedTiles: List[List[(Option[Char], TileState)]] =
      guesses.map(guess => toTiles(guess, solution).map { case (char, tile) => (Some(char), tile) }).toList
    val currentGuessTiles: List[(Option[Char], TileState)] =
      (currentGuess.map(char => (Some(char), TileState.Empty)) ++ List.fill(5)(emptyTile)).take(5).toList
    val remainingTiles: List[List[(Option[Char], TileState)]] = List.fill(6, 5)(emptyTile)
    (guessedTiles ++ (currentGuessTiles :: remainingTiles)).take(6)
  }
}

object GameState {
  final case class Results(guesses: List[String] = Nil, solution: String) extends GameState {
    val currentGuess = ""
    val lineDrift    = 0
  }

  final case class InGame(
      guesses: List[String] = Nil,
      currentGuess: String = "",
      dictionary: List[String] = Nil,
      lineDrift: Int = 0
  ) extends GameState {

    val solution   = dictionary.head
    val finalState = guesses.size >= 6 || guesses.lastOption.contains(solution)

    def addChar(char: Char): GameState =
      if (currentGuess.size >= 5) this
      else copy(currentGuess = currentGuess + char)

    def backspace =
      copy(currentGuess = currentGuess.init)

    def enterGuess =
      if (currentGuess.size < 5 || !dictionary.contains(currentGuess)) copy(lineDrift = 5)
      else
        copy(
          guesses = guesses :+ currentGuess,
          currentGuess = ""
        )

    def updateDrift = if (lineDrift > 0) copy(lineDrift = lineDrift - 1) else this

    val keys: Map[Char, TileState] = {
      val guessMap = tiles.flatten.groupMap(_._1)(_._2).view.mapValues(_.toSet)
      ('a' to 'z').map { char =>
        val guesses = guessMap.getOrElse(Some(char), Set.empty)
        val value =
          if (guesses(TileState.Correct)) TileState.Correct
          else if (guesses(TileState.Almost)) TileState.Almost
          else if (guesses(TileState.Wrong)) TileState.Wrong
          else TileState.Empty
        char -> value
      }.toMap
    }
  }

  def toTiles(string: String, solution: String): List[(Char, TileState)] = {
    val worstCase = string
      .zip(solution)
      .map { case (c1, c2) =>
        if (c1 == c2) c1 -> TileState.Correct
        else c1          -> TileState.Wrong
      }
      .toList
    val letterPool = string.zip(solution).filter(_ != _).map(_._2).mkString("")
    def aux(tiles: List[(Char, TileState)], pool: String): List[(Char, TileState)] = {
      lazy val (char, state) = tiles.head
      if (tiles.isEmpty || pool.isEmpty) tiles
      else if (state == TileState.Correct || !pool.contains(char)) tiles.head :: aux(tiles.tail, pool)
      else (char, TileState.Almost) :: aux(tiles.tail, pool.replaceFirst(char.toString, ""))
    }
    aux(worstCase, letterPool)
  }

  enum TileState {
    case Empty
    case Wrong
    case Almost
    case Correct
  }
}
