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

import spinal.core.internals.Operator.BitVector
import spinal.core.{Bits, _}
import spinal.core.internals._

import scala.collection.mutable.ArrayBuffer

//object MB extends BitVectorLiteralFactory[MaskedBits] {
//  def apply(): MaskedBits = new MaskedBits()
//  def getFactory: (BigInt, Int, MaskedBits) => MaskedBits =  BitsLiteral.apply[MaskedBits]
//  def isSigned: Boolean = false
//}

//trait MaskedBitsFactory {
//  /** create a masked bits vector */
//  def MaskedBits() = new MaskedBits2()
//  /** create a masked bits vector with a given width */
//  def MaskedBits(width: BitCount): MaskedBits2 = MaskedBits().setWidth(width.value)
//}
////
////object MB extends BitVectorLiteralFactory[MaskedBits2] {
////    def apply(): MaskedBits2 = new MaskedBits2()
////    def apply(that: Data): MaskedBits2 = ???
////
////    override def newInstance(bitCount: BitCount): MaskedBits2 = MaskedBits(bitCount)
////    override def isSigned: Boolean = false
////    override def getFactory: (BigInt, Int, MaskedBits2) => MaskedBits2 = ???
////}
//
//class MaskedBits2 extends BaseType with BitwiseOp[MaskedBits2] with Widthable {
//  private[spinal] def _data: (Bits, Bits) = (Bits, Bits)
//  type T = (Bits, Bits)
//
//  override def weakClone: this.type = new MaskedBits2().asInstanceOf[this.type]
//  def weakCloneBits: Bits = new Bits()
//
//  override def opName: String = "MaskedBits"
//
//  def := (that: (Bits, Bits)): Unit = {
//    _data._1 := that._1
//    _data._2 := that._2
//  }
//
//  def setWidth(width: Int): this.type = {
//    _data._1.setWidth(width)
//    _data._2.setWidth(width)
//    this
//  }
//
//  private[sidechannel] def computeMaskedData(right: MaskedBits2, op: BinaryOperator): this.type = {
//    assert(right != null)
//
//    val n1 = weakCloneBits.setAsTypeNode()
//    op.left = this._data._1.asInstanceOf[op.T]
//    op.right = right._data._1.asInstanceOf[op.T]
//    n1.assignFrom(op)
//
//    val n2 = weakCloneBits.setAsTypeNode()
//    op.left = this._data._2.asInstanceOf[op.T]
//    op.right = right._data._2.asInstanceOf[op.T]
//    n2.assignFrom(op)
//
//    val typeNode = this.weakClone.setAsTypeNode()
//    typeNode.assignFrom(n1, n2)
//    typeNode
//  }
//
//  override def &(right: MaskedBits2): MaskedBits2 = computeMaskedData(right, new Operator.Bits.And)
//  override def |(right: MaskedBits2): MaskedBits2 = computeMaskedData(right, new Operator.Bits.Or)
//  override def ^(right: MaskedBits2): MaskedBits2 = computeMaskedData(right, new Operator.Bits.Xor)
//  override def unary_~ : MaskedBits2 = this
//
//
//  override def getTypeObject: Any = null
//  override def getZero: MaskedBits2.this.type = null
//  override def asBits: Bits = _data._1 ^ _data._2
//  override def assignFromBits(bits: Bits): Unit = null
//  override def assignFromBits(bits: Bits, hi: Int, low: Int): Unit = null
//  override def getBitsWidth: Int = 0
//  override type RefOwnerType = this.type
//  override def newMultiplexerExpression() = new MultiplexerBits
//  override def newBinaryMultiplexerExpression() = new BinaryMultiplexerBits
//  override def isEquals(that: Any): Bool = False
//  override def isNotEquals(that: Any): Bool = False
//  override def calcWidth : Int = 0
//}

