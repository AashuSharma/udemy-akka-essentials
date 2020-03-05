package playground.fault_tolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated}

object StartingStoppingActorsDemo extends App{

  /**
   * Stopping Actors
   */
  val system = ActorSystem("StoppingActorsDemo")
  case class StartChild(name: String)
  case class StopChild(name: String)
  case object Stop

  class Parent extends Actor with ActorLogging{
    override def postStop(): Unit = log.info(s"post Stop $self")
    override def receive: Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) => {
        log.info(s"Starting child $name")
        val child = context.actorOf(Props[Child], name)
        context.watch(child)
        context.become(withChildren(children + (name -> child)))
      }
      case StopChild(name) => {
        val child = children(name)
        log.info(s"Stopping child: $name")
        context.stop(child)
      }
      case Stop => {
        log.info(s"Stopping $self")
        context.stop(self)
      }
      case Terminated(ref) => log.info(s"Terminated $ref")
    }
  }

  class Child extends Actor with ActorLogging {
    override def postStop(): Unit = log.info(s"post Stop $self")
    override def receive: Receive = {
      case message => {
        log.info(s"$message")
      }
    }
  }

  val parent = system.actorOf(Props[Parent], "papa")
  parent ! StartChild("beta1")
  parent ! StartChild("beta2")
  val beta1 = system.actorSelection("/user/papa/beta1")
  val beta2 = system.actorSelection("/user/papa/beta2")
  beta1 ! "Hi there"
  beta2 ! "Hi there"
  //parent ! StopChild("beta1")
  parent ! Stop
  for(i <- 1 to 500) {
    beta1 ! s"beat_$i"
  }
}
