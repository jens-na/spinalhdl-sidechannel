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
import spinal.lib._
import spinal.core.sim._

object CounterExtensions {

  case class PrngSeed(width: Int) extends Bundle with IMasterSlave {
    val valid = Bool
    val ready = Bool
    val payload = Bits(width bits)

    override def asMaster(): Unit = {
      out(valid, payload)
      in(ready)
    }
  }

  /**
   * Hiding counter with double buffer functionality. The buffer includes two sub counters which change when the other
   * counter overflows. While the other counter is active the other one gets the next random numbers from the PRNG.
   * @param start
   * @param end
   */
  case class HidingCounterDoubleBuffer(override val start: BigInt, override val end: BigInt) extends Counter(start, end) {
    val c1 = HidingCounter(start, end) // active when bufferToggle == false
    val c2 = HidingCounter(start, end) // active when bufferToggle == true
    val bufferToggle = Reg(Bool) init(False)


    val overrides = List[Data](valueNext, willOverflowIfInc, willOverflow, willIncrement, willClear)
    overrides.foreach(x => x.allowOverride)

    val ready = Mux(bufferToggle, c2.ready, c1.ready)
    valueNext := Mux(bufferToggle, c2.valueNext, c1.valueNext)
    willIncrement := Mux(bufferToggle, c2.willIncrement, c1.willIncrement)
    willOverflowIfInc := Mux(bufferToggle, c2.willOverflowIfInc, c1.willOverflowIfInc)
    willClear := Mux(bufferToggle, c2.willClear, c1.willClear)

    // Toggle the buffer
    when(c1.willOverflowIfInc || c2.willOverflowIfInc) { bufferToggle := !bufferToggle}

    override def clear(): Unit = {
      when(bufferToggle) {c2.clear()}
      when(!bufferToggle) {c1.clear()}
    }

    override def increment(): Unit = {
      when(bufferToggle) {c2.increment()}
      when(!bufferToggle) {c1.increment()}
    }
  }

  /**
   * The hiding counter which comes with a random shuffle for the values start..end
   *
   * @param start the start value of the counter
   * @param end the end value of the counter
   */
  case class HidingCounter(override val start: BigInt, override val end: BigInt) extends Counter(start, end) {
    val ready = Reg(Bool()) init(False) simPublic()
    val width = log2Up(end - start + 1)
    val currShuffleInit = Reg(UInt(width bits)) init(start)
    val currShufflePop = Reg(UInt(width bits)) init(start)

    val numbers = Vec(Reg(UInt(width bits)), (end + 1).toInt)
    ((start to end), numbers).zipped.foreach((x,y) => y init(x))

    val overrides = List[Data](valueNext, willOverflowIfInc)
    overrides.foreach(x => x.allowOverride)

    val Seed = new Area {
      val fifo = StreamFifo(Bits(64 bits), 16)
      val popPort = master Stream (Bits(64 bit))

      fifo.io.pop >/-> popPort
      fifo.io.pop.valid := willOverflowIfInc
      // in toplevel:
      // val pushPort = slave Stream (Bits(64 bit))
      // fifo.io.push << io.slavePort

    }

    /**
     * Shuffle implementation
     * Knuth, Donald E., The Art of Computer Programming, Volume 2: Seminumerical Algorithms, 3d edition
     * Algorithm P (Shuffling)
     */
    val Shuffle = new Area {
      val rnd = Bits(64 bits)
      val rndShift = Bits(width bits)
      val prng = new XorShift64()

      prng.io.seed := 0 // TODO
      rndShift := rnd >> (64 - width)
      rnd := prng.io.rngout

      // Swap
      val tmp = numbers(rndShift.asUInt)
      when(!ready) {
        numbers(rndShift.asUInt) := numbers(currShuffleInit)
        numbers(currShuffleInit) := tmp
        currShuffleInit := currShuffleInit + 1

        when(currShuffleInit === (end - 1)) {
          ready := True
        }
      }
    }

    // Increment + direct forward iff the last random would change numbers(0)
    when(currShuffleInit === (end - 1) && Shuffle.rndShift.asUInt === 0) {
      valueNext := numbers(currShuffleInit)
    }.otherwise {
      valueNext := Mux(willIncrement, numbers(currShufflePop + 1), numbers(0))
    }
    willOverflowIfInc := currShufflePop === end

    when(willClear) {
      ready := False
      currShuffleInit := start
      currShufflePop := start
    }

    override def increment(): Unit = {
      when(ready){
        currShufflePop := currShufflePop + 1
        willIncrement := True
      }
    }

  }

  implicit class CounterExtension[T <: Counter](val c: T) {

    /**
     * Puts the Counter in the arbitrary order state
     * @param enabled enable toggle, true default
     * @return the HidingCounter implementation
     */
    def arbitraryOrder(enabled: Boolean = true): Counter = {
      if(!enabled) return c
      require(isPow2(c.end - c.start + 1), "Hiding extension counter must be a power of 2. Requirement: isPow2(end - start)")
      HidingCounter(c.start, c.end)
    }

    /**
     * Puts the Counter in the arbitrary order state with double buffer functionality
     * @param enabled enable toggle, true default
     * @return the HidingCounter implementation
     */
    def arbitraryOrderDoubleBuffer(enabled: Boolean = true): Counter = {
      if(!enabled) return c
      require(isPow2(c.end - c.start + 1), "Hiding extension counter must be a power of 2. Requirement: isPow2(end - start)")
      HidingCounterDoubleBuffer(c.start, c.end)
    }
  }
}