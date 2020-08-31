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

import spinal.core.{assert, _}
import spinal.core.sim._
import spinal.lib._
import spinal.lib.sidechannel.HideMethods._
import org.scalatest.FunSuite

import scala.collection.mutable.ListBuffer


/**
 * Example Test componenent to test the PseudoRandom object
 */

case class HidingTestSuite() extends FunSuite {

  test("HidingCounter_16_Single") {

    // Example Component
    class HidingCounter_16_Single(counterLength: Int) extends Component {
      val io = new Bundle {
        val value = out UInt (log2Up(counterLength) bits)
        val seed = in Bits (64 bits)
      }

      val counter = Counter(counterLength) arbitraryOrder()
      counter.asInstanceOf[HidingCounter].Shuffle.seed := io.seed // Set a seed from a TRNG for example

      when(counter.willOverflow) {
        counter.clear()
      }

      counter.increment()
      io.value := counter.value
    }

    // Variables
    val seed = BigInt("110111100000011100000011", 2)
    val counterLength = 16
    val expected = (0 to counterLength - 1).map(x => BigInt(x)).toList
    var actual = ListBuffer[BigInt]()

    // Simulation
    SimConfig.withWave.compile(new HidingCounter_16_Single(16)).doSim { dut =>
      dut.clockDomain.forkStimulus(10)
      dut.clockDomain.waitRisingEdge()

      dut.io.seed #= seed // Static seed for testing
      val counterImpl = dut.counter.asInstanceOf[HidingCounter]

      // Wait until seeded
      waitUntil(counterImpl.ready.toBoolean == true)
      dut.clockDomain.waitRisingEdge()

      // Check the counterLength values
      for (j <- 0 until counterLength) {
        println(s"value=${dut.io.value.toBigInt.toString(16)}, ready=${counterImpl.ready.toBoolean}")
        actual += dut.io.value.toBigInt
        dut.clockDomain.waitRisingEdge()
      }

      // Assert
      spinal.core.assert(actual.length == counterLength,
        s"Counter length incorrect. Must be '${counterLength.toString}', but is '${actual.length}''")

      for (n <- expected) {
        spinal.core.assert(actual.contains(n), s"Value '${n.toString(16)}' not in the actual values list: [${actual.toString()}]")
      }
    }
  }

  test("HidingCounter_16_Double") {

    // Example Component
    class HidingCounter_16_Double(counterLength: Int) extends Component {
      val io = new Bundle {
        val value = out UInt (log2Up(counterLength) bits)
        val seed1 = in Bits (64 bits)
        val seed2 = in Bits (64 bits)
      }

      val counter = Counter(counterLength) arbitraryOrderDoubleBuffer()
      counter.asInstanceOf[HidingCounterDoubleBuffer].c1.Shuffle.seed := io.seed1 // Set a seed from a TRNG for example
      counter.asInstanceOf[HidingCounterDoubleBuffer].c2.Shuffle.seed := io.seed2

      when(counter.willOverflow) {
        counter.clear()
      }

      counter.increment()
      io.value := counter.value
    }

    // Variables
    val seed1 = BigInt("110111100000011100000011", 2)
    val seed2 = BigInt("11000110111001110000001", 2)
    val counterLength = 16
    val expected = (0 to counterLength - 1).map(x => BigInt(x)).toList
    var actual = ListBuffer[BigInt]()

    // Simulation
    SimConfig.withWave.compile(new HidingCounter_16_Double(16)).doSim { dut =>
      dut.clockDomain.forkStimulus(10)
      dut.clockDomain.waitRisingEdge()

      dut.io.seed1 #= seed1 // Static seed for testing
      dut.io.seed2 #= seed1 // Static seed for testing
      val counterImpl = dut.counter.asInstanceOf[HidingCounterDoubleBuffer]

      // Wait until seeded
      waitUntil(counterImpl.c1.ready.toBoolean == true)
      dut.clockDomain.waitRisingEdge()

      // Check the counterLength values
      for (j <- 0 until counterLength) {
        println(s"counter=c1, value=${dut.io.value.toBigInt.toString(16)}, ready=${counterImpl.c1.ready.toBoolean}")
        actual += dut.io.value.toBigInt
        dut.clockDomain.waitRisingEdge()
      }

      // Assert
      spinal.core.assert(actual.length == counterLength,
        s"Counter length incorrect. Must be '${counterLength.toString}', but is '${actual.length}''")

      for (n <- expected) {
        spinal.core.assert(actual.contains(n), s"Value '${n.toString(16)}' not in the actual values list: [${actual.toString()}]")
      }

      // Wait until seeded
      waitUntil(counterImpl.c2.ready.toBoolean == true)
      actual.clear()
      dut.clockDomain.waitRisingEdge()

      // Check the counterLength values
      for (j <- 0 until counterLength) {
        println(s"counter=c2, value=${dut.io.value.toBigInt.toString(16)}, ready=${counterImpl.c2.ready.toBoolean}")
        actual += dut.io.value.toBigInt
        dut.clockDomain.waitRisingEdge()
      }

      // Assert
      spinal.core.assert(actual.length == counterLength,
        s"Counter length incorrect. Must be '${counterLength.toString}', but is '${actual.length}''")

      for (n <- expected) {
        spinal.core.assert(actual.contains(n), s"Value '${n.toString(16)}' not in the actual values list: [${actual.toString()}]")
      }
    }
  }
}