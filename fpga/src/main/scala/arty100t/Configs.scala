// See LICENSE for license details.
package chipyard.fpga.arty100t

import org.chipsalliance.cde.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.system._
import freechips.rocketchip.tile._

import sifive.blocks.devices.uart._
import sifive.fpgashells.shell.{DesignKey}

import testchipip.serdes.{SerialTLKey}

import chipyard.{BuildSystem}

// don't use FPGAShell's DesignKey
class WithNoDesignKey extends Config((site, here, up) => {
  case DesignKey => (p: Parameters) => new SimpleLazyModule()(p)
})

// By default, this uses the on-board USB-UART for the TSI-over-UART link
// The PMODUART HarnessBinder maps the actual UART device to JD pin
class WithArty100TTweaks(freqMHz: Double = 50) extends Config(
  new WithArty100TPMODUART ++
  new WithArty100TUARTTSI ++
  new WithArty100TDDRTL ++
  new WithArty100TJTAG ++
  new WithNoDesignKey ++
  new testchipip.tsi.WithUARTTSIClient ++
  new chipyard.harness.WithSerialTLTiedOff ++
  new chipyard.harness.WithHarnessBinderClockFreqMHz(freqMHz) ++
  new chipyard.config.WithMemoryBusFrequency(freqMHz) ++
  new chipyard.config.WithFrontBusFrequency(freqMHz) ++
  new chipyard.config.WithSystemBusFrequency(freqMHz) ++
  new chipyard.config.WithPeripheryBusFrequency(freqMHz) ++
  new chipyard.config.WithControlBusFrequency(freqMHz) ++
  new chipyard.config.WithOffchipBusFrequency(freqMHz) ++
  new chipyard.harness.WithAllClocksFromHarnessClockInstantiator ++
  new chipyard.clocking.WithPassthroughClockGenerator ++
  new chipyard.config.WithTLBackingMemory ++ // FPGA-shells converts the AXI to TL for us
  new freechips.rocketchip.subsystem.WithExtMemSize(BigInt(256) << 20) ++ // 256mb on ARTY
  new freechips.rocketchip.subsystem.WithoutTLMonitors)

class RocketArty100TConfig extends Config(
  new WithArty100TTweaks ++
  new testchipip.soc.WithMbusScratchpad(base = 0x08000000, size = 128 * 1024) ++ // use rocket l1 DCache scratchpad as base phys mem
  new chipyard.config.WithBroadcastManager ++ // no l2
  new chipyard.RocketConfig)

class NoCoresArty100TConfig extends Config(
  new WithArty100TTweaks ++
  new chipyard.config.WithBroadcastManager ++ // no l2
  new chipyard.NoCoresConfig)

// This will fail to close timing above 50 MHz
class BringupArty100TConfig extends Config(
  new WithArty100TSerialTLToGPIO ++
  new WithArty100TTweaks(freqMHz = 50) ++
  new testchipip.serdes.WithSerialTLClockDirection(provideClockFreqMHz = Some(50)) ++
  new chipyard.ChipBringupHostConfig)
