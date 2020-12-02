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

import spinal.core.internals.WhenStatement
import spinal.core._
import spinal.lib._
import spinal.core.sim._
import spinal.lib.asg.{GaloisLFSR, GaloisLFSRConfig}
import spinal.lib.sidechannel.DummyOpsExtension.DummyOpsRegLayer

import scala.Predef._


object DummyOpsExtension {

  /**
   * The class which prepares the randomness for the dummy execution.
   *
   * @param totalOps the total amount of dummy operations to perform
   * @param insertPoints the insert points for the dummy operations
   */
  case class DummyOpsRandomConfig(totalOps: BigInt, insertPoints: Int) extends Component {
      val io = new Bundle {
        val seed = slave Stream(Bits(64 bits))
        val pop = master Stream(UInt(log2Up(totalOps - 1) bits))
      }
      val vector = Reg(Vec(UInt(log2Up(totalOps) bits), insertPoints))
      val prngseed = Reg(Bits(64 bits)) init(0)

      def max(v1: UInt, v2: UInt): UInt = Mux(v2 > v1, v2, v1)
      def min(v1: UInt, v2: UInt): UInt = Mux(v2 < v1, v2, v1)

      // PRNG
      val prng = new XorShift64()
      prng.io.seed := prngseed
      when(io.seed.valid) {
        prngseed := io.seed.payload
      }

      val Random = new Area {
        val randomObtained = Reg(Bool) init(False)
        val rndVec = Reg(Vec(UInt(log2Up(totalOps) bits), insertPoints - 1))
        val internalCount = Counter(insertPoints - 1)

        when(!randomObtained) { // Increment
          rndVec(internalCount.value) := (prng.io.rngout >> (64 - log2Up(totalOps))).asUInt
          internalCount.increment()
        }
        when(internalCount.willOverflow) { // Exit cond
          randomObtained := True
        }
      }

      val SortRandom = new Area {
        val randomSorted = Reg(Bool) init(False)
        //val checkDistinctRnd = B(Random.rndVec =/= Random.rndVec.distinct) keep()
        //val checkZeroRnd = B(Random.rndVec(0) === U(0)) keep()

        val j = Counter(insertPoints - 1)
        val k = Counter(insertPoints - 2)
        val swaps = Reg(UInt(log2Up(insertPoints) bits)) init(1) keep()
        val min = Reg(UInt(log2Up(totalOps) bits)) init(totalOps - 1)
        val posIdx = (swaps - 1)
        val pos = Random.rndVec(posIdx)
        val minIndex = Reg(UInt(log2Up(insertPoints) bits)) init(0)
        val swapPerform = RegNext(j.willOverflow)

        // Selection sort O(n^2).
        // 0. rndVec(0) - rndVec(n)
        // 1. rndVec(1) - rndVec(n)
        // ...
        // n-1. rndVec(n-1) - rndVec(n)
        when(Random.randomObtained && !randomSorted) {
          j.increment()
          when(Random.rndVec(j) < min) { // Get minimum
            min := Random.rndVec(j)
            minIndex := j
          }

          when(swapPerform) { // Swap and prepare next iteration
            Random.rndVec(posIdx) := min
            Random.rndVec(minIndex) := pos
            j.value := swaps
            swaps := swaps + 1
            k.increment()
            min := totalOps - 1
          }

          when(k.willOverflow) { // Exit cond
            randomSorted := True
          }
        }
      }

      val CreateVector = new Area {
        val vectorCreated = Reg(Bool) init(False)
        val internalCount = Counter(insertPoints)

        when(SortRandom.randomSorted && !vectorCreated) {
            when(internalCount === 0) {
              vector(internalCount) := (Random.rndVec(internalCount) - 1)
            }.elsewhen(internalCount === insertPoints - 1) {
              vector(internalCount) := ((totalOps + insertPoints - 1) - Random.rndVec(internalCount)).resize(log2Up(totalOps))
            }.otherwise {
              vector(internalCount) := (Random.rndVec(internalCount) - Random.rndVec(internalCount - 1) - 1)
            }

            internalCount.increment()

            when(internalCount.willOverflow) { // Exit cond
              vectorCreated := True
            }
        }
      }

      val Output = new Area {
        val empty = Reg(Bool) init(False)
        val internalCounter = Counter(insertPoints)
        val hasNext = !empty

        when(internalCounter.willOverflowIfInc) {
          empty := True
        }
        when(!empty && io.pop.ready && CreateVector.vectorCreated) {
          internalCounter.increment()
        }
        when(io.seed.valid && !Random.randomObtained) {
          prngseed := io.seed.payload
        }

        io.pop.valid := CreateVector.vectorCreated && hasNext
        io.pop.payload := vector(internalCounter)
      }
    }

  case class DummyOpsRegLayer(rnd: DummyOpsRandomConfig) extends Area {
      val regBefore = Reg(UInt(log2Up(rnd.totalOps - 1) bits)) init(0) keep()
      val regAfter = Reg(UInt(log2Up(rnd.totalOps - 1) bits)) init(0) keep()
      val regBlock = Reg(Bool) init(False) keep()
    }

