package playground.Router

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}

object RouterDemo extends App{

  /**
   * Method 1 => Manually
   */
  class Master extends Actor with ActorLogging {
    //step 1 create routees
    val slaves = for (i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave_$i")
      context.watch(slave)
      ActorRefRoutee(slave)
    }

    //step 2 create router
    var router = Router(RoundRobinRoutingLogic(), slaves)
    override def receive: Receive = {
      case message => {
        //step 3 route message
        router.route(message, sender())
      }
      case Terminated(slave) => {
        //step 4 handle the termination of the router
        router = router.removeRoutee(slave)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router = router.addRoutee(newSlave)
      }
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive =  {
      case message => log.info(s"${message}")
    }
  }

  val system = ActorSystem("RouterSystem")
  val router = system.actorOf(Props[Master], "masterRouter")
  for (i <- 1 to 10) {
    router ! s"[$i] Hi there!"
  }

}
