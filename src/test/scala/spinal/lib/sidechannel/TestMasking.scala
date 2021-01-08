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

import org.scalatest.FunSuite
import spinal.lib._
import spinal.lib.sidechannel._
import spinal.core._
import spinal.core.sim._

import scala.collection.mutable

case class TestMasking() extends FunSuite {

  test("TestMasking_Simple") {

    class TestMasking_Simple extends Component {

      val io = new Bundle {
        val result = out Bits(8 bits)
      }
      val counter = Counter(4)

     // val areaTest = Reg(new AreaTest()) // does not work!


      //val testnewbits = new TestNewBits()
      //testnewbits := B("001")
      //testnewbits </> B("000")

//      val q8 = Reg(Masked2(Bits(8 bits))) init(B(0), B(0))
//
//      val q9 = Masked2(Bits(8 bits))
//      q9 := (B("00001111"), B("11111110"))
//
//      when(counter.willOverflow) {
//        q8 := q8 ^ q9
//      }
//      counter.increment()

      val q0 = Masked2(Bits(8 bits)) keep()
      val q1 = Masked2(Bits(8 bits)) keep()
      val q2 = Masked2(Bits(8 bits)) keep()

      q0 := (counter.asBits.resize(8), B("01010111"))
      q1 := (B("01101010"), B("11111001"))
      q2 := q0 ^ q1

      val qx = new MaskedN(Bits(8 bits), 2) keep()

      val qa = Masked2(Bits(8 bits)) keep()
      val qb = Masked2(Bits(8 bits)) keep()
      val qc = Masked2(Bits(8 bits)) keep()
      qa := (counter.asBits.resize(8), B("01010111"))
      qb := (B("01101010"), B("11111001"))
      qc := qa & qb


      io.result := qc.asUnmaskedBits
//      io.result := q8.asUnmaskedBits
//      io.result := q0.asUnmaskedBits
      counter.increment()
    }

    val cfg = SimConfig.withConfig(new SpinalConfig(verbose = true))
    cfg.withWave.compile(new TestMasking_Simple()).doSim { dut =>
      dut.clockDomain.forkStimulus(10)

      for (j <- 0 until 50) {
        dut.clockDomain.waitRisingEdge()
      }
    }
  }

  test("TestMasking_Simple_2") {

    class TestMasking_Simple_2 extends Component {

      val io = new Bundle {
        val result = out Bits(8 bits)
      }
      val counter = Counter(4)

      // val areaTest = Reg(new AreaTest()) // does not work!


      //val testnewbits = new TestNewBits()
      //testnewbits := B("001")
      //testnewbits </> B("000")

      //      val q8 = Reg(Masked2(Bits(8 bits))) init(B(0), B(0))
      //
      //      val q9 = Masked2(Bits(8 bits))
      //      q9 := (B("00001111"), B("11111110"))
      //
      //      when(counter.willOverflow) {
      //        q8 := q8 ^ q9
      //      }
      //      counter.increment()

      val q0 = Reg(Masked2(Bits(8 bits))) keep()
      val q1 = Reg(Masked2(Bits(8 bits))) keep()
      val q2 = Reg(Masked2(Bits(8 bits))) keep()

      q0 := (counter.asBits.resize(8), B("01010111"))
      q1 := (B("01101010"), B("11111001"))
      q2 := q0 ^ q1

      val qx = new MaskedN(Bits(8 bits), 2) keep()

      val qa = Masked2(Bits(8 bits))keep()
      val qb = Masked2(Bits(8 bits)) keep()
      val qc = Masked2(Bits(8 bits)) keep()
      val qd = Masked2(Bits(8 bits)) keep()
      qa := (counter.asBits.resize(8), B("01010111"))
      qb := (B("01101010"), B("11111001"))
      qc := qa & qb
      qd := qa ^ qb




      io.result := qc.asUnmaskedBits
      //      io.result := q8.asUnmaskedBits
      //      io.result := q0.asUnmaskedBits
      counter.increment()
    }

    val cfg = SimConfig.withConfig(new SpinalConfig(verbose = true))
    cfg.withWave.compile(new TestMasking_Simple_2()).doSim { dut =>
      dut.clockDomain.forkStimulus(10)

      for (j <- 0 until 50) {
        dut.clockDomain.waitRisingEdge()
      }
    }
  }
}
