package com.codingame.game

import com.codingame.game.model.*
import com.codingame.gameengine.module.entities.Entity
import com.codingame.gameengine.module.entities.GraphicEntityModule
import com.codingame.gameengine.module.entities.Sprite
import com.codingame.gameengine.module.entities.Text

var cellWidth: Int = 0
val cellSpacing = 5
val yOffset = 100
val xOffset = 50

class BoardView(val graphicEntityModule: GraphicEntityModule, baseBoard: Board, matchPlayers: List<Player>) {

  lateinit var board: Board
  lateinit var players: List<Player>
  lateinit var queue: CustomerQueue

  var scores = mutableMapOf<Int, Text>()
  private var queueSprites: List<ItemSpriteGroup>
  private var queueAwards: List<Text>
  private var cellViews: MutableList<CellView> = mutableListOf()

  init {
    queueSprites = List(3) {
      ItemSpriteGroup(graphicEntityModule, 50)
    }

    queueAwards = List(3) {
      graphicEntityModule.createText("0").setFillColor(0xffffff)
    }

    val floorColor = 0xe0e0eb
    val tableColor = 0x756b68
//    val floorColor = 0xe0e0eb
//    val tableColor = 0xb35900

    val worldWidth = 1920
    val worldHeight = 1080

    val gridHeight = worldHeight - yOffset
    val gridWidth = worldWidth - xOffset
    cellWidth = Math.min(gridHeight / baseBoard.height, gridWidth / baseBoard.width) - cellSpacing

    for (cellCol in baseBoard.cells) {

      for (cell in cellCol) {
        val x = cell.x * (cellWidth + cellSpacing) + xOffset
        val y = cell.y * (cellWidth + cellSpacing) + yOffset

        cellViews.add(CellView(cell).apply {
          background = graphicEntityModule
              .createRectangle()
              .setHeight(cellWidth)
              .setWidth(cellWidth)
              .setFillColor(if (cell.isTable) tableColor else floorColor)

          val equipment = cell.equipment
          content = graphicEntityModule.createSprite().apply {
            baseHeight = cellWidth - 8
            baseWidth = cellWidth - 8
            anchorX = 0.5
            anchorY = 0.5
            setX(cellWidth / 2)
            setY(cellWidth / 2)
            when (equipment) {
              is ChoppingBoard -> image = "board.png"
              is GeneralCrate -> image = "crate.png"
              is Oven -> image = "oven.png"
              is Window -> image = "window.png"
              is WaffleIron -> image = "waffle-iron.png"
              is DishReturn -> image = "dishwasher.png"
              is Jarbage -> image = "trash.png"
            }
          }
          secondaryContent = graphicEntityModule.createSprite().apply {
            when (equipment) {
              is BananaCrate -> image = "banana.png"
              is BlueberryCrate -> image = "blueberries.png"
              is StrawberryCrate -> image = "strawberry.png"
              is IceCreamCrate -> image = "ice-cream.png"
              is PieCrustCrate -> image = "pie.png"
              else -> isVisible = false
            }
            baseHeight = cellWidth / 2
            baseWidth = cellWidth / 2
            anchorX = 0.5
            anchorY = 0.5
            setX(cellWidth / 2)
            setY(cellWidth / 2)
          }

          itemSpriteGroup = ItemSpriteGroup(graphicEntityModule)

          group = graphicEntityModule.createGroup(background, content, secondaryContent, itemSpriteGroup.group)
              .setX(x).setY(y)

        })
      }
    }

    for (player in matchPlayers) {
      player.characterSprite = graphicEntityModule.createSprite().apply {
        image = "chef.png"
        baseHeight = cellWidth
        baseWidth = cellWidth
        center()
        tint = player.colorToken
      }

      player.itemSprite = ItemSpriteGroup(graphicEntityModule)

      player.sprite = graphicEntityModule.createGroup(player.characterSprite, player.itemSprite.group)
//          .setX(player.location.view.group.x + 5)
//          .setY(player.location.view.group.y + 5)
    }

    // TODO: Replace this
//    graphicEntityModule.createRectangle().setX(0).setY(10).setFillColor(players[0].colorToken).setHeight(15).setWidth(15)
//    graphicEntityModule.createRectangle().setX(0).setY(30).setFillColor(players[3].colorToken).setHeight(15).setWidth(15)
//    scores[0] = graphicEntityModule.createText("0").setX(20).setY(20).setFillColor(0xffffff)
//
//    graphicEntityModule.createRectangle().setX(400).setY(10).setFillColor(players[1].colorToken).setHeight(15).setWidth(15)
//    graphicEntityModule.createRectangle().setX(400).setY(30).setFillColor(players[2].colorToken).setHeight(15).setWidth(15)
//    scores[1] = graphicEntityModule.createText("0").setX(420).setY(20).setFillColor(0xffffff)
  }

