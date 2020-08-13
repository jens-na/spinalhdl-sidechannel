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
import spinal.lib.asg.GaloisLFSRConfig
import sun.java2d.loops.XorFillSpansANY

object HideMethods {

  /**
   * The hiding counter which comes with a random shuffle for the values start..end
   *
   * @param start the start value of the counter
   * @param end the end value of the counter
   */
  case class HidingCounter(override val start: BigInt, override val end: BigInt, mode: Int) extends Counter(start, end) {
    val willResetShuffle = False
    val willShuffle = False
    val width = log2Up(end - start + 1)
    val currNum = Reg(UInt(width bits)) init(start)
    val currShufflePop = Counter(end - start + 1)
    val ready = Reg(Bool()) init(False)
    val valueNextInternal = UInt(log2Up(end + 1) bits)

    // Allow statement overlapping due to software overriding the Counter
    val overrides = List[Data](value, valueNext, willOverflowIfInc, willOverflow, willIncrement)
    overrides.foreach(x => x.allowOverride)

    val numbers = Vec(Reg(UInt(width bits)), (end + 1).toInt)
    ((start to end), numbers).zipped.foreach((x,y) => y init(x))
    // reset of regs: numbers(0) = 0, numbers(1) = 1, etc.
    //val mem = Mem(UInt(log2Up(end + 1) bit), end + 1) init((start to end).toSeq.map(x => U(x)))

    /**
     * Shuffle implementation
     * Knuth, Donald E., The Art of Computer Programming, Volume 2: Seminumerical Algorithms, 3d edition
     * Algorithm P (Shuffling)
     */
    val Shuffle = new Area {
      val rnd = Bits(64 bits)
      val rndShift = Bits(width bits)
      val seed = Bits(64 bits) randBoot()

      val prng = new XorShift64()
      prng.io.seed := seed
      rndShift := rnd >> (64 - width)
      rnd := prng.io.rngout

      val tmp = numbers(rndShift.asUInt)
      when(!ready) {
        numbers(rndShift.asUInt) := numbers(currNum)
        numbers(currNum) := tmp
        currNum := currNum + 1
        ready := currNum === (end - 1)

        // Feed to current value. When the last rnd eq 0 the value which get shuffled gets directly set as the next
        // value. Otherwise the first number of the number pool gets forwarded.
        when(currNum === (end - 1)) {
          when(rndShift.asUInt === 0) {
            valueNext := numbers(currNum)
          }.otherwise {
            valueNext := numbers(0)
          }
        }.otherwise {
          valueNext := 0
        }
      }
    }

    val Increment = new Area {
      willOverflowIfInc := currShufflePop === end
      when(willOverflow) {
        currShufflePop := start
      }
      when(ready) {
        valueNext := numbers(currShufflePop.value + 1) // Start at 1, the value 0 gets set in the Shuffle area
      }
      when(willClear) {
        ready := False
        currShufflePop := start
      }
    }

    override def clear(): Unit = {
      willClear := True
    }

    override def increment(): Unit = {
      when(ready){
        currShufflePop.increment()
        willIncrement := True
      }
    }

    def withSeed(seed : Bits): Unit = {
      Shuffle.seed := seed
    }
  }

  implicit class HidingCounterExtension[T <: Counter](val c: T) {

    /**
     * Puts the Counter in the arbitrary order state
     * @return
     */
    def arbitraryOrder(enabled: Boolean = true): Counter = {
      if(!enabled) return c
      require(isPow2(c.end - c.start + 1), "Hiding extension counter must be a power of 2. Requirement: isPow2(end - start)")
      HidingCounter(c.start, c.end, 1)
    }

    /**
     * Sets the seed of the hiding counter. If the counter is no hiding counter, nothing happens.
     *
     * @param seed
     * @return
     */
    def withSeed(seed : Bits): Counter = {
      try {
        val hc = c.asInstanceOf[HidingCounter]
        hc.withSeed(seed)
        hc
      }
      catch {
        case e: ClassCastException => c
      }
    }
  }

}