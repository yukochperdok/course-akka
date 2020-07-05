package com.reactivebbq.orders

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import akka.pattern._

import scala.concurrent.Future

object OrderActor {
  sealed trait Command extends SerializableMessage

  case class OpenOrder(server: Server, table: Table) extends Command
  case class OrderOpened(order: Order) extends SerializableMessage
  case class AddItemToOrder(item: OrderItem) extends Command
  case class ItemAddedToOrder(order: Order) extends SerializableMessage
  case class GetOrder() extends Command

  case class OrderNotFoundException(orderId: OrderId) extends IllegalStateException(s"Order Not Found: $orderId")
  case class DuplicateOrderException(orderId: OrderId) extends IllegalStateException(s"Duplicate Order: $orderId")

  case class Envelope(orderId: OrderId, command: Command) extends SerializableMessage

  val entityIdExtractor: ExtractEntityId = {
    case Envelope(orderId, command) => (orderId.value.toString, command)
  }

  val shardIdExtractor: ExtractShardId = {
    case Envelope(orderId, _) => Math.abs(orderId.value.toString.hashCode % 30).toString
  }

  def props(repository: OrderRepository) = Props(new OrderActor(repository))
}

class OrderActor(repository: OrderRepository) extends Actor with ActorLogging {
  import OrderActor._
  import context.dispatcher

  private val orderId: OrderId = OrderId(UUID.fromString(context.self.path.name))

  override def receive: Receive = {
    case OpenOrder(server, table) =>
      log.info(s"[$orderId] OpenOrder($server, $table)")
      repository
        .find(orderId)
        .flatMap {
          case Some(Order(orderId,_,_,_)) => duplicateOrder(orderId)
          case None => openOrder(orderId, server, table)
        }.pipeTo(sender)

    case AddItemToOrder(item) =>
      log.info(s"[$orderId] AddItemToOrder($item)")
      repository
        .find(orderId)
        .flatMap {
          case Some(order: Order) => addItem(order, item)
          case None => orderNotFound(orderId)
        }.pipeTo(sender)

    case GetOrder() =>
      log.info(s"[$orderId] GetOrder()")
      repository
        .find(orderId)
        .flatMap {
          case Some(order: Order) => Future.successful(order)
          case None => orderNotFound(orderId)
        }.pipeTo(sender)

  }

  private def openOrder(orderId: OrderId, server: Server, table: Table): Future[OrderOpened] = {
    repository
      .update(Order(orderId,server,table, List.empty))
      .map(OrderOpened)
  }

  private def duplicateOrder[T](orderId: OrderId): Future[T] = {
    Future.failed(DuplicateOrderException(orderId))
  }

  private def addItem(order: Order, item: OrderItem): Future[ItemAddedToOrder] = {
    repository
      .update(order.withItem(item))
      .map(ItemAddedToOrder)
  }

  private def orderNotFound[T](orderId: OrderId): Future[T] = {
    Future.failed(OrderNotFoundException(orderId))
  }


}
