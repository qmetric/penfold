package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{Version, AggregateId}

trait Event {
  val aggregateId: AggregateId
  val aggregateVersion: Version
}









