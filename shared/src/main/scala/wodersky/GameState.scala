package eu.joaocosta.wodersky

import eu.joaocosta.wodersky.Constants._
import eu.joaocosta.wodersky.GameState._

sealed trait GameState {
  def guesses: List[String]
  def currentGuess: String
  def solution: String

  lazy val tiles: List[List[(Option[Char], TileState)]] = {
    val emptyTile = (None, TileState.Empty)
    val guessedTiles: List[List[(Option[Char], TileState)]] =
      guesses.map(guess => toTiles(guess, solution).map { case (char, tile) => (Some(char), tile)}).toList
    val currentGuessTiles: List[(Option[Char], TileState)] =
      (currentGuess.map(char => (Some(char), TileState.Empty)) ++ List.fill(5)(emptyTile)).take(5).toList
    val remainingTiles: List[List[(Option[Char], TileState)]] = List.fill(6, 5)(emptyTile)
    (guessedTiles ++ (currentGuessTiles :: remainingTiles)).take(6)
  }
}

object GameState {
  final case class Results(guesses: List[String] = Nil, solution: String) extends GameState {
    val currentGuess = ""
  }

  final case class InGame(
    guesses: List[String] = Nil,
    currentGuess: String = "",
    dictionary: List[String] = Nil
  ) extends GameState {

    val solution = dictionary.head
    val finalState = guesses.size >= 6 || guesses.lastOption.contains(solution)

    def addChar(char: Char): GameState =
      if (currentGuess.size >= 5) this
      else copy(currentGuess = currentGuess + char)

    def backspace =
      copy(currentGuess = currentGuess.init)

    def enterGuess =
      if (currentGuess.size < 5 || !dictionary.contains(currentGuess)) this
      else copy(
        guesses = guesses :+ currentGuess,
        currentGuess = ""
      )

    val keys: Map[Char, TileState] = {
      val guessMap = tiles.flatten.groupMap(_._1)(_._2).view.mapValues(_.toSet)
      ('a' to 'z').map { char =>
        val guesses = guessMap.getOrElse(Some(char), Set.empty)
        val value =
          if (guesses(TileState.Correct)) TileState.Correct
          else if (guesses(TileState.Almost)) TileState.Almost
          else TileState.Wrong
        char -> value
      }.toMap
    }
  }

  def toTiles(string: String, solution: String): List[(Char, TileState)] =
    if (string.isEmpty || solution.isEmpty) Nil
    else {
      val guess = string.head
      if (guess == solution.head)
        (guess, TileState.Correct) :: toTiles(string.tail, solution.tail)
      else if (solution.contains(guess))
        (guess, TileState.Almost) :: toTiles(string.tail, solution.replaceFirst(guess.toString, "") + solution.head)
      else
        (guess, TileState.Wrong) :: toTiles(string.tail, solution.tail + solution.head)
    }

  enum TileState {
    case Empty
    case Wrong
    case Almost
    case Correct
  }
}
