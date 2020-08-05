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
package spinal.lib.sidechannel.blackbox

import spinal.lib.sidechannel.BuildInfo
import spinal.core._

class FYS(m : Int) extends BlackBox {

  val io = new Bundle {
    val clk = in Bool // input wire clk
    val start = in Bool // input wire start

    val seed = in Bits(64 bits) // input wire [63:0] seed
    val INIT_done = out Bool // output wire INIT_done
    val done = out Bool // output wire done
    val rd_en = in Bool //  input wire rd_en,
    val rd_addr_P = in Bits(m bits) //  input wire [m-1:0] rd_addr_P,
    val data_out = out Bits(m bits)//  output wire [m-1:0] data_out
  }
  mapCurrentClockDomain(clock=io.clk, reset =io.start)
  noIoPrefix()

  val rtl = List[String](
    "FYS.v",
    "XorShift64.v",
    "mem_dual.v"
  )
  rtl.foreach(file => addRTLPath(s"${BuildInfo.externalLibs}/fys/${file}"))
}