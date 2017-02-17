package se.kth.id2203.core

/**
  * Created by sindrikaldal on 17/02/17.
  */

import se.kth.id2203.core.Ports._
import se.kth.id2203.core.ExercisePrimitives._
import se.sics.kompics.network._
import se.sics.kompics.sl.{Init, _}
import se.sics.kompics.{ComponentDefinition => _, Port => _, KompicsEvent}

import scala.collection.immutable.Set
import scala.collection.mutable.ListBuffer

class WaitingCRB(init: Init[WaitingCRB]) extends ComponentDefinition {

  //subscriptions
  val rb = requires[ReliableBroadcast];
  val crb = provides[CausalOrderReliableBroadcast];

  //configuration
  val (self, vec) = init match {
    case Init(s: Address, t: Set[Address]@unchecked) => (s, VectorClock.empty(t.toSeq))
  };

  //state
  var pending: ListBuffer[(Address, DataMessage)] = ListBuffer();
  var lsn = 0;

  //handlers
  crb uponEvent {
    case x: CRB_Broadcast => handle {
      // Copy the vector clock
      val W = VectorClock(vec);
      //W[self ] := lsn
      W.set(self, lsn);
      //lsn := lsn + 1
      lsn += 1;
      //Reliable broadcast the message and piggyback the vector clock
      trigger(RB_Broadcast(DataMessage(W, x.payload)) -> rb);
    }
  }

  rb uponEvent {
    case x@RB_Deliver(src: Address, msg: DataMessage) => handle {
      pending += ((src, msg));
      var i = 1;
      while(i == 1) {
        try {
          for((address, dataMessage) <- pending if dataMessage.timestamp <= vec) {
            pending -= ((address, dataMessage));
            vec.inc(address);
            trigger(CRB_Deliver(address, dataMessage.payload) -> crb);
            throw continueException("continue")
          }
          i = 0;
        }
        catch {
          case continueException : Throwable => i = 1;
        }
      }


    }
  }
}
