package eu.joaocosta.wodersky

import eu.joaocosta.minart.input.KeyboardInput

object Constants {
  val puzzleInterval = (1000 * 60 * 60 * 24)
  val firstPuzzle = 19028

  val tileSize = 32
  val tileSpacing = 4
  val componentSpacing = 16
  
  val fontWidth = 22
  val fontHeight = 24

  val keyboardPadding = 16

  val screenWidth = 2 * keyboardPadding + 10 * (tileSize + tileSpacing)

  val title = "Wordersky"
  val titleSpacing = 4
  val titleX = (screenWidth - (fontWidth + titleSpacing) * title.size) / 2
  val titleY = componentSpacing

  val tilesPadding = (screenWidth - (tileSize + tileSpacing) * 5) / 2
  val tilesY = titleY + fontHeight + componentSpacing

  val keyboardY = tilesY + 6 * (tileSize + tileSpacing) + componentSpacing

  val screenHeight = keyboardY + 3 * (tileSize + tileSpacing) + componentSpacing

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
}
