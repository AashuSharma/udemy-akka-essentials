package playground

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging
import com.typesafe.config.ConfigFactory

object AkkaLoggingDemo extends App {

  val loggingInlineConf =
    """
      |akka {
      |loglevel = INFO
      |}
      |""".stripMargin

  val config = ConfigFactory.parseString(loggingInlineConf)

  val system = ActorSystem("InlineLoggingSystem", config)

  class InlineLoggerActor1 extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info("1) My message is {}",message)
    }
  }

  class InlineLoggerActor2 extends Actor {
    val logger = Logging(context.system, this)
    override def receive: Receive = {
      case message => logger.info("2) My message is {}",message)
    }
  }

  val inlineLoggerActor1 = system.actorOf(Props[InlineLoggerActor1], "InlineLoggerActor1")
  inlineLoggerActor1 ! "Hello inline configuration1"

  val inlineLoggerActor2 = system.actorOf(Props[InlineLoggerActor2], "InlineLoggerActor2")
  inlineLoggerActor2 ! "Hello inline configuration2"

  val nestedConfig = ConfigFactory.load().getConfig("myNestedConf")
  val appConfActorSystem = ActorSystem("AppConfActorSystem", nestedConfig)
  val appConfActor = appConfActorSystem.actorOf(Props[InlineLoggerActor1], "InlineLoggerActor3")
  appConfActor ! "Hello from nested application conf"

  val nestedFileConfig = ConfigFactory.load("MyLogConfig.conf").getConfig("FileNestedConf")
  val fileConfActorSystem = ActorSystem("fileConfActorSystem", nestedFileConfig)
  val fileConfActor = fileConfActorSystem.actorOf(Props[InlineLoggerActor1], "fileConfActor")
  fileConfActor ! "Hello from nested file conf"

}
