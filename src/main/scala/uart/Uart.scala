package uart

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._

class UartTop extends Component {
  val MyUartCtrlGenerics = new UartCtrlGenerics( dataWidthMax = 8,
    clockDividerWidth = 20, // !! baudrate = Fclk / (rxSamplePerBit*clockDividerWidth) !!
    preSamplingSize = 1,
    samplingSize = 5,
    postSamplingSize = 2,
    ctsGen  = false,
    rtsGen = false)
  val io = new Bundle {
    val configFrame = in(UartCtrlFrameConfig(MyUartCtrlGenerics))
    val samplingTick = in Bool
    val write = slave Stream (Bits(8 bit))
    val cts = in Bool()
    val txd = out Bool()
    val break = in Bool()
  }
  val data = Stream (Bits(8 bit))
  val MyUartCtrl = new UartCtrlTx(MyUartCtrlGenerics)
  io <> MyUartCtrl.io
}



object UartTopVerilog {
  def main(args: Array[String]): Unit = {
    SpinalConfig(mode = Verilog,
      defaultConfigForClockDomains = ClockDomainConfig(resetKind = ASYNC,
        clockEdge = RISING,
        resetActiveLevel = LOW),
      targetDirectory="../src")
      .generate(new UartTop)
  }
}