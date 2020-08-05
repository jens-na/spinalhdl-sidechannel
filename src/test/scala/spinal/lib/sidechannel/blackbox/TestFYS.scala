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
import spinal.lib.sidechannel.blackbox.FYS


object TestFYS {

  class FYSExample(m : Int) extends Component {
    val io = new Bundle {
      val start = in Bool // input wire start
      val seed = in Bits(64 bits) // input wire [63:0] seed
      val INIT_done = out Bool // output wire INIT_done
      val done = out Bool // output wire done
      val rd_en = in Bool //  input wire rd_en,
      val rd_addr_P = in Bits(m bits) //  input wire [m-1:0] rd_addr_P,
      val data_out = out Bits(m bits)//  output wire [m-1:0] data_out
    }

    val fys_inst = new FYS(m)
    fys_inst.io.seed <> io.seed
    fys_inst.io.INIT_done <> io.INIT_done
    fys_inst.io.done <> io.done
    fys_inst.io.rd_en <> io.rd_en
    fys_inst.io.rd_addr_P <> io.rd_addr_P
    fys_inst.io.data_out <> io.data_out
  }

  def main(args: Array[String]): Unit = {

    val tb_seed = BigInt("0011223344556677", 16)
    val tb_addr = BigInt("0000000000000", 2)

    SimConfig.withWave.compile(new FYSExample(13)).doSim{ dut =>
      dut.clockDomain.forkStimulus(10)

      dut.io.seed #= tb_seed
      dut.io.rd_en #= true
      dut.io.rd_addr_P #= tb_addr

      for(j <- 0 until 40000) {
        dut.clockDomain.waitRisingEdge()
      }
    }
  }
}