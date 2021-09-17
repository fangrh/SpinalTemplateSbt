package eval_spi

import spinal.core._
import spinal.lib._
import spinal.core.sim._

class SpiTop(dataWidth : Int=24,
  ClockDivide : Int = 4,
  CPOL : Boolean=true,
  CPHA : Boolean=false) extends Component {
  val spiIo = new EvalSpiMaster(dataWidth = dataWidth)
  spiIo.asMaster()
  val evalSpiControl = new EvalSpi(dataWidth, ClockDivide, CPOL, CPHA)
  spiIo <> evalSpiControl.io
}

object SpiTopVerilog {
  def main(args: Array[String]): Unit = {
    SpinalVerilog(new SpiTop())
  }
}

object SpiTopSim {
  def main(args: Array[String]): Unit = {
    SimConfig.withWave.doSim(new SpiTop) {dut=>
      dut.clockDomain.forkStimulus(20)
      dut.spiIo.mosiDataPayload #= 11184811
      dut.spiIo.go #= false
      sleep(398)
      dut.spiIo.go #= true
      sleep(40)
      dut.spiIo.go #= false
      sleep(5000)
    }
  }
}