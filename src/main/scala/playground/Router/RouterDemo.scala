package playground.Router

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, FromConfig, RoundRobinGroup, RoundRobinPool, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory

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
  /*for (i <- 1 to 10) {
    router ! s"[$i] Hi there!"
  }*/

  /**
   * Method 2.1 => Using Router Actor
   */
  val master2 = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "masterRouter2")
/*  for (i <- 1 to 10) {
    master2 ! s"[$i] Hi there!"
  }*/

  /**
   * Method 2.2 => Using Router Actor via configuration
   */
  val system2 = ActorSystem("RouterSystem2", ConfigFactory.load().getConfig("RouterDemo"))
  val master3 = system2.actorOf(FromConfig.props(Props[Slave]), "master3")
/*  for (i <- 1 to 10) {
    master3 ! s"[$i] Hi there!"
  }*/

  /**
   * Method 3.1 Router Group for actors declared somewhere else
   */

  val slavesPath = (1 to 5).map(i => system.actorOf(Props[Slave], s"slave_$i").path.toString).toList
  val master4 = system.actorOf(RoundRobinGroup(slavesPath).props(), "master4")
/*  for (i <- 1 to 10) {
    master4 ! s"[$i] Hi there!"
  }*/

  /**
   * Method 3.2 Router Group for actors somewhere else configuration
   */
  val system3 = ActorSystem("RouterSystem3", ConfigFactory.load().getConfig("RouterGroupDemo"))
  (1 to 5).foreach(i => system3.actorOf(Props[Slave], s"slave_$i"))
  val master5 = system3.actorOf(FromConfig.props(), "master5")
  for (i <- 1 to 10) {
    master5 ! s"[$i] Hi there!"
  }
}
