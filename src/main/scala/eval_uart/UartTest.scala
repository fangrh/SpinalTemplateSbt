package eval_uart

import spinal.core._
import spinal.lib._
import spinal.core.sim._

class UartTest extends Component {
  val io = new Bundle {
    val go = in Bool()
    val data = in UInt(8 bits)
  }
  val uartTx = new EvalUartTx
  val uartRx = new EvalUartRx
  uartTx.io.txd <> uartRx.io.rxd
  uartTx.io.go <> io.go
  uartTx.io.data <> io.data
}

object UartTestSim {
  def main(args: Array[String]): Unit = {
    SimConfig.withWave.doSim(new UartTest) {dut =>
      dut.clockDomain.forkStimulus(20)
      dut.io.go #= false
      sleep(400)
      dut.io.data #= 4
      dut.io.go #= true
      sleep(20)
      dut.io.go #= false
      sleep(500000)
    }
  }
}
