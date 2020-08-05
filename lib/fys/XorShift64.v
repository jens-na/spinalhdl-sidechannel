/*
 * This file is a PRNG module.
 *
 * Copyright (C) 2017
 * Authors: Wen Wang <wen.wang.ww349@yale.edu>
 *          Ruben Niederhagen <ruben@polycephaly.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
*/

module XorShift64 (
  input wire   		clk,
  input wire    	rst,
  input wire [63:0] seed,
  output reg	 [63:0] rngout
);

//reg [63:0] seed_next;
reg [63:0] rnglast = 0;

wire [63:0] rng1;
wire [63:0] rng2;
wire [63:0] rng3;

assign rng1 = rngout ^ (rngout << 64'd21);
assign rng2 = rng1 ^ (rng1 >> 64'd35);
assign rng3 = rng2 ^ (rng2 << 64'd4);

always @(posedge clk)
begin
  if(rst) 
    begin
      rngout <= seed;
      rnglast <= seed;
    end
  else
    begin
      rnglast <= rngout;
      rngout <= rng3;
    end
end

endmodule