  object dummyN {
    def apply(layer: DummyOpsRegLayer)(block: => Unit): Unit = {
      val regSetCount = Counter(2)
      val regSet = Reg(Bool) init(False) keep() simPublic()
      val beforeTotal = Reg(UInt(log2Up(layer.rnd.totalOps - 1) bits)) init(layer.rnd.totalOps -1) keep() simPublic()
      val afterTotal = Reg(UInt(log2Up(layer.rnd.totalOps -1) bits)) init(layer.rnd.totalOps -1) keep() simPublic()
      layer.rnd.io.pop.ready := False

      // Load before und after dummy executions (2 cycles)
      when(!regSet && regSetCount === 0 && layer.rnd.io.pop.valid) {
        layer.rnd.io.pop.ready := True
        beforeTotal := layer.rnd.io.pop.payload
        regSetCount.increment()
      }.elsewhen(!regSet && regSetCount === 1 && layer.rnd.io.pop.valid) {
        afterTotal := layer.rnd.io.pop.payload
        layer.rnd.io.pop.ready := True
        regSet := True
      }

      when(regSet) {
        when(layer.regBefore < beforeTotal) {
          layer.regBefore := layer.regBefore + 1
        }

        when(layer.regBefore === beforeTotal && !layer.regBlock) {
          block
          layer.regBlock := True
        }

        when(layer.regBlock && layer.regAfter < afterTotal) {
          layer.regAfter := layer.regAfter + 1
        }

        // Reset
        when(layer.regAfter === afterTotal) {
          layer.regAfter := 0
          layer.regBlock := False
          layer.regBefore := 0
        }
      }
    }
  }

  case class Dummy2(totalOps: BigInt) extends Component {
    val rndBitWidth = log2Up(totalOps) bits
    val seedBitWidth = 64 bits

    val io = new Bundle {
      val seed = slave Stream(Bits(seedBitWidth))
      val rnd =  out Vec(UInt(rndBitWidth), 2)
    }

    val PRNG = new Area {
      val prng = new XorShift64()
      val prngseed = Reg(Bits(seedBitWidth))
      val prngtemp = Reg(UInt(rndBitWidth))

      prng.io.seed := prngseed
      prngtemp := (prng.io.rngout >> (64 - log2Up(totalOps))).asUInt

      when(io.seed.valid) {
        prngseed := io.seed.payload
      }
    }

    val Vectors = new Area {
      when(PRNG.prngtemp === 0) {
        io.rnd(0) := totalOps - 1
        io.rnd(1) := 0
      }.otherwise {
        io.rnd(0) := PRNG.prngtemp - 1
        io.rnd(1) := (totalOps + 1 - PRNG.prngtemp).resize(log2Up(totalOps))
      }
    }

    val Output = new Area {
      when(io.seed.valid) {
        PRNG.prngseed := io.seed.payload
      }
    }
  }

  case class Dummy2Wrap(cfg: Dummy2) extends Area {
    val initialized = Reg(Bool) init(False)
    val beforeBlock = RegNextWhen(cfg.io.rnd(0), !initialized)
    val afterBlock = RegNextWhen(cfg.io.rnd(1), !initialized)
    val zzzbefore = Reg(UInt(cfg.rndBitWidth)) init(0) keep()
    val zzzafter = Reg(UInt(cfg.rndBitWidth)) init(0) keep()
    val zzzblock = Reg(Bool) init(False) keep()
  }
  object dummy2 {
    def apply(l: Dummy2Wrap)(block: => Unit): Unit = {
      l.initialized := True

      // Before
      when(l.zzzbefore < l.beforeBlock) {
        l.zzzbefore := l.zzzbefore + 1
      }

      // Block
      when(l.zzzbefore === l.beforeBlock && !l.zzzblock) {
        block
        l.zzzblock := True
      }

      // After
      when(l.zzzblock && l.zzzafter < l.afterBlock) {
        l.zzzafter := l.zzzafter + 1
      }

      // Reset
      when(l.zzzafter === l.afterBlock) {
        l.zzzafter:= 0
        l.zzzblock := False
        l.zzzbefore := 0
      }
    }
  }



  case class Dummy2OpsCmd(totalOps: BigInt) extends Bundle {
    val seed = Bits(64 bits)
    val activator = Vec(Bool, 2)
  }
  case class Dummy2OpsRsp(totalOps: BigInt) extends Bundle {
    val rnd = Vec(Bits(log2Up(totalOps - 1) bits), 2)
  }
  case class DummyOpsRndIO(totalOps: BigInt) extends Bundle {
    val cmd = master Stream(Dummy2OpsCmd(totalOps))
    val rsp = slave Flow (Dummy2OpsRsp(totalOps))
  }
  case class DummyOpsRnd(totalOps: BigInt) extends Bundle {

  }


}

