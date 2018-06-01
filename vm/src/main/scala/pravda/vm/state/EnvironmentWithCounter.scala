package pravda.vm.state

import pravda.common.domain.{Address, NativeCoin}
import pravda.vm.watt.WattCounter
import pravda.vm.watt.WattCounter.CpuStorageUse

class EnvironmentWithCounter(environment: Environment, wattCounter: WattCounter) extends Environment {

  def updateProgram(address: Address, code: Data): Data = {
    wattCounter.cpuUsage(CpuStorageUse)
    wattCounter.storageUsage(occupiedBytes = code.size())

    val previous = environment.updateProgram(address, code)
    wattCounter.storageUsage(releasedBytes = previous.size())
    previous
  }

  def createProgram(owner: Address, code: Data): Address = {
    wattCounter.cpuUsage(CpuStorageUse)
    wattCounter.storageUsage(occupiedBytes = code.size())

    environment.createProgram(owner, code)
  }

  def getProgram(address: Address): Option[ProgramContext] = {
    wattCounter.cpuUsage(CpuStorageUse)

    environment.getProgram(address)
  }

  def getProgramOwner(address: Address): Option[Address] = {
    wattCounter.cpuUsage(CpuStorageUse)

    environment.getProgramOwner(address)
  }

  def transfer(from: Address, to: Address, amount: NativeCoin): Unit = {
    wattCounter.cpuUsage(CpuStorageUse)

    environment.transfer(from, to, amount)
  }

  def balance(address: Address): NativeCoin = {
    wattCounter.cpuUsage(CpuStorageUse)

    environment.balance(address)
  }

  def withdraw(address: Address, amount: NativeCoin): Unit = {
    wattCounter.cpuUsage(CpuStorageUse)

    environment.withdraw(address, amount)
  }

  def accrue(address: Address, amount: NativeCoin): Unit = {
    wattCounter.cpuUsage(CpuStorageUse)

    environment.accrue(address, amount)
  }

}
