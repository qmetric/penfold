package com.qmetric.penfold.readstore

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.mock.Mockito

class PageResultTest extends SpecificationWithJUnit with Mockito {

  "know when empty" in {
    new PageResult(List(mock[TaskRecord]), previousPage = None, nextPage = None).isEmpty must beFalse
    new PageResult(Nil, previousPage = None, nextPage = None).isEmpty must beTrue
  }
}
