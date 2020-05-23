package com.lightbend.akkassembly

object Car {
  def apply(unfinishedCar: UnfinishedCar): Car =
    new Car(
      SerialNumber(),
      unfinishedCar.color.get,
      unfinishedCar.engine.get,
      unfinishedCar.wheels,
      unfinishedCar.upgrade,
    )
}

case class Car(serialNumber: SerialNumber, color: Color, engine: Engine, wheels: Seq[Wheel], upgrade: Option[Upgrade]) {
  require(wheels.size == 4)
}
