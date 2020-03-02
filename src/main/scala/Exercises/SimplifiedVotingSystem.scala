package Exercises

import Exercises.SimplifiedVotingSystem.Citizen.{VoteStatusReply, VoteStatusRequest}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object SimplifiedVotingSystem extends App {

  val system = ActorSystem("SimplifiedActorSystem")
  object Citizen {
    case class Vote(candidate:String)
    case object VoteStatusRequest
    case class VoteStatusReply(candidate:Option[String])
  }
  class Citizen extends Actor {
    import Citizen._
    override def receive: Receive = {
      case Vote(candidate) => context.become(voted(candidate))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate:String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
      case Vote(_) => sender() ! VoteStatusReply(None)
    }
  }

  object VoteAggregator {
    case class AggregateVotes(citizens: Set[ActorRef])
  }
  class VoteAggregator extends Actor {
    import VoteAggregator._
    override def receive: Receive = {
      case AggregateVotes(set) => {
        set.foreach(x => x ! VoteStatusRequest)
        context.become(generateStats(set, Map()))
      }
    }

    def generateStats(remainingCitizens:Set[ActorRef], stats:Map[String, Int]):Receive = {
      case VoteStatusReply(Some(candidate)) => {
        val newStats = stats + (candidate -> (1+stats.getOrElse(candidate,0)))
        val newRemCit = remainingCitizens - sender()
        if(newRemCit.isEmpty) {
          println(newStats)
        } else {
          context.become(generateStats(newRemCit,newStats))
        }
      }
      case VoteStatusReply(None) => sender() ! VoteStatusRequest
    }
  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  import Citizen._
  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])

  import VoteAggregator._
  voteAggregator ! AggregateVotes(Set(charlie, alice, bob, daniel))


}
