package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object CoffeeHouse {

  case object CreateGuest

  def props: Props = Props(new CoffeeHouse)
}

class CoffeeHouse extends Actor with ActorLogging{
  import CoffeeHouse.CreateGuest

  log.debug("CoffeeHouse Open")

  protected def createGuest: ActorRef = context.actorOf(Guest.props)

  override def receive: Receive = {
    case CreateGuest => createGuest
  }
}
