package playground.SchedulersAndTimers

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Timers}

import scala.concurrent.duration._
import scala.language.postfixOps

object TimersDemo extends App {

  /**
   * Approach 2 => Extending Timers trait
   */
  case object SimpleTimerKey
  class SimpleActor extends Actor with ActorLogging with Timers{
    timers.startSingleTimer(SimpleTimerKey, "Hi there", 3 second)
    override def receive: Receive = {
      case message => log.info(s"$message")
    }
  }

  val system = ActorSystem("TimerSystem")
  val simpleTimerActor = system.actorOf(Props[SimpleActor], "simpleTimerActor")

  case object NonSimpleTimerKey
  class NonSimpleActor extends Actor with ActorLogging with Timers {
    timers.startTimerWithFixedDelay(NonSimpleTimerKey, "Akka is awesome", 2 second)

    override def receive: Receive = {
      case "stop" => timers.cancel(NonSimpleTimerKey)
      case message => log.info(s"$message")
    }
  }

  val nonSimpleActor = system.actorOf(Props[NonSimpleActor], "nonSimpleActor")
  import system.dispatcher
  system.scheduler.scheduleOnce(9 second){
    nonSimpleActor ! "stop"
  }

  /**
   * Self closing Actor using Timers
   */

  case object SelfClosingTimerKey
  class SelfClosingTimerActor extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(SelfClosingTimerKey, "stop", 1 second)
    override def receive: Receive = {
      case "stop" => timers.cancel()
      case message => {
        log.info(s"Received message: $message. Staying Alive!")
        timers.startSingleTimer(SelfClosingTimerKey, "stop", 1 second)
      }
    }
  }

  val selfClosingTimerActor = system.actorOf(Props[SelfClosingTimerActor], "selfClosingTimerActor")
  val sch = system.scheduler.schedule(100 millis, 500 millis) {
    selfClosingTimerActor ! "check"
  }

  system.scheduler.scheduleOnce(5 second) {
    sch.cancel()
  }
}
