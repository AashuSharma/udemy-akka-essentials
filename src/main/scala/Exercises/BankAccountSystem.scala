package Exercises

import Exercises.BankAccountSystem.Person.Banking
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object BankAccountSystem extends App {
  /**
   * 2 - create a Bank Account as an actor
   * receives
   *  - Deposit an amount
   *  - Withdraw an amount
   *  - Statement
   * replies with
   *  - Success
   *  - Failure
   *
   *  interact with some other kind of actor
   */

  val bankAccountSystem = ActorSystem("BankAccountActorSystem")

  class BankAccountActor extends Actor {
    var balance=0
    import BankAccountActor._
    override def receive: Receive = {
      case Withdraw(amount) => {
        if(amount<0)
          TransactionFailure("Invalid amount!!")
        else if(balance - amount < 0) {
          TransactionFailure("Insufficient funds")
        }
        else {
          balance-= amount
          TransactionSuccess("Funds withdraw success")
        }
      }
      case Deposit(amount) => {
        if(amount<0) {
          TransactionFailure("Invalid amount")
        } else {
          balance += amount
          TransactionSuccess("Funds deposit Success")
        }
      }
      case Statement => println(balance)
    }
  }

  object BankAccountActor {
    case class Deposit(amount:Int)
    case class Withdraw(amount:Int)
    case object Statement

    case class TransactionFailure(reason: String)
    case class TransactionSuccess(message: String)
  }

  object Person {
    case class Banking(bankAccount:ActorRef)
  }

  class Person extends Actor {
    import BankAccountActor._

    override def receive: Receive = {
      case Banking(bankAccount) => {
        bankAccount ! Deposit(100)
        bankAccount ! Deposit(200)
        bankAccount ! Statement
        bankAccount ! Withdraw(400)
        bankAccount ! Withdraw(50)
        bankAccount ! Statement
      }
    }
  }

  val bankAccountActor = bankAccountSystem.actorOf(Props[BankAccountActor], "bankAccountActor")
  val personActor = bankAccountSystem.actorOf(Props[Person], "Bob")

  personActor ! Banking(bankAccountActor)
}
