package Exercises

import akka.actor.{Actor, ActorSystem, Props}

object CounterSystem extends App {

  /**
   * 1 - Create a Counter actor
   *  - Increment
   *  - Decrement
   *  - Print
   */

  val  actorSystem = ActorSystem("CounterActorSystem")

  class CounterActor extends Actor {

    var balance = 0;

    import CounterActor._

    def receive : Receive = {
    case Increment => balance += 1
    case Decrement => balance -= 1
    case Print => println(balance)
    }
  }

  object CounterActor {
    case object Increment
    case object Decrement
    case object Print
  }

  val counterActor = actorSystem.actorOf(Props[CounterActor],"CounterActor")
  import CounterActor._
  counterActor ! Increment
  counterActor ! Print
  counterActor ! Increment
  counterActor ! Print
  counterActor ! Increment
  counterActor ! Print
  counterActor ! Decrement
  counterActor ! Print
}