//object MaskedBits2 {
//  def apply(width: BitCount): MaskedBits2 = new MaskedBits2(width)
//}
//
//class MaskedBits2(width: BitCount) extends ImplicitArea[MaskedBits2] {
//  type T = MaskedBits2
//  private[sidechannel] def weakClone: this.type = new MaskedBits2(width).asInstanceOf[this.type]
//
//  val value = Bits(width)
//  val mask = Bits(width)
//
//  def :=(that: T): Unit = {
//    this.value := that.value
//    this.mask := that.mask
//  }
//
//  def :=(that: (Bits, Bits)): Unit = {
//    this.value := that._1
//    this.mask := that._2
//  }
//
//  def &(right: MaskedBits2): MaskedBits2 = {
//    assert(right != null)
//    weakClone.value := (this.value & right.value)
//    weakClone.mask := (this.mask & right.mask)
//    weakClone
//  }
//
//  def |(right: MaskedBits2): MaskedBits2 = {
//    assert(right != null)
//    weakClone.value := (this.value | right.value)
//    weakClone.mask := (this.mask | right.mask)
//    weakClone
//  }
//  def ^(right: MaskedBits2): MaskedBits2 = {
//    assert(right != null)
//    weakClone.value := (this.value ^ right.value)
//    weakClone.mask := (this.mask ^ right.mask)
//    weakClone
//  }
//
//  def asBits: Bits = {
//    val result = new Bits()
//    result := (value ^ mask)
//    result
//  }
//
//  def unary_~ : MaskedBits2 = this
//
//  override def implicitValue: MaskedBits2 = this
//
//  override type RefOwnerType = this.type
//}

//class Masked2[T <: Data](val dataType: HardType[T]) extends MultiData with BitwiseOp[Masked2[T]] {
//
//  if(component != null) component.addPrePopTask(() => {
//    for(i <- elements.indices){
//      val name = elements(i)._1
//      val e = elements(i)._2
//      if(OwnableRef.proposal(e, this)) e.setPartialName(name, Nameable.DATAMODEL_WEAK)
//    }
//  })
//
//  val share_2 = dataType()
//  val share_1 = dataType()
//  def weakClone: this.type = new Masked2(dataType).asInstanceOf[this.type]
//
//  def := (that: (T, T)): Unit = {
//    share_1 := (that._1.asBits ^ that._2.asBits).asInstanceOf[T]
//    share_2 := that._2
//  }
//
//  override def assignFromImpl(that: AnyRef, target: AnyRef, kind: AnyRef): Unit = {
//    that match {
//      case (x, y) => {
//        share_1 := (x.asInstanceOf[T].asBits ^ y.asInstanceOf[T].asBits).asInstanceOf[T]
//        share_2 := y.asInstanceOf[T]
//      }
//      case (true, x, y) => { // like (x,y) but without XOR
//        this.share_1 := x.asInstanceOf[T]
//        this.share_2 := y.asInstanceOf[T]
//      }
//      case x: Masked2[T] => {
//        this.share_1 := x.share_1
//        this.share_2 := x.share_2
//      }
//      case _ => SpinalError("Undefined assignment")
//    }
//  }
//
//  /** Returns the masked value as bits */
//  override def asBits: Bits = {
//    val ret = new Bits()
//    ret := share_1.asBits
//    ret
//  }
//
//  /** Returns unmasked value as bits */
//  def asUnmaskedBits: Bits = {
//    val ret = new Bits()
//    ret := share_1.asBits ^ share_2.asBits
//    ret
//  }
//
//  override def elements: ArrayBuffer[(String, Data)] = ArrayBuffer(
//    "share_1" -> share_1,
//    "share_2" -> share_2
//  )
//
//  override def &(right: Masked2[T]): Masked2[T] = {
//    val obj = weakClone
//    val newVal = (this.share_1.asBits & right.share_1.asBits).asInstanceOf[T]
//    val newMask = (this.share_2.asBits & right.share_2.asBits).asInstanceOf[T]
//    obj.assignFrom((newVal, newMask))
//    obj
//  }
//  override def |(right: Masked2[T]): Masked2[T] = this
//  override def ^(right: Masked2[T]): Masked2[T] = {
//    val obj = weakClone
//    val newVal = (this.share_1.asBits ^ right.share_1.asBits).asInstanceOf[T]
//    val newMask = (this.share_2.asBits ^ right.share_2.asBits).asInstanceOf[T]
//    obj.assignFrom((true, newVal, newMask))
//    obj
//  }
//  override def unary_~ : Masked2[T] = this
//  setRefOwner(this)
//}

