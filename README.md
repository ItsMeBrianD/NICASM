# NICASM-Required-Files
This branch contains all files that are required to use the NICASM Simulated Processors

## Credits
Work on this project was primarily completed as follows:

Architecture Design : Nicholas Deckhut

Assembler Programming : Brian Donald

NICASM was loosely based on the LC3, with several changes made throughout its creation.

## Instructions
This program works in tandem with a software called Logisim, the executable can be found [here](logisim/logisim.exe).

#### Assembling a program
The software will run from the BetterAssembler class, and requires the following command line syntax:
```
  java site.projectname.Assembler [-debug] filename
```
Example .nic files can be found [in asmFiles](asmFiles)

#### Executing a Program
1. Open [logisim](logisim/logisim.exe)
2. Load [Computer.circ](logisim/Computer.circ).
3. Right click the memory object labeled "MEM.HDD", and select "Load Image." Select the image you have assembled with BetterAssembler (Output file will be stored next to the original in a .nicp file)
4. Ensure Simulate>Ticks Enabled is enabled (Keyboard Shortcut CTRL+K)
5. Set Tick Speed to desired option Simulate>Tick Speed
6. Turn the NIC on, power key is on the upper left
7. Interact with program as needed
   - Keyboard input is required when the ASK indicator is lit
   - Keyboard is a white bar located in the upper-right, near the Clock and LD.READ tags, click on it to start typing, simply click elsewhere to stop

## A Quick and Dirty intro to NICASM Syntax
Like other assembly-like languages, NICASM has very rudimentary syntax, consisting of at most 3 or 4 parts per line. The most common are:
- Command (ADD, AND, BR, JMP, etc.)
  - Declares which operation this line performs
  - Required on (nearly) every line.
- Register (R0,R1,R2,R3)
  - Refers to a value stored in a CPU Register
  - NICASM has 8 registers available (0-7)
- Immediate Value (#5,xF,etc.)
  - Refers to a direct value, limits vary based on command
- Variable ($MYVARNAME)
  - Used to spare a headache, can be referred to by LD,LDI,LEA,ST,STI,and .FILL)
- Label (\*Label)
  - Used to name a line, makes BR much simpler


#### A not as Quick and much Dirtier intro to NICASM Syntax
###### Commands
Glossary
> DR   = Destination Register
> SRX  = Source Register 0-7
> IMMX = X-bit Immediate Value (-1\*((2^x)/2-1) -> ((2^x)/2-1)) ), can be Decimal(#) or Hexadecimal(x) (Example, IMM5 can be \#-15 -> \#15)
> PC   = Current memory address of program
> PC'  = Next memory address of program (Most often PC+1)
```
ADD DR, SR1, [SR2|IMM5]
> Adds SR1 and [SR2|IMM5] and saves result in DR
> ADD R0 R0 R1
> ADD R0 R0 #15

AND DR, SR1, [SR2|IMM5]
> Bitwise and of SR1 and [SR2|IMM5] and saves result in DR
> AND R0 R0 R1
> AND R0 R0 xF

BR[n][z][p] [Label|IMM8]
> Jumps execution to line specified by Label, or line offset specified by IMM8
> n jumps if value is negative, z jumps if value is zero, p jumps if value is positive, order matters here, but they are all optional
> For Immediate Value the new PC will be PC'+IMM8
> BRn .Main
> BRp #-15 \*Goes back 14 Lines (-15+PC' is the same as -14+PC)\*

JMP BR
> Always jumps program to line specified in BR (Base Register)
> JMP R7 \*This command is identical to RET\*

JSR Label
> Jumps program to Label, used to create functions
> Stores current address in R7
> JSR .Main

JSRR BR
> Identical to JSR, uses Register for offset rather than label
> Stores current address in R7
> JSRR R0

LD DR, [Variable|IMM8]
> Loads value stored at variable or immediate value into Destination Register
> LD R0 $MyVarName
> LD R0 #127

LDI DR, Variable
> Treats variable as pointer and loads that value into DR
> LDI R0 $MyVarName

LDR DR, SR, IMM6
> Loads value at MEM[SR+IMM6] into DR
> LDR R0 R5 #-1

LEA DR, [Variable|IMM8]
> Loads value of PC'+IMM8 or Variable into DR
> LEA R0 #-100

NOT DR, SR
> Bitwise not of SR into DR
NOT R0 R0

RET
> Jumps program to address in R7, if used with JSR and JSRR it can form rudimentary functions
> RET

ST DR, [Variable|IMM8]
> Stores value of DR in variable, or MEM[PC'+IMM8]
> ST R0 $MyVarName
> ST R1 xF3

STI DR, Variable
> Treats variable as pointer and stores the value of DR there
> i.e. if $Pointer = x0001, and R0 = #10, then
> STI R0 $Pointer
> Would make MEM[x0001] = #10

STR DR, SR, IMM6
> Stores value in DR at MEM[SR+IMM6]
> STR R0 R1 #6

TRAP
> Not used

.FILL
> Fills the line with a IMM16, most commonly used for variables

.BLK
> Blocks of a portion of lines as empty, good for creating space for array variables
> Shouldn't be used with large values, chunk into multiple smaller .BLKs

READ DR
> Gets a value from the user, and stores it in the given register

PRINT SR
> Prints the character in the given register
```
###### Shorthand commands

These commands are given as a way to make common tasks much easier, because these add new lines, it is #extremely# recommended to use variables over offset values when using them in your program.
This is not a comprehensive list, the stack-specific shorthand commands have been left out, simply because they are not working properly at this time.
```
.SUB DR, SR1, SR2
> Subtracts SR2 from SR1 and stores into DR

.MUL DR, SR1, [SR2|IMM5]
> Multiplies SR1 by SR2|IMM5 and stores in DR

.SET DR, [IMM16|Variable]
> Sets DR to the IMM16 or Variable value

.COPY DR, SR1
> Copies value from SR1 into DR
> Equivalent to ADD DR, SR1, #0, but makes code easier to read

.ZERO DR
> Sets DR to 0
> Equivalent to AND DR,DR,#0, but makes code easier to read

.MAIN
> Defines entry point of program

.PRINTS [Variable|IMM16]
> Prints a string, stops upon reaching a negative value

.READS IMM16
> Reads a string, stops reading on Return Key
> String gets stored at IMM16
```
