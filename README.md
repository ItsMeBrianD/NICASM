# NICASM-Required-Files
This branch contains all files that are required to use the NICASM Simulated Processors

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
3. Right click the memory object labled "MEM.HDD", and select "Load Image." Select the image you have assembled with BetterAssembler (Output file will be stored next to the original in a .nicp file)
4. Ensure Simulate>Ticks Enabled is enabled (Keyboard Shortcut CTRL+K)
5. Set Tick Speed to desired option Simulate>Tick Speed
6. Interact with program as needed (Keyboard input is required when the ASK indicator is green)

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
- Variable ($MyVarName)
  - Used to spare a headache, can be referred to by LD,LDI,LEA,ST,STI,and .FILL)
- Label (\*Label)
  - Used to name a line, makes BR much simpler
