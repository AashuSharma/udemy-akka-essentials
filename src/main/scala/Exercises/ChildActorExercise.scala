package Exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.collection.immutable.Queue

object ChildActorExercise extends App {

  //Distributed word counting
  object WordCountMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(task: String)
    case class WordCountReply(count: Int)
  }
  class WordCountMaster extends Actor {
    import WordCountMaster._
    override def receive: Receive = {
      case Initialize(num) => {
        val queue = Queue((1 to num).map(x => context.actorOf(Props[WordCounterWorker], s"Child$x")) : _*)
        context.become(initialized(queue))
      }
    }

    def initialized(queue: Queue[ActorRef]): Receive = {
      case task:String => {
        val (worker, remainingQ) = queue.dequeue
        worker ! WordCountTask(task)
        context.become(initialized(remainingQ.enqueue(worker)))
      }
      case WordCountReply(c) => println(s"${sender().path} calculates to $c")
    }
  }

  class WordCounterWorker extends Actor {
    import WordCountMaster._
    override def receive: Receive = {
      case WordCountTask(word) => sender() ! WordCountReply(word.split(" ").size)
    }
  }
  import WordCountMaster._
  val system = ActorSystem("WordCountSystem")
  val master = system.actorOf(Props[WordCountMaster], "master")
  master ! Initialize(3)
  master ! "Akka is awesome"
  master ! "My family is best"
  master ! "Hey how are you"
  master ! "My love life is going awesome"
  master ! "Me myself"
  /**
   * create WordCountMaster
   * send Initialize(10) to WordCountMaster
   * send "Akka is awesome" to WordCountMaster
   * wcm will send a WordCountTask("...") to one of its children
   * chils reply with WordCountReply(3) to master
   * master replies with 3 to sender
   *
   * requester -> wcm -> wcw
   *         r <- wcm <-
   */

}
