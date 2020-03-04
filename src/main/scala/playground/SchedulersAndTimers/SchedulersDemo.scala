package playground.SchedulersAndTimers

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}

import scala.language.postfixOps
import scala.concurrent.duration._

object SchedulersDemo extends App{

  class SimpleActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(s"$message")
    }

    override def preStart(): Unit = log.info("Starting simple actor")
  }

  val system = ActorSystem("SchedulerSystem")
  val simpleActor = system.actorOf(Props[SimpleActor],"simpleActor")

  /**
   * Approach 1 => using system.scheduler
   */
  import system.dispatcher
/*  system.scheduler.scheduleOnce(3 second) {
    simpleActor ! "Hi there!"
  }*/

  /**
   * scheduling for interval (This approach is not a recommended approach. Look for better alternatives)
   */
/*  val heartbeat = system.scheduler.schedule(2 second, 1 second) {
    simpleActor ! "heartbeat"
  }

  system.scheduler.scheduleOnce(7 second) {
    heartbeat.cancel()
  }*/

  /**
   * Exercise => Create self closing actor which will remain active after a message is received.
   */
  class SelfClosingActor extends Actor with ActorLogging {

    override def postStop(): Unit = {
      log.info("Stopped")
    }
    var cancellable = createScheduleForOneSecond

    private def createScheduleForOneSecond = {
      context.system.scheduler.scheduleOnce(1 second) {
        self ! "stop"
      }
    }

    override def receive: Receive = {
      case "stop" => {
        context.stop(self)
      }
      case message => {
        log.info(s"Received $message")
        cancellable.cancel()
        cancellable = createScheduleForOneSecond
      }
    }
  }

  val selfClosingActor = system.actorOf(Props[SelfClosingActor], "selfClosingActor")

  val testSchedule = system.scheduler.schedule(100 millis, 300 millis) {
    selfClosingActor ! "check"
  }

  system.scheduler.scheduleOnce(10 second) {
   testSchedule.cancel()
  }

}
