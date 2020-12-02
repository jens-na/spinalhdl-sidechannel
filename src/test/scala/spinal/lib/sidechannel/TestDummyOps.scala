package spinal.lib.sidechannel

import org.scalatest.FunSuite
import spinal.lib._
import spinal.lib.sidechannel._
import spinal.core._
import spinal.core.sim._
import DummyOpsExtension._
import DummyOpsFsm._

case class TestDummyOps() extends FunSuite {

  class TestDummyOps_Simple(totalOps: BigInt, insertPoints: BigInt) extends Component {
    val io = new Bundle {
      val result = out UInt(log2Up(4) bits)
      val seed = in Bits(64 bits)
    }

    val counter = Counter(4)

    val dummycfg = Dummy2(16)
    dummycfg.io.seed.payload := io.seed
    dummycfg.io.seed.valid := True

    val wrap= Dummy2Wrap(dummycfg)
    dummy2(wrap) {
      counter.increment()
    }

    io.result := 0
  }

  case class TestDummyOps_Simple2_Sub(totalOps: BigInt) extends Dummy2Component(totalOps) {
    val io = new Bundle {
      val a = in UInt(4 bits)
      val result = out UInt(4 bits)
    }
    val b = Counter(16)
    b.increment()

    io.result := (io.a + b).resize(4 bits)
  }

  class TestDummyOps_Simple2(totalOps: BigInt) extends Component {
    val io = new Bundle {
      val result = out UInt(4 bits)
      val seed = in Bits(64 bits)
    }


    val dummy2Comp = TestDummyOps_Simple2_Sub(totalOps)
    dummy2Comp.internalIo.seed.payload := io.seed
    dummy2Comp.internalIo.seed.valid := True
    dummy2Comp.io.a := 4

    io.result := dummy2Comp.io.result
  }


  test("TestDummyOps_Simple") {

    val seed1 = BigInt("110111100000011100000011", 2)
    SimConfig.withWave.compile(new TestDummyOps_Simple(16, 2)).doSim { dut =>
      dut.clockDomain.forkStimulus(10)

      dut.io.seed #= seed1
      for (j <- 0 until 100) {
        dut.clockDomain.waitRisingEdge()
      }
    }
  }

  test("TestDummyOps_Simple2") {

    val seed1 = BigInt("110111100000011100000011", 2)
    SimConfig.withWave.compile(new TestDummyOps_Simple2(16)).doSim { dut =>
      dut.clockDomain.forkStimulus(10)

      dut.io.seed #= seed1
      for (j <- 0 until 100) {
        dut.clockDomain.waitRisingEdge()
      }
    }
  }
}

