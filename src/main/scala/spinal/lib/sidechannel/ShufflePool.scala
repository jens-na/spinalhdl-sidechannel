/*
 * The MIT License (MIT)
 * Copyright (c) 2020, Jens Nazarenus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package spinal.lib.sidechannel

import spinal.core._
import spinal.lib._
import scala.collection.mutable.ListBuffer

/**
 * The shuffle configuration. A shuffle with length 'length' always starts
 * with the number 0 and ends at index length - 1.
 * @param length
 * @param fn the number transformation function, default: identity function
 */
case class ShuffleConfig(length: BigInt, fn : UInt => UInt = (x) => x)


/**
 * The pool which holds multiple instances of Fisher Yates Shuffles.
 */
object ShufflePool {
  var configs = ListBuffer[ShuffleConfig]()

  def add(config : ShuffleConfig): Unit = {
    require(config.length > 1)
    configs += config
  }

  case class ShuffleComponent() extends Component {
    def build(): Unit = {
    }

    val reg = Bits(12 bits) keep()

    for(x <- configs) { // Create all configs
      println(x.hashCode() + ": " +x.length)
    }

    Component.current.addPrePopTask(() => build())
  }

}

