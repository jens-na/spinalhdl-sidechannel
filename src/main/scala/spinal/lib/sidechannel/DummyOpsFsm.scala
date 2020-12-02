
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

object DummyOpsFsm {
  class Dummy2Component(totalOps: BigInt) extends Component {
    import spinal.lib.fsm._

    // Requirements
    require(totalOps >= 2, "totalOps must be >= 2")
    require(isPow2(totalOps), "totalOps must a power of 2")

    // I/O
    val internalIo = new Bundle {
      val seed = slave Stream(Bits(64 bits))
    }

    // Variables/Regs
    val bitWidthRnd = log2Up(totalOps) bits
    val bitWidthSeed = 64 bits
    val prngtemp = Reg(UInt(bitWidthRnd))
    val prngseed = Reg(Bits(bitWidthSeed))
    val n = Reg(UInt(bitWidthRnd)) // rnd
    val k = Reg(UInt(bitWidthRnd)) // totalOps - n

    // FSM
    val internalFsm =  new StateMachine {

      // Obtain random number n
      val obtainRandom: State = new State with EntryPoint {
        val prng = new XorShift64()
        prng.io.seed := prngseed
        when(internalIo.seed.valid) {
          prngseed := internalIo.seed.payload
        }

        whenIsActive {
          prngtemp := (prng.io.rngout >> (64 - log2Up(totalOps))).asUInt
          goto(setRandom)
        }
      }

      // Set random number n and k = totalOps - n
      // If the random number is 0, set n = 15 and k = 0. Probably causes a bias.
      val setRandom: State = new State {
        whenIsActive{
          when(prngtemp === 0) {
            n := totalOps - 1
            k := 0
          }.otherwise {
            n := prngtemp - 1
            k := (totalOps + 1 - prngtemp).resize(log2Up(totalOps))
          }
          goto(dummyOpsBefore)
        }
      }

      // Delay n cycles and afterwards perform the component block
      val dummyOpsBefore : State = new StateDelay(n){
        whenCompleted(goto(performBlock))
      }

      val performBlock : State = new State {
        whenIsActive {
          this
          goto(dummyOpsAfter)
        }
      }

      // Delay k cycles and afterwards go to initial state again
      val dummyOpsAfter : State = new StateDelay(k){
        whenCompleted(goto(obtainRandom))
      }
    }
  }
}