trait MaskedFactory extends TypeFactory {
  def Masked2[T <: Data](dataType: HardType[T]) : MaskedN[T] = new MaskedN[T](dataType, 2)
  def Masked3[T <: Data](dataType: HardType[T]) : MaskedN[T] = new MaskedN[T](dataType, 3)
  def Masked4[T <: Data](dataType: HardType[T]) : MaskedN[T] = new MaskedN[T](dataType, 4)
  def MaskedN[T <: Data](dataType: HardType[T], numShares: Int) : MaskedN[T] = new MaskedN[T](dataType, numShares)
}

class MaskedN[T <: Data](val dataType: HardType[T], numShares: Int) extends MultiData with BitwiseOp[MaskedN[T]] {
  require(numShares >= 2, "Invalid number of shares")

  if(component != null) component.addPrePopTask(() => {
    for(i <- elements.indices){
      val name = elements(i)._1
      val e = elements(i)._2
      if(OwnableRef.proposal(e, this)) e.setPartialName(name, Nameable.DATAMODEL_WEAK)
    }
  })

  def weakClone: this.type = new MaskedN(dataType, numShares).asInstanceOf[this.type]

  val share = Vec(dataType(), numShares)
  share.zipWithIndex.foreach{ case(x,i) => x.setWeakName("share_" + i) } // Rename wires to "wire_share_0, wire_share_1, etc.

  def := (that: T*): Unit = {
    require(that.length == numShares, s"Operation with different number of shares. (this: ${this.numShares}, that: ${that.length})")
    assignFromOther(that, true)
  }

  def assignFromOther(that: Seq[T], zeroDifferent: Boolean): Unit = {
    require(that.length == numShares, s"Operation with different number of shares. (this: ${this.numShares}, that: ${that.length})")
    for(j <- (if(zeroDifferent) 1 else 0) until numShares) {
      share(j) := that(j)
    }
    if(zeroDifferent)
      share(0) := that.foldLeft(B(0).resize(dataType.getBitsWidth))(_.asBits ^ _.asBits).asInstanceOf[T]
  }

  override def assignFromImpl(that: AnyRef, target: AnyRef, kind: AnyRef): Unit = {
    that match {
      case x: Seq[T] => {
        assignFromOther(x, true)
      }
      case (false, x: Seq[T]) => {
        assignFromOther(x, false)
      }
      case x: MaskedN[T] => {
        this.share := x.share
      }
    }
  }

  override def elements: ArrayBuffer[(String, Data)] = {
    var array = ArrayBuffer[(String, Data)]()
    share.foreach(x => array += (x.getName() -> x))
    array
  }

  override def &(right: MaskedN[T]): MaskedN[T] = {
    val obj = weakClone
    val shares = Vec(dataType(), numShares)
    numShares match {
      case 2 => {
        // https://eprint.iacr.org/2018/1007.pdf page 7
        val a0 = share(0).asBits keep()
        val a1 = share(1).asBits keep()
        val b0 = right.share(0).asBits keep()
        val b1 = right.share(1).asBits keep()

        shares(0).assignFromBits((a0 & b0 ^ (a0 & b1 ^ b1)) ^ ((a1 & b0 ^ (a1 & b1 ^ b1)) ^ a1))
        shares(1).assignFromBits(a1)

        obj.assignFrom((false, shares))
        obj
      }
      case _ => this /* TODO for 3+ shares */
    }
  }

  override def |(right: MaskedN[T]): MaskedN[T] = this // TODO

  override def ^(right: MaskedN[T]): MaskedN[T] = {
    val obj = weakClone
    val shares = (right.share).zip(this.share).map(t => t._1.asBits ^ t._2.asBits)
    obj.assignFrom((false, shares))
    obj
  }

  override def unary_~ : MaskedN[T] = this // TODO

  /** Returns the masked value as bits */
  override def asBits: Bits = {
    val ret = new Bits()
    ret := share(0).asBits
    ret
  }

  /** Returns unmasked value as bits */
  def asUnmaskedBits: Bits = {
    val ret = new Bits()
    ret := share.foldLeft(B(0).resize(dataType.getBitsWidth))(_.asBits ^ _.asBits)
    ret
  }

  setRefOwner(this)
}

