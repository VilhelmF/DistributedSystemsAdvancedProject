package se.kth.id2203.core

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address

/**
  * Created by sindrikaldal on 17/02/17.
  */
case class OriginatedData(src: Address, payload: KompicsEvent) extends KompicsEvent;
