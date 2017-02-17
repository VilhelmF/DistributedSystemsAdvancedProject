package se.kth.id2203.core

/**
  * Created by sindrikaldal on 17/02/17.
  */

import se.kth.id2203.core.Ports._
import se.sics.kompics.network._
import se.sics.kompics.sl.{Init, _}
import se.sics.kompics.{ComponentDefinition => _, Port => _, KompicsEvent}

case class OriginatedData(src: Address, payload: KompicsEvent) extends KompicsEvent;

class EagerReliableBroadcast(init: Init[EagerReliableBroadcast]) extends ComponentDefinition {

  //EagerReliableBroadcast Subscriptions
  val beb = requires[BestEffortBroadcast];
  val rb = provides[ReliableBroadcast];

  //EagerReliableBroadcast Component State and Initialization
  val self = init match {
    case Init(s: Address) => s
  };
  var delivered = collection.mutable.Set[KompicsEvent]();

  //EagerReliableBroadcast Event Handlers
  rb uponEvent {
    case x@RB_Broadcast(payload) => handle {
      trigger(BEB_Broadcast(OriginatedData(self, payload)) -> beb);
    }
  }

  beb uponEvent {
    case BEB_Deliver(_, data@OriginatedData(origin, payload)) => handle {
      if (!delivered.contains(payload)) {
        delivered = delivered + payload;
        trigger(RB_Deliver(origin, payload) -> rb);
        trigger(BEB_Broadcast(OriginatedData(origin, payload)) -> beb);
      }
    }
  }
}

