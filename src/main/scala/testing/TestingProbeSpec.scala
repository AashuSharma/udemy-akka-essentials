package testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import testing.TestingProbSpec.{Master, Register, RegisterAck, Response, SlaveWork, Work, WorkCompleted}

class TestingProbeSpec extends TestKit(ActorSystem("TestProbeSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A master actor" should {
    "Register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegisterAck)
    }

    "send work to slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegisterAck)
      val msg = "I love akka"
      master ! Work(msg)
      slave.expectMsg(SlaveWork(msg, testActor))
      slave.reply(WorkCompleted(3,testActor))
      expectMsg(Response(3))
    }

    "aggregate the result" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegisterAck)

      val msg = "I am Ashu Sharma"
      master ! Work(msg)
      master ! Work(msg)

      slave.receiveWhile() {
        case SlaveWork(`msg`, `testActor`) => slave.reply(WorkCompleted(4, testActor))
      }
      expectMsg(Response(4))
      expectMsg(Response(8))
    }
  }


}

object TestingProbSpec {
  //scenario
  /*
  word counting actor hierarchy  master-slave

  send some work to master
  - master sends the piece of work to slave
  - slave processes the work and reply to master
  - master aggregates the result
  master sends the result to the original requester
   */

  case class Register(slaveRef: ActorRef)
  case class Work(text: String)
  case class SlaveWork(text: String, originalSender: ActorRef)
  case class WorkCompleted(count: Int, originalSender: ActorRef)
  case class Response(count: Int)
  case object RegisterAck

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! RegisterAck
        context.become(register(slaveRef,0))
    }

    def register(slaveRef: ActorRef, totalCount:Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalSender) => {
        val newCount = count + totalCount
        originalSender ! Response(newCount)
        context.become(register(slaveRef,newCount))
      }
    }
  }

}
