package eval_spi

import spinal.core._
import spinal.lib._

class SpiTest extends Component {
  val spiIo = new EvalSpiMaster(dataWidth = 24)
  val spiIo_go_n = in Bool()
//  spiIo_go_n.init(True)
  spiIo.go := (!spiIo_go_n).rise()
  spiIo.done.asOutput()
  spiIo.mosi.asOutput()
  spiIo.sclk.asOutput()
  spiIo.ss.asOutput()
  val spiCtrl = new EvalSpi(dataWidth = 24, ClockDivide = 16, CPOL = true, CPHA = true)
  spiIo.mosiDataPayload := 0xF0A854
  spiIo <> spiCtrl.io
}


object SpiTestVerilog {
  def main(args: Array[String]): Unit = {
    SpinalConfig(mode = Verilog,
      defaultConfigForClockDomains = ClockDomainConfig(resetKind = ASYNC,
                                                    clockEdge = RISING,
                                                    resetActiveLevel = LOW),
                                                  targetDirectory="../src")
                                                    .generate(new SpiTest)
  }
}

object SpiTestSim {
  def main(args: Array[String]): Unit = {

  }
}