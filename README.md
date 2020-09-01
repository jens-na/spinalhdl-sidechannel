# Overview

**spinalhdl-sidechannel** is a collection of Side-channel hardening extensions for the hardware description language SpinalHDL. This repository is work in progress for now.

## Prerequisites
- Verilator v.3.926
- SBT 1.3.13
- Scala 2.11.12
- SpinalHDL 1.4.0 [[ecb5a80](https://github.com/SpinalHDL/SpinalHDL/releases/tag/v1.4.0)]

## Usage
Since this repository is not present in any public repositories it is neccessary to install the project in your local SBT repository:
```
$ git clone https://github.com/jens-na/spinalhdl-sidechannel.git
$ cd spinalhdl-sidechannel
$ sbt publish-local
```
To use the modules the dependency must be added in your projects `build.sbt` file:
```
libraryDependencies += "com.github.spinalhdl" % "spinalhdl-sidechannel_2.11" % "0.1"
```

### Extension: Counter
The counter extension adds more functionality to the SpinalHDL [Counter](https://spinalhdl.github.io/SpinalDoc-RTD/SpinalHDL/Examples/Simple%20ones/counter_with_clear.html?highlight=counter). With the enabled extension it is possible to "count" from start to end in an arbitrary order. 

Default counter:
```
// => [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, A, B, C, D, E, F] (values per cycle)
val cx = Counter(16) 
```
Counter with arbitrary execution:
```
// => [3, A, 7, 0, C, 1, 5, E, B, 8, 2, D, 9, 6, F, 4] (values per cycle, random permutation)
val cy = Counter(16) arbitraryOrder() 

// Needs a seed for the PRNG which is needed to create the random permutation
val seed = ... // io.seed for example
cy.asInstanceOf[HidingCounter].Shuffle.seed := seed
```
The function `arbitraryOrder() ` activates the extension and overrides some core Counter functionality:
- `Counter.willOverflowIfInc` signal is set to high in the next cycle if the next cycle will also be the last number in the permutation.
- `Counter.willClear` signal is set when the function `clear()` is called and causes the counter to create a new permutation of the values. This process takes N-1 cycles, where N is the maximum counter value.

Counter with double buffering:

For a lot of use cases it is neccessary to count from the start value to the end value. If the counter overflows the counter gets cleared and reset to the start value and the process repeats. Since the counter extension must draw new random numbers to create a new permutation, the `clear()` function adds N-1 cycles of delay per counter overflow to the circuit. To make the delay as little as possible a double buffer functionality is implemented:

```
val cy = Counter(16) arbitraryOrder() doubleBuffer()

// Needs two seeds for two internal counters
cy.asInstanceOf[HidingCounterDoubleBuffer].c1.Shuffle.seed := ... // io.seed1 for example
cy.asInstanceOf[HidingCounterDoubleBuffer].c2.Shuffle.seed := ... // io.seed2 for example
```
The double buffer holds two internal counters which alternate when the a counter overflows. The not active counter creates a new permutation while the other counter is in use.

### Examples
The AES implementation in the SpinalCrypto library uses a `Counter(16)` in the SubBytes step. In this step each byte a_{i,j} in the state array gets replaced by S(a_{i,j}) with a S-Box substitution. These S-Box lookups are independet of each other and may be executed in an arbitrary order with optional double buffering.
<table style="padding:10px">
  <tr>
    <td>Default Counter</td>
    <td>Arbitrary Order Execution</td>
  <tr>
    <td><img alt="counter default" src="https://i.imgur.com/JT4qFTu.png"></img></td>
    <td><img alt="counter arbitrary order" src="https://i.imgur.com/1k83nwH.png"></img></td>
  </tr>
</table>

### Tests
Tests and examples can be found in the folder `src/test/scala/spinal/lib/sidechannel` and can be run with
```
$ cd spinalhdl-sidechannel
$ sbt test
```

# License
Released code is licensed under the MIT license, if not stated differently.

[1]: https://i.imgur.com/JT4qFTu.png
[2]: https://i.imgur.com/1k83nwH.png
