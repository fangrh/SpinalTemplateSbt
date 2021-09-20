package eval_uart

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._
import spinal.core.sim._

class EvalUartCtrl() extends Component {
  val sampleSize : Int = 1
  var g = new UartCtrlGenerics(dataWidthMax = 8,
    clockDividerWidth = 20,
    preSamplingSize = 1 * sampleSize,
    samplingSize = 5 * sampleSize,
    postSamplingSize = 2 * sampleSize)
  val io = new Bundle {
    val config = in(UartCtrlConfig(g))
    val write  = slave(Stream(Bits(g.dataWidthMax bit)))
    val read   = master(Stream(Bits(g.dataWidthMax bit)))
    val uart   = master(Uart())
  }

  val tx = new UartCtrlTx(g)
  val rx = new UartCtrlRx(g)

  //Clock divider used by RX and TX
  val clockDivider = new Area {
    val counter = Reg(UInt(g.clockDividerWidth bits)) init(0)
    val tick = counter === 0

    counter := counter - 1
    when(tick) {
      counter := io.config.clockDivider
    }
  }

  tx.io.samplingTick := clockDivider.tick
  rx.io.samplingTick := clockDivider.tick

  tx.io.configFrame := io.config.frame
  rx.io.configFrame := io.config.frame

  tx.io.write << io.write
  rx.io.read >> io.read

  io.uart.txd <> tx.io.txd
  io.uart.rxd <> rx.io.rxd
  tx.io.cts := False
  tx.io.break := False
}

class EvalUartCtrlTest extends Component{

}

object EvalUartCtrlSim{
  def main(args: Array[String]): Unit = {
    SimConfig.withWave.doSim(new EvalUartCtrl) {dut=>
      dut.clockDomain.forkStimulus(20)
      sleep(40)
      dut.io.config.frame.dataLength #= 8
      dut.io.config.clockDivider #= 50 * 1000000 / 115200 / 8
      dut.io.write.payload #= 250
      dut.io.uart.txd <> dut.io.uart.rxd
    }
  }
}

object EvalUartCtrlVerilog {
  def main(args: Array[String]): Unit = {
    SpinalConfig(mode = Verilog,
      defaultConfigForClockDomains = ClockDomainConfig(resetKind = ASYNC,
                                                    clockEdge = RISING,
                                                    resetActiveLevel = LOW),
                                                  targetDirectory="../src")
    .generate(new EvalUartCtrl)
  }
}