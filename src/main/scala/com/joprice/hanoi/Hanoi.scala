package com.joprice.hanoi

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.ext._
import scala.scalajs.js
import js.annotation.JSExport
import scala.concurrent.duration._
import scala.scalajs.js.timers.setTimeout
import scala.collection.mutable.{ArrayBuffer, Stack}

object Hanoi extends js.JSApp {

  implicit class CanvasExt(val ctx: dom.CanvasRenderingContext2D) extends AnyVal {
    def vline(x: Int, y: Int, width: Int = 0, color: String) = {
      ctx.strokeStyle = color
      ctx.beginPath()
      ctx.moveTo(x, 0)
      ctx.lineWidth = width
      ctx.lineTo(x, y)
      ctx.stroke()
    }
  }

  def main(): Unit = {
    val canvas = dom.document.createElement("canvas").cast[html.Canvas]
    val ctx = canvas.getContext("2d").cast[dom.CanvasRenderingContext2D]
    dom.document.body.appendChild(canvas)

    val canvasWidth = 500
    val canvasHeight = 300

    canvas.width = canvasWidth
    canvas.height = canvasHeight

    ctx.fillRect(0, 0, canvas.width, canvas.height)
    ctx.fillStyle = "black"

    val states = hanoi(10)
    render(states, ctx, canvasWidth = canvasWidth, canvasHeight = canvasHeight)
  }

  def render(
    states: Seq[Seq[Int]],
    ctx: dom.CanvasRenderingContext2D,
    canvasWidth: Int,
    canvasHeight: Int
  ) {
    val padding = 20
    val blockWidth = canvasWidth / 3
    val height = 20
    val vPadding = 8
    val stepWidth = 10
    val interval = .1.seconds

    def drawDisk(i: Int, j: Int): Unit = {
      val width = blockWidth - padding - (stepWidth * j)
      require(width > 0, "width must be positive")
      val x = (blockWidth * i) + (blockWidth / 2) - (width / 2)
      val y = canvasHeight - ((height + vPadding) * j)
      ctx.fillRect(x, y, width, height)
    }

    def clear() = ctx.clearRect(0, 0, canvasWidth, canvasHeight)

    def tower(i: Int, height: Int) = {
      ctx.fillStyle = "black"
      (1 to height).map { x =>
        drawDisk(i, x)
      }
    }

    def drawPoles() = {
      (0 until 3).map { i =>
        val x = (blockWidth * i) + (blockWidth / 2)
        ctx.vline(x = x, y = canvasHeight, width = 3, color = "red")
      }
    }

    def updateTowers(heights: Seq[Int]) = {
      clear()
      drawPoles()
      heights.zipWithIndex.map { case (x, i) =>
        tower(i, x)
      }
    }

    def display(states: Seq[Seq[Int]]) = {
      var i = 0
      def loop(): Unit = {
        if (i < states.size) {
          updateTowers(states(i))
          i += 1
          setTimeout(interval)(loop)
        }
      }
      loop()
    }

    display(states)
  }

  /*
   * Solves hanoi puzzle, returning sequence of states of tower heights
   **/
  def hanoi(height: Int): Seq[Seq[Int]] = {
    val states = ArrayBuffer[Seq[Int]]()

    val stacks = Vector(
      new Stack[Int](),
      new Stack[Int](),
      new Stack[Int]()
    )

    def step() = states += stacks.map(_.size)

    (0 until height).foreach(i => stacks(0).push(height - i))

    def moveDisks[T](
      n: Int,
      origin: Stack[T],
      dest: Stack[T],
      buffer: Stack[T]
    ): Unit = {
      if (n <= 0) return ()
      moveDisks(n - 1, origin, buffer, dest)
      moveTop(origin, dest)
      moveDisks(n - 1, buffer, dest, origin)
    }

    def moveTop[T](origin: Stack[T], dest: Stack[T]) = {
      dest.push(origin.pop())
      step()
    }

    //intialize with empty state
    step()
    moveDisks(height, stacks(0), stacks(2), stacks(1))
    states
  }
}

