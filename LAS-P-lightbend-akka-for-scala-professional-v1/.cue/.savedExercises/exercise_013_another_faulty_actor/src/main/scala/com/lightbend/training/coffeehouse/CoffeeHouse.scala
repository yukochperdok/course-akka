package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated}

import scala.concurrent.duration._

object CoffeeHouse {

  case class CreateGuest(favouriteCoffee: Coffee, guestCaffeineLimit: Int)
  case class ApproveCoffee(coffee: Coffee, guest: ActorRef)

  def props(caffeineLimit: Int): Props = Props(new CoffeeHouse(caffeineLimit))
}

class CoffeeHouse(caffeineLimit: Int) extends Actor with ActorLogging{
  import CoffeeHouse._

  log.debug("CoffeeHouse Open")


  override def supervisorStrategy: SupervisorStrategy = {
    val decider: SupervisorStrategy.Decider = {
      case Guest.CaffeineException => SupervisorStrategy.Stop
    }

    OneForOneStrategy()(decider.orElse(super.supervisorStrategy.decider))
  }

  private var guestBook: Map[ActorRef, Int] = Map.empty.withDefaultValue(0)

  val baristaAccuracy: Int =
    context.system.settings.config.getInt("coffee-house.barista.accuracy")

  val waiterMaxComplaintCount: Int =
    context.system.settings.config.getInt("coffee-house.waiter.max-complaint-count")

  val prepareCoffeeDuration: FiniteDuration =
    context.system.settings.config.getDuration("coffee-house.barista.prepare-coffee-duration", MILLISECONDS).millis

  val finishCoffeeDuration: FiniteDuration =
    context.system.settings.config.getDuration("coffee-house.guest.finish-coffee-duration", MILLISECONDS).millis

  private val barista = createBarista
  protected def createBarista: ActorRef =
    context.actorOf(Barista.props(prepareCoffeeDuration, baristaAccuracy), "barista")

  private val waiter = createWaiter
  protected def createWaiter: ActorRef =
    context.actorOf(Waiter.props(self, barista, waiterMaxComplaintCount), "waiter")

  protected def createGuest(favouriteCoffee: Coffee, guestCaffeineLimit: Int): ActorRef =
    context.actorOf(Guest.props(waiter, favouriteCoffee, finishCoffeeDuration, guestCaffeineLimit))

  override def receive: Receive = {
    case CreateGuest(coffee, guestCaffeineLimit) =>
      val guest = createGuest(coffee, guestCaffeineLimit)
      guestBook += guest -> 0
      log.info(s"Guest ${guest} added to guest book")
      context.watch(guest)

    case ApproveCoffee(coffee, guest) if guestBook(guest) < caffeineLimit =>
      guestBook += guest -> (guestBook(guest) + 1)
      log.info(s"Guest $guest caffeine count incremented")
      barista.forward(Barista.PrepareCoffee(coffee, guest))

    case ApproveCoffee(_, guest) =>
      log.info(s"Sorry, $guest, but you have reached your limit")
      context.stop(guest)

    case Terminated(guest) =>
      guestBook -= guest
      log.info(s"Thanks $guest for being our guest")
  }
}
