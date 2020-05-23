/**
 * Copyright © 2014 - 2020 Lightbend, Inc. All rights reserved. [http://www.lightbend.com]
 */

package com.lightbend.training.coffeehouse

import akka.testkit.TestProbe

class CoffeeHouseAppSpec extends BaseAkkaSpec {

  import CoffeeHouseApp._

  "Calling argsToOpts" should {
    "return the correct opts for the given args" in {
      argsToOpts(List("a=1", "b", "-Dc=2")) should ===(Map("a" -> "1", "-Dc" -> "2"))
    }
  }

  "Calling applySystemProperties" should {
    "apply the system properties for the given opts" in {
      System.setProperty("c", "")
      applySystemProperties(Map("a" -> "1", "-Dc" -> "2"))
      System.getProperty("c") should ===("2")
    }
  }

  "Creating CoffeeHouseApp" should {
    "result in creating a top-level actor named 'coffee-house'" in {
      new CoffeeHouseApp(system)
      TestProbe().expectActor("/user/coffee-house")
    }
    "result in sending a message to CoffeeHouse" in {
      val coffeeHouse = TestProbe()
      new CoffeeHouseApp(system) {
        override def createCoffeeHouse() = coffeeHouse.ref
      }
      coffeeHouse.expectMsgType[Any]
    }
  }
}
