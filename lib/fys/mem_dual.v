
/*
 * Dual-ported memory module.
 *
 * Public domain.
 *
 */

`define CLOG2(x) ( \
  (x <= 2) ? 1 : \
  (x <= 4) ? 2 : \
  (x <= 8) ? 3 : \
  (x <= 16) ? 4 : \
  (x <= 32) ? 5 : \
  (x <= 64) ? 6 : \
  (x <= 128) ? 7 : \
  (x <= 256) ? 8 : \
  (x <= 512) ? 9 : \
  (x <= 1024) ? 10 : \
  (x <= 2048) ? 11 : \
  (x <= 4096) ? 12 : \
  (x <= 8192) ? 13 : \
  (x <= 16384) ? 14 : \
  (x <= 32768) ? 15 : \
  (x <= 65536) ? 16 : \
  (x <= 131072) ? 17 : \
  (x <= 262144) ? 18 : \
  (x <= 524288) ? 19 : \
  (x <= 1048576) ? 20 : \
  (x <= 2097152) ? 21 : \
  (x <= 4194304) ? 22 : \
  (x <= 8388608) ? 23 : \
  (x <= 16777216) ? 24 : \
  (x <= 33554432) ? 25 : \
  (x <= 67108864) ? 26 : \
  (x <= 134217728) ? 27 : \
  (x <= 268435456) ? 28 : \
  (x <= 536870912) ? 29 : \
  (x <= 1073741824) ? 30 : \
  -1)

module mem_dual
#(
  parameter WIDTH = 8,
  parameter DEPTH = 64,
  parameter FILE = "",
  parameter INIT = 0
)
(
  input  wire                 clock,
  input  wire [WIDTH-1:0]     data_0,
  input  wire [WIDTH-1:0]     data_1,
  input  wire [`CLOG2(DEPTH)-1:0] address_0,
  input  wire [`CLOG2(DEPTH)-1:0] address_1,
  input  wire                 wren_0,
  input  wire                 wren_1,
  output reg  [WIDTH-1:0]     q_0,
  output reg  [WIDTH-1:0]     q_1
);

  reg [WIDTH-1:0] mem [0:DEPTH-1] /* synthesis ramstyle = "M20K" */;

  integer file;
  integer scan;
  integer i;

  initial
    begin
      // read file contents if FILE is given
      if (FILE != "")
        $readmemb(FILE, mem);

      // set all data to 0 if INIT is true
      if (INIT)
        for (i = 0; i < DEPTH; i = i + 1)
          mem[i] = {WIDTH{1'b0}};
   end

  always @ (posedge clock)
  begin
    if (wren_0)
      begin
        mem[address_0] <= data_0;
        q_0 <= data_0;
      end
    else
      q_0 <= mem[address_0];
  end

  always @ (posedge clock)
  begin
    if (wren_1)
      begin
        mem[address_1] <= data_1;
        q_1 <= data_1;
      end
    else
      q_1 <= mem[address_1];
  end

endmodule

