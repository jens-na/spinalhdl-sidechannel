package spinal.lib.sidechannel

import org.scalatest.FunSuite
import spinal.core.Component
import spinal.lib._
import spinal.core._
import spinal.core.sim._

case class StreamTestSuite() extends FunSuite {

  test("StreamTest_Simple") {

    class StreamTest_Simple extends Component {
      val io = new Bundle {
        val slavePort = slave Stream (Bits(64 bit))
        val masterPort = master Stream (Bits(64 bit))
        val output = out Bits(64 bits)
      }
      val fifo = StreamFifo(Bits(64 bits), 16)
      fifo.io.push << io.slavePort
      fifo.io.pop >/-> io.masterPort

      io.output := 0

      assert(3 == LatencyAnalysis(io.slavePort.payload, io.masterPort.payload))
      assert(2 == LatencyAnalysis(io.masterPort.ready, io.slavePort.ready))
    }

    SimConfig.withWave.compile(new StreamTest_Simple()).doSim { dut =>
      dut.clockDomain.forkStimulus(10)
      dut.clockDomain.waitRisingEdge()

      dut.io.slavePort.valid #= true
      for (j <- 0 until 50) {
        dut.io.slavePort.payload #= j
        dut.clockDomain.waitRisingEdge()
      }

    }
  }
}