  fun updateQueue() {
    queueSprites.forEachIndexed { index, sprite ->
      sprite.group.apply { x = 500 + (100 * index); y = 10 }

      if(queue.size > index) {
        queueAwards[index].text = queue[index].award.toString()
        queueAwards[index].apply { x = 500 + (100 * index); y = 55 }
        queueAwards[index].isVisible = true

        sprite.update(queue[index].item)
        sprite.group.isVisible = queue[index].award > 0
      } else {
        sprite.group.isVisible = false
        queueAwards[index].isVisible = false
      }
    }
  }

  fun updateCells(boardCells: List<Cell>) {
    boardCells.zip(cellViews).forEach { (cell, view) ->
      view.itemSpriteGroup.update(cell.item) }
  }

  fun <T : Entity<*>?> Entity<T>.setLocation(cell: Cell) {
    // TODO: PLEEEASE fix me. Such hacks :(
    x = cell.x * (cellWidth + cellSpacing) + xOffset + 5
    y = cell.y * (cellWidth + cellSpacing) + yOffset + 5
  }

  fun updatePlayer(player: Player, useTarget: Cell?) {
    player.characterSprite.isVisible = true
    player.itemSprite.isVisible = true

    player.itemSprite.update(player.heldItem)

    if (useTarget == null) {
      player.sprite.setLocation(board[player.location.x, player.location.y])
    } else {
      player.sprite.setLocation(useTarget)
      graphicEntityModule.commitEntityState(0.3, player.sprite)
      player.sprite.setLocation(board[player.location.x, player.location.y])
      graphicEntityModule.commitEntityState(0.6, player.sprite)
    }

    graphicEntityModule.commitEntityState(0.5, player.sprite)

  }

  fun removePlayer(player: Player) {
    player.characterSprite.isVisible = false
    player.itemSprite.isVisible = false
  }
}

class ItemSpriteGroup(graphicEntityModule: GraphicEntityModule, width: Int = cellWidth) {
  val mainSprite = graphicEntityModule.createSprite().apply {
    center()
    baseHeight = width + 4
    baseWidth = width + 4
    zIndex = 50
    isVisible = false
  }

  val subSprites = List(4) { i ->
    graphicEntityModule.createSprite().apply {
      center()
      baseHeight = width / 2
      baseWidth = width / 2
      zIndex = 50 + i
      x = ((i - 1.5) * 8 + width / 2).toInt()
      y = ((i - 1.5) * 8 + width / 2).toInt()
      isVisible = false
    }
  }

  val group = graphicEntityModule.createGroup(*(subSprites + mainSprite).toTypedArray())
  var isVisible: Boolean = true
  set(value) {
    mainSprite.isVisible = value
    subSprites.forEach { it.isVisible = value }
  }

  fun update(item: Item?) {
    subSprites.forEach { it.isVisible = false }
    mainSprite.apply {
      isVisible = true

      when(item) {
        is Banana -> image = "banana.png"
        is ChoppedBananas -> image = "open-banana.png"
        is Waffle -> image = "waffle.png"
        is RawPie -> image = "pie.png"  // TODO: Add flavour
        is Pie -> image = "pie.png"  // TODO: Add flavour and tint
        is PieSlice -> image = "pie-slice.png"  // TODO: add flavour
        is IceCream -> image = "ice-cream.png"
        is Strawberries -> image = "strawberry.png"
        is Blueberries -> image = "blueberries.png"
        is Dish -> {
          image = "dish.png"
          item.contents.zip(subSprites).forEach { (edible, subSprite) ->
            subSprite.apply {
              isVisible = true
              when (edible) {
                is PieSlice -> image = "pie-slice.png"  // TODO: add flavour
                is IceCream -> image = "ice-cream.png"
                is Strawberries -> image = "strawberry.png"
                is Blueberries -> image = "blueberries.png"
                is Waffle -> image = "waffle.png"
                is ChoppedBananas -> image = "open-banana.png"
              }
            }
          }
        }
        else -> mainSprite.isVisible = false
      }
    }
  }
}

private fun Sprite.center() {
  anchorY = 0.5
  anchorX = 0.5
  x = cellWidth / 2
  y = cellWidth / 2
}
