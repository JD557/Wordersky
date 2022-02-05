package eu.joaocosta.wodersky

object Constants {
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
}
