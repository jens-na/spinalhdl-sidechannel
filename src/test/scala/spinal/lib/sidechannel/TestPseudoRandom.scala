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
import spinal.sim._
import spinal.core.sim._
import spinal.lib.asg.GaloisLFSRConfig
import spinal.lib.sidechannel.HideMethods.HidingCounter
import spinal.core.sim._
import spinal.lib.Counter


object TestPseudoRandom {

  /**
   * Example Test componenent to test the PseudoRandom object
   * @param start Count from
   * @param end Count to
   */
  class PseudoRandomExample(start: Int, end: Int) extends Component {
    val configP = new GaloisLFSRConfig(
      width = 8,
      poly = List(8,1)
    )
    val configQ = new GaloisLFSRConfig(
      width = 8,
      poly = List(8,1)
    )
    val configK = new GaloisLFSRConfig(
      width = 8,
      poly = List(8,1)
    )
    val configs = List(configK, configP, configQ)
    val prng = PseudoRandomImpl(configs)
  }

  def main(args: Array[String]): Unit = {
    SimConfig.withWave.compile(new PseudoRandomExample(1,8)).doSim{ dut =>
      dut.clockDomain.forkStimulus(10)
      dut.prng.io.cmd.seed #= BigInt("000000100000001000000010", 2)
      dut.prng.io.cmd.valid #= true

      dut.prng.io.rsp.ready #= true
      for(j <- 0 until 50) {
        dut.clockDomain.waitRisingEdge()
      }
      dut.prng.io.rsp.ready #= false
      for(j <- 0 until 10) {
        dut.clockDomain.waitRisingEdge()
      }

      dut.prng.io.rsp.ready #= true
      for(j <- 0 until 50) {
        dut.clockDomain.waitRisingEdge()
      }
    }
  }
}