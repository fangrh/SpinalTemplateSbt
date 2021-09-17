package eval_spi

import spinal.core._
import spinal.lib._
import com.spi._
import spinal.lib.fsm._

class EvalSpiMaster(dataWidth : Int) extends SpiMaster{
  val go = Bool()
  val done = Bool()
  val mosiDataPayload = Bits(dataWidth bits)  // 输送数据
  val misoDataPayload = Reg(Bits(dataWidth bits))  // 接收数据

  override def asMaster(): Unit = {
    out(ss, sclk, mosi, done, misoDataPayload)
    in(go, miso, mosiDataPayload)
  }
}

class EvalSpi(dataWidth : Int=24,
              ClockDivide : Int = 4,
              CPOL : Boolean=true,
              CPHA : Boolean=false) extends Component{
  val io = new EvalSpiMaster(dataWidth=dataWidth)
  io.asMaster()
  io.misoDataPayload.setAll()
//  val clk,rst_n = in Bool()
//  val myClockDomain = ClockDomain(clk, rst_n)
//  new ClockingArea(myClockDomain) {
    val sclk = Reg(Bool())
    val ss = Reg(Bits(1 bits))
    val mosi = Reg(Bool())
    val done = Reg(Bool())


    val fsm = new StateMachine {
      val IDLE = new State with EntryPoint
      val LowSs = new State
      val SendClk = new State

      val counter = Reg(UInt(8 bits)) init (0)
      val sclkCounter = Reg(UInt(8 bits)) init (0)
      val dataCounter = Reg(UInt(5 bits)) init (0)

      //    val go_delay = Reg(Bool())
      //    go_delay.init(False)
      //    val go_timeout = new Timeout(3)
      //    when(io.go.rise(False)){
      //      go_delay.set()
      //    }

      if (CPOL)
        sclk.init(True)
      else
        sclk.init(False)
      ss.init(B"1'b1")
      mosi.init(False)
      done.init(False)

      IDLE
        .onEntry(counter := 0)
        .whenIsActive {
          when(!(counter === ClockDivide+6)){
          counter := counter + 1}
          mosi.clear()
          if (CPOL)
            sclk.set()
          else
            sclk.clear()
          when(counter === ClockDivide / 4) {
            ss.setAll()
          }
          done := (counter === ClockDivide / 4 + 2) | (counter === ClockDivide / 4 + 3) | (counter === ClockDivide / 4 + 4) | (counter === ClockDivide / 4 + 5)
          when(io.go & ss(0)) {
            goto(LowSs)
          }
        }
      LowSs
        .onEntry(counter := 0)
        .whenIsActive {
          counter := counter + 1
          when(counter === ClockDivide / 2 - 1) {
            ss.clearAll()
          }.elsewhen(counter === ClockDivide - 1) {
            goto(SendClk)
          }
        }

      SendClk
        .onEntry {
          sclkCounter := 0
          if (CPHA) {
            mosi := Reverse(io.mosiDataPayload).asBits(dataCounter)
            dataCounter := 1
          } else {
            dataCounter := 0
          }
        }
        .whenIsActive {
          sclkCounter := sclkCounter + 1
          when(sclkCounter === ClockDivide / 4 - 1) {

          }.elsewhen(sclkCounter === ClockDivide / 2 - 1) {
            sclk := !sclk
            if (!CPHA) {
              mosi := Reverse(io.mosiDataPayload).asBits(dataCounter)
              dataCounter := dataCounter + 1
            }
          }.elsewhen(sclkCounter === ClockDivide / 4 * 3 - 1) {

          }.elsewhen(sclkCounter === ClockDivide - 1) {
            sclk := !sclk
            sclkCounter := 0
            if (CPHA) {
              mosi := Reverse(io.mosiDataPayload).asBits(dataCounter)
              dataCounter := dataCounter + 1
            }
            //when ((dataCounter === dataWidth + 1) & Bool(CPOL) | (dataCounter === dataWidth) & !Bool(CPOL))
            when(dataCounter === dataWidth) {
              goto(IDLE)
            }
          }
        }
    }
    io.sclk := sclk
    io.ss := ss
    io.mosi := mosi
    io.done := done
//  }
}

object EvalSpiVerilog{
  def main(args: Array[String]): Unit = {
    SpinalConfig(mode = Verilog,
      defaultConfigForClockDomains = ClockDomainConfig(resetKind = ASYNC,
                                                        clockEdge = RISING,
                                                        resetActiveLevel = LOW),
                                                        targetDirectory="../src")
      .generate(new EvalSpi(24, ClockDivide = 4, CPOL = true, CPHA = true))
  }
}