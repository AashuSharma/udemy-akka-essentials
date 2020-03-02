package Exercises

import akka.actor.{Actor, ActorSystem, Props}

object StatelessCounterSystem extends App{

  val system = ActorSystem("StatelessCounterSystem")

  object StatelessCounterActor {
    case object Increment
    case object Decrement
    case object Display
  }
  class StatelessCounterActor extends Actor {

    import StatelessCounterActor._
    override def receive: Receive = counterReceive(0)

    def counterReceive(count:Int):Receive = {
      case Increment => context.become(counterReceive(count+1))
      case Decrement => context.become(counterReceive(count-1))
      case Display =>  println(count)
    }
  }

  val statelessCounterActor = system.actorOf(Props[StatelessCounterActor])

  import StatelessCounterActor._
  statelessCounterActor ! Increment
  statelessCounterActor ! Increment
  statelessCounterActor ! Increment
  statelessCounterActor ! Decrement
  statelessCounterActor ! Display
}
