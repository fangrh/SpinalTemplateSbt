package eval_spi

import spinal.core._
import spinal.sim._
import spinal.core.sim._
import math._


object EvalSpiSim {
  def main(args: Array[String]): Unit = {
    SimConfig.withWave.doSim(new EvalSpi(24, ClockDivide = 4, CPOL=false, CPHA=false)){dut=>
      dut.clockDomain.forkStimulus(period = 20)
      dut.io.mosiDataPayload #= 11184811
      dut.io.go #= false
      sleep(400)
      dut.io.go #= true
      sleep(40)
      dut.io.go #= false
      sleep(5000)
    }
  }
}
