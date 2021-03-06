package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model._
import org.joda.time.DateTime
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.domain.model.Payload

case class FutureTaskCreated(aggregateId: AggregateId,
                             aggregateVersion: AggregateVersion,
                             created: DateTime,
                             queue: QueueId,
                             triggerDate: DateTime,
                             payload: Payload,
                             score: Long) extends TaskCreatedEvent