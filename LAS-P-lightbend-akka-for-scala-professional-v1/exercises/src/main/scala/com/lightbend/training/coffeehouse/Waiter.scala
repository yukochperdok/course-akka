package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object Waiter {
  case class ServeCoffee(coffee: Coffee)
  case class CoffeeServed(coffee: Coffee)
  case class Complaint(coffee: Coffee)
  case class FrustratedException(coffee: Coffee, guest:ActorRef) extends IllegalStateException("Too many complaints.")

  def props(coffeeHouse: ActorRef, barista: ActorRef, maxComplaintCount:Int): Props =
    Props(new Waiter(coffeeHouse, barista, maxComplaintCount))
}

class Waiter(
  coffeeHouse: ActorRef,
  barista: ActorRef,
  maxComplaintCount: Int) extends Actor with ActorLogging{
  import Waiter._

  private var complaintCount = 0

  override def receive: Receive = {
    case ServeCoffee(coffee) =>
      coffeeHouse ! CoffeeHouse.ApproveCoffee(coffee, sender)

    case Barista.CoffeePrepared(coffee, guest) =>
      guest ! CoffeeServed(coffee)

    case Complaint(correctCoffee) if complaintCount >= maxComplaintCount =>
      // Make control in parent's supervisor
      throw FrustratedException(correctCoffee, sender)

    case Complaint(correctCoffee) =>
      complaintCount += 1
      barista ! Barista.PrepareCoffee(correctCoffee, sender)
  }
}
