/*
 * The MIT License (MIT)
 * Copyright (c) 2020, Jens Nazarenus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package spinal.lib.sidechannel

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.sidechannel.HideMethods._

import scala.collection.mutable.ListBuffer


object TestHiding {

  /**
   * Example Test componenent to test the PseudoRandom object
   * @param start Count from
   * @param end Count to
   */
  class HidingExample(start: Int, end: Int, seed: BigInt, hiding: Boolean = true) extends Component {
    val io = new Bundle {
      val valCounterN = out UInt(log2Up(end + 1) bits)
      val valCounterH = out UInt(log2Up(end + 1) bits)
    }

    val counterHiding = Counter(16) arbitraryOrder() withSeed(seed)
    val counterNormal = Counter(16)

    counterHiding.increment()

    when(counterHiding.willIncrement) {
      counterNormal.increment()
    }
    io.valCounterN := counterNormal.value
    io.valCounterH := counterHiding.value
  }

  def main(args: Array[String]): Unit = {
    val seed = BigInt("110111100000011100000011", 2)
    SimConfig.withWave.compile(new HidingExample(1,8, seed)).doSim{ dut =>
      dut.clockDomain.forkStimulus(10)

      var counterList = ListBuffer[String]()
      var counterHList = ListBuffer[String]()

      for(j <- 0 until 100) {
        dut.clockDomain.waitRisingEdge()

        counterList += dut.io.valCounterN.toBigInt.toString(16)
        counterHList += dut.io.valCounterH.toBigInt.toString(16)
      }

      counterList.foreach(x => print(s"${x} "))
      println("")
      counterHList.foreach(x => print(s"${x} "))
      println("")
    }
  }
}