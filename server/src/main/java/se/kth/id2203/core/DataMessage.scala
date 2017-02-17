package se.kth.id2203.core

import se.kth.id2203.core.ExercisePrimitives._
import se.sics.kompics.KompicsEvent
;

/**
  * Created by sindrikaldal on 17/02/17.
  */
case class DataMessage(timestamp: VectorClock, payload: KompicsEvent) extends KompicsEvent;
