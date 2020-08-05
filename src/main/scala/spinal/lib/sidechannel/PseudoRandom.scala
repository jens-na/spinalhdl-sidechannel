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
import spinal.lib.asg.{GaloisASG, GaloisLFSRConfig}


case class PseudoRandomConfig(seedWidth : Int, rndWidth : Int)

case class PseudoRandomCmd(seedWidth : Int) extends Bundle {
  val seed = Bits(seedWidth bits)
}

case class PseudoRandomRsp(rndWidth : Int) extends Bundle {
  val rnd = Bits(rndWidth bits)
}

case class PseudoRandomIO(config: PseudoRandomConfig) extends Bundle with IMasterSlave {
  val cmd = Stream(PseudoRandomCmd(config.seedWidth))
  val rsp = Stream(PseudoRandomRsp(config.rndWidth))

  override def asMaster() = {
    master(cmd)
    slave(rsp)
  }
}

case class PseudoRandomImpl(lfsrConfigs: List[GaloisLFSRConfig]) extends Component {
  val config = PseudoRandomConfig(
    lfsrConfigs.map(x => x.width).sum,
    lfsrConfigs(1).width
  )
  val io = slave(PseudoRandomIO(config)) simPublic()
  val engine = new GaloisLfsrAsgPRNG(lfsrConfigs)
  engine.io.engine <> io
}

case class GaloisLfsrAsgPRNG(lfsrConfigs: List[GaloisLFSRConfig]) extends Component {
  val config = PseudoRandomConfig(
    lfsrConfigs.map(x => x.width).sum,
    lfsrConfigs(1).width
  )
  val io = new Bundle {
    val engine = slave(PseudoRandomIO(config))
  }

  val seed = Reg(Bits(config.seedWidth bits))
  val asg = new GaloisASG(lfsrConfigs)

  when(io.engine.cmd.valid) { // new seed
    seed := io.engine.cmd.seed
  }
  io.engine.cmd.ready := True
  io.engine.rsp.rnd := asg.io.rnd

  asg.io.enable := io.engine.rsp.ready // Only new rnd, when sink is ready for it
  asg.io.seed := seed
}