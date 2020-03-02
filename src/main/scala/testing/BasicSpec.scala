package testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import testing.BasicSpec.{BlackHoleActor, FancyActor, SimpleActor}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
{
  //set up
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A simple actor" should {
    "return the message received" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "Hello test"
      echoActor ! message

      expectMsg("Hello test") // default timeout -> akka.test.single-expect-default
    }
  }

  "A Black hole actor" should {
    "not return the message received" in {
      val blackHoleActor = system.actorOf(Props[BlackHoleActor])
      val message = "Hello test"
      blackHoleActor ! message

      expectNoMessage(1 second)
    }
  }

  "A fancy actor" should {
    val fancyActor = system.actorOf(Props[FancyActor])
    "reply in upper case for any message" in {
      fancyActor ! "i am ashu"
      val reply = expectMsgType[String]
      assert(reply == "I AM ASHU")
    }

    "reply with Hi or Hello for greeting" in {
      fancyActor ! "greeting"
      expectMsgAnyOf("Hi","Hello")
    }

    "reply with Scala and Akka for fancy" in {
      fancyActor ! "fancy"
      expectMsgAllOf("Scala", "Akka")
    }

    "reply with Scala and Akka for fancy different way" in {
      fancyActor ! "fancy"
      expectMsgPF() {
        case "Akka" => //do nothing
        case "Scala" => //nothing
      }
    }
  }
}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class BlackHoleActor extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class FancyActor extends Actor {
    val random = new Random()

    override def receive: Receive = {
      case "greeting" => if(random.nextBoolean()) sender() ! "Hi" else sender() ! "Hello"
      case "fancy" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase()

    }
  }
}
