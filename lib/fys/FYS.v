/*
 * This file is Fisher-Yates shuffle module.
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

module FYS
#(
  parameter m = 13
)
(
  input wire clk,
  input wire start,
  input wire [63:0] seed,
  output wire INIT_done,
  output wire done,
  input wire rd_en,
  input wire [m-1:0] rd_addr_P,
  output wire [m-1:0] data_out
);

reg reg_init_done = 0;

assign INIT_done = reg_init_done;

//init the memory
//interface for mem_dual
wire [m-1:0] din_A;
wire [m-1:0] din_B;
wire wren;
wire [m-1:0] addr_A;
wire [m-1:0] addr_B;
wire [m-1:0] dout_A;
wire [m-1:0] dout_B;

reg [m-2:0] init_counter = 0;
// for left-1-bit shift use later
wire [m-1:0] init_counter_ex;
assign init_counter_ex = {1'b0, init_counter};

reg wren_perm = 0;

//mem contents permutation
//address A thres
reg [m-1:0] thres = 0;

//write happens one cycle after read, PRG changes each cycles, need to hold it for writing
reg [m-1:0] PRG_buffer = 0;

//choose the last m bits of PRG output as random number
wire [63:0] PRG;
wire [m-1:0] PRG_low;
wire [m-1:0] PRG_low_shift;

reg [4:0] sec = 0;
wire sec_done;

always @(posedge clk)
  begin
    init_counter <= (init_counter > 0 | start) ? init_counter + 1 : init_counter;
  end

wire init_running;
assign init_running = start | (init_counter > 0);

assign wren = init_running | wren_perm;

assign addr_A = init_running ? (init_counter_ex << 1) : thres;
assign addr_B = init_running ? addr_A + 1 : 
				wren_perm ? PRG_buffer : PRG_low_shift;

assign din_A = init_running ? addr_A : dout_B;
assign din_B = init_running ? addr_B : dout_A;
 
wire init_done_buffer;
assign init_done_buffer = (init_counter == ((1 << (m-1)) - 1));
reg swap_running = 0;

always @(posedge clk)
  begin
	  reg_init_done <= init_done_buffer ? 1 : 0;
	  swap_running <= init_done_buffer ? 1 : 
	  					(sec == (m+1)) ? 1'b0 : swap_running;
  end


wire perm_start;
assign perm_start = reg_init_done;

//address A changes after each swapping
always @(posedge clk)
  begin
	  thres <= init_done_buffer ? ((1 << m) - 1) :
	  			(!init_running && wren) ? thres - 1: thres;
  end

wire PRG_valid;
assign PRG_valid = (PRG_low_shift <= thres) && swap_running;

always @(posedge clk)
  begin
    wren_perm <= start ? 0 :
	  			PRG_valid ? (!wren_perm) : 0;			
  end

assign PRG_low = PRG[m-1:0];

assign PRG_low_shift = (PRG_low >> sec);

always @(posedge clk)
  begin
    PRG_buffer <= PRG_low_shift;
  end

wire [m-1:0] checkpoint;
assign checkpoint = (1 << (m-1-sec));

assign sec_done = (thres == checkpoint);

always @(posedge clk)
  begin
    sec <= start ? 0 :
	  		(sec_done && (sec <= (m+1))) ? sec + 1 : sec;
  end

reg done_buffer = 0;

always @(posedge clk)
  begin
	  done_buffer <= start ? 0 : (sec == (m+1));
  end
  
assign done = done_buffer; 

assign data_out = dout_A;

XorShift64 Xorshift64_inst(
  .clk(clk),
  .rst(start),
  .seed(seed),
  .rngout(PRG)
);

mem_dual #(.WIDTH(m), .DEPTH(1 << m)) mem_dual_inst (
  .clock(clk),
  .data_0(din_A),
  .data_1(din_B),
  .address_0(rd_en ? rd_addr_P : addr_A),
  .address_1(addr_B),
  .wren_0(wren),
  .wren_1(wren),
  .q_0(dout_A),
  .q_1(dout_B)
);
 
endmodule



