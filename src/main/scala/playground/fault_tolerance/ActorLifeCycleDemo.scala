package playground.fault_tolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import playground.fault_tolerance.ActorLifeCycleDemo.SimpleActor.{Fail, FailChild}
import playground.fault_tolerance.StartingStoppingActorsDemo.StartChild

object ActorLifeCycleDemo extends App{

  object SimpleActor {
    case class StartChild(name: String)
    case class FailChild(name: String)
    case object Fail
  }
  class SimpleActor extends Actor with ActorLogging {
    var children: Map[String, ActorRef] = Map()
    override def receive: Receive = {
      case StartChild(name) => {
        log.info(s"Starting child with name $name")
        val child = context.actorOf(Props[SimpleActor], name)
        children = children + (name -> child)
      }
      case FailChild(name) => {
        val child = children(name)
        child ! Fail
      }
      case Fail => throw new RuntimeException(s"throwing runtime exception!!")
      case message => log.info(s" received $message")
    }

    override def preStart(): Unit = {
      log.info("called PreStart")
    }

    override def postStop(): Unit = {
      log.info("called PostStop")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.info(s"Restarting due to $reason and message is $message")
    }

    override def postRestart(reason: Throwable): Unit = {
      log.info(s"Restart done due  to $reason")
    }
  }

  val system = ActorSystem("ActorSystem")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "checking simple actor"
  simpleActor ! StartChild("child1")
  val child1 = system.actorSelection("/user/simpleActor/child1")
  simpleActor ! FailChild("child1")
  child1 ! "checking child1"
  Thread.sleep(5000)
  simpleActor ! PoisonPill
}
