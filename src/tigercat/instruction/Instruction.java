/*
 * Author       : Team TigerCat
 * Date         : 16 October 2017
 * Filename     : Instruction.java
 * Description  : Helper class for converting assembly string lines to machine code
 */

package tigercat.instruction;

import java.util.HashMap;

import tigercat.Label;

/**
 * Helper class for converting assembly string lines to machine code
 *
 */
public abstract class Instruction
{

  /**
   * Every instruction is four bytes (32-bits, two words)
   */
  static final int BYTES_PER_INSTRUCTION = 4;

  // Size Definitions (in bits)
  static final int SIZEOF_INSTRUCTION               = 32;
  static final int SIZEOF_OPCODE                    = 5;
  static final int SIZEOF_SIZE_FLAG                 = 1;
  static final int SIZEOF_TYPE_FLAG                 = 1;
  static final int SIZEOF_SINGLE_WORD_REG_ENCODING  = 4;
  static final int SIZEOF_DOUBLE_WORD_REG_ENCODING  = 3;

  static final int SIZEOF_WORD                      = 16;
  static final int SIZEOF_BYTE                      = 8;
  
  // Bitshift Definitions
  static final int SHIFT_OPCODE     = SIZEOF_INSTRUCTION - SIZEOF_OPCODE;
  static final int SHIFT_SIZE_FLAG  = SHIFT_OPCODE - SIZEOF_SIZE_FLAG;
  static final int SHIFT_TYPE_FLAG  = SHIFT_SIZE_FLAG - SIZEOF_TYPE_FLAG;
  
  // Used to avoid magic numbers, at least a bit
  static final int THREE_ARGUMENTS = 3;
  static final int TWO_ARGUMENTS = 2;
  static final int ONE_ARGUMENT = 1;
  static final int ZERO_ARGUMENTS = 0;
  
  // What tokens the assembly expects
  public static final String REGISTER_PREFIX = "%";
  public static final String IMMEDIATE_PREFIX = "$";

  /**
   * Record whether an instruction operates on single-word or double-word data
   */
  protected enum DataWidth
  {
    SINGLE_WORD(0), DOUBLE_WORD(1);

    protected int flag;

    DataWidth(int flag)
    {
      this.flag = flag;
    }

    public int getFlag()
    {
      return flag;
    }
  }

  /**
   * Record whether an instruction takes an immediate argument or not
   */
  protected enum DataType
  {
    IMMEDIATE(0), REGISTER(1);

    protected int flag;

    DataType(int flag)
    {
      this.flag = flag;
    }

    public int getFlag()
    {
      return flag;
    }
  }

  protected int opcode_encoding;
  protected DataWidth dataWidth;
  protected DataType instructionType;
  protected Argument[] arguments;
  
  protected int machineCode;

  /**
   * Return the machine code representation of this instruction
   * 
   * For the default implementation, it is expected that machineCode already contains the opcode
   * All other portions filled in based on local variables!
   * 
   * @return Machine code representation of this instruction
   */
  public Byte[] getMachineCode()
  {
    assert arguments != null : "Instruction defined with no labelMapping. Cannot get machine code.";
    int index;

    this.machineCode |= opcode_encoding << SHIFT_OPCODE;
    
    this.machineCode |= dataWidth.flag << SHIFT_SIZE_FLAG;
    
    this.machineCode |= instructionType.flag << SHIFT_TYPE_FLAG;
    
    int shiftDistance = SHIFT_TYPE_FLAG;
    
    // Loop over all the register arguments and encode them
    for (index = 0; index < arguments.length - 1; index ++)
    {
      assert arguments[index].getArgumentType() == DataType.REGISTER : "Expected register argument";
      
      shiftDistance -= arguments[index].getEncodingSize();
      this.machineCode |= arguments[index].getMachineCodeRepresentation() << shiftDistance;
    }
    
    switch(arguments[index].argumentType)
    {
    case REGISTER:
      // Shift in the register, as normal
      shiftDistance -= arguments[index].getEncodingSize();
      this.machineCode |= arguments[index].getMachineCodeRepresentation() << shiftDistance;
      break;
    case IMMEDIATE:
      // TODO: Handle encoding immediate value
      assert false : "Encoding immediates not implemented";
      break;
    }
    
    return convertIntToByteArray(this.machineCode);
  }

  /**
   * Return a byte array representing the number. Converts a number from java's internal
   * form to a byte array for use in the assembled program.
   *
   * @param input The number to convert to bytes
   * @return A little-endian byte-array representation of the number //todo: verify
   */
  public static Byte[] convertIntToByteArray(int input)
  {
    Byte[] toReturn = new Byte[4];
    int mask = 0xFF000000;

    // Takes each byte in turn of the input and stores it into the toReturn array
    for (int index = 0; index < toReturn.length; index++)
    {
      toReturn[index] = (byte) ((input & mask) >> (8 * (toReturn.length - 1 - index)));
      mask = mask >>> 8; //logical right shift
    }

    return toReturn;
  }

  /**
   * Return the size, in words, of this instruction Currently all individual instructions
   * are two words (32-bits), but pseudo instructions may be longer
   * 
   * @return The size of this instruction in machine words
   */
  public Integer getSize()
  {
    return 2;
  }

  /**
   * Converts the given line of assembly into an Instruction object
   * 
   * @param line Line of assembly which corresponds to a machine instruction
   * @return The newly-created instruction
   * @throws InvalidRegisterException 
   * @throws InstructionSyntaxError 
   * @throws InvalidOpcodeException 
   * @throws InstructionArgumentCountException 
   * @throws InvalidDataWidthException 
   */
  public static Instruction createInstruction(String line, HashMap<String, Label> labelMapping)
      throws InstructionArgumentCountException,
      InvalidOpcodeException,
      InstructionSyntaxError,
      InvalidRegisterException,
      InvalidDataWidthException
  {
    String[] tokens = line.split("\\s+");
    String opcode = tokens[0];
    
    if (opcode.startsWith("add"))
    {
      return new AddInstruction(tokens, labelMapping);
    }
    if (opcode.startsWith("sub"))
    {
      return new SubInstruction(tokens, labelMapping);
    }
    if (opcode.startsWith("mov"))
    {
      return new MoveInstruction(tokens, labelMapping);
    }

    throw new InvalidOpcodeException("Unable to create instruction from: " + line);
  }
  
  /**
   * Create an Instruction from the given string
   * 
   * An Instruction may be created with null labelMapping, in which case only getSize() is defined
   * 
   * If labelMapping is defined, this function must instantiate in all class-level variables and
   * the top five bits of the top byte of machineCode should be set to the opcode
   * 
   * @param tokens The instruction to create
   * @param labelMapping Resolve labels to addresses
   * @throws InvalidDataWidthException If the instruction specifies an unrecognized data width
   * @throws InstructionSyntaxError 
   * @throws InstructionArgumentCountException 
   * @throws InvalidOpcodeException 
   * @throws InvalidRegisterException 
   */
  protected Instruction(String[] tokens, HashMap<String, Label> labelMapping, int opcode_encoding, int num_args)
      throws InvalidDataWidthException, InstructionSyntaxError, InstructionArgumentCountException, InvalidOpcodeException, InvalidRegisterException
  {
    this.machineCode = 0;
    this.arguments = new Argument[num_args];
    
    if (tokens.length < num_args + 1)
    {
      throw new InstructionArgumentCountException();
    }
    
    checkInstructionSyntax(tokens);

    String opcode = tokens[0];
    String last_arg = tokens[num_args];

    // Add the data-width flag to the machine code
    if (opcode.endsWith("w"))
    {
      this.dataWidth = DataWidth.SINGLE_WORD;
    } else if (opcode.endsWith("d"))
    {
      this.dataWidth = DataWidth.DOUBLE_WORD;
    } else
    {
      throw new InvalidDataWidthException(opcode);
    }
    
    this.opcode_encoding = opcode_encoding;

    // Decide whether we are using immediate data or not
    // The only argument which can validly be immediate is the last one,
    // and the syntax check has already said the instruction is valid
    if (last_arg.startsWith(IMMEDIATE_PREFIX))
    {
      this.instructionType = DataType.IMMEDIATE;
    } else if (last_arg.startsWith(REGISTER_PREFIX))
    {
      this.instructionType = DataType.REGISTER;
    } else
    {
      // TODO: Handle label lookup
      throw new InstructionSyntaxError("Undefined prefix on " + last_arg);
    }
    
    // All but the last argument are certainly registers
    for (int index = 0; index < num_args - 1; index++)
    {
      arguments[index] = new Argument(tokens[index + 1].substring(1), this.dataWidth, DataType.REGISTER);
    }
    // The last argument may be an immediate, depending on the type of instruction
    arguments[num_args - 1] = new Argument(last_arg.substring(1), this.dataWidth, this.instructionType);

  }

  /**
   * Checks a given instruction against design invariants, throwing an exception if the
   * requirements are not met
   *
   * @param tokens The instruction to check
   * @throws InstructionSyntaxError Triggers if an immediate was found where we weren't expecting one
   * or a token we could not classify was found.
   */
  //todo: move into instruction constructor
  protected static void checkInstructionSyntax(String[] tokens)
      throws InstructionSyntaxError
  {
    String opcode = tokens[0];
    
    // Check that the remaining arguments are either:
    //   A register
    //   An immediate and the last argument
    for (int index = 1; index < tokens.length; index ++)
    {
      if (tokens[index].startsWith(REGISTER_PREFIX))
      {
        // No problem. This may be a register.
        continue;
      }
      
      if (tokens[index].startsWith(IMMEDIATE_PREFIX))
      {
        if (index == tokens.length - 1)
        {
          // No problem. This is the last argument and may be an immediate
          continue;
        }
        else 
        {
          throw new InstructionSyntaxError("Immediate encountered at non-end-of-line");
        }
      }
      
      // TODO: Handle label lookup
      throw new InstructionSyntaxError("Invalid token encountered: " + tokens[index]);
    }
  }

  protected class Argument
  {
    protected static final String ZERO_REG = "zero";
    
    protected int machineCodeRepresentation;
    protected DataType argumentType;
    protected int size;
    
    public DataType getArgumentType()
    {
      return argumentType;
    }
    
    public int getMachineCodeRepresentation()
    {
      return machineCodeRepresentation;
    }
    
    /**
     * Returns the size of the encoding of this argument
     * 
     * @return The size of this argument
     */
    public int getEncodingSize()
    {
      assert argumentType == DataType.REGISTER : "getEncodingSize undefined for non-register arguments";
      return size;
    }

    public Argument(String argument, DataWidth dataWidth, DataType argumentType) throws InvalidRegisterException
    {
      
      this.argumentType = argumentType;
      if (argumentType == DataType.IMMEDIATE)
      {
        machineCodeRepresentation = this.parseImmediate(argument);
      } else if (argumentType == DataType.REGISTER)
      {
        machineCodeRepresentation = this.parseRegister(argument, dataWidth);
        switch(dataWidth)
        {
        case SINGLE_WORD:
          size = SIZEOF_SINGLE_WORD_REG_ENCODING;
          break;
        case DOUBLE_WORD:
          size = SIZEOF_DOUBLE_WORD_REG_ENCODING;
          break;
        }
      } else
      {
        throw new RuntimeException("Undefined Instruction Data Type");
      }
    }

    /**
     * Convert a register to its machine code representation
     * 
     * @param argument
     *          A string containing a register
     * @param dataWidth
     *          Whether a single- or double-word register should be encoded
     * @return The machine code encoding of the register
     * @throws InvalidRegisterException
     *           If an undefined register is encountered
     */
    protected int parseRegister(String argument, DataWidth dataWidth) throws InvalidRegisterException
    {
      if (dataWidth == DataWidth.SINGLE_WORD)
      {
        switch (argument)
        {
        case Argument.ZERO_REG: //todo: magic numbers are evil (put this into a lookup xml file)
          return 0xF;
        case "r1l":
          return 0x0;
        case "r2l":
          return 0x1;
        case "a1l":
          return 0x2;
        default:
          throw new InvalidRegisterException(argument);
        }
      } else if (dataWidth == DataWidth.DOUBLE_WORD)
      {
        switch (argument)
        {
        case "ret1":
          return 0x0;
        case "ret2":
          return 0x1;
        case "arg1":
          return 0x2;
        default:
          throw new InvalidRegisterException(argument);
        }
      } else
      {
        throw new RuntimeException("Undefined Data Width");
      }
    }

    /**
     * Convert an immediate value into a byte array The return value is always
     * 32-bits, even though no immediate can legally have that length
     * 
     * @param argument
     *          A string containing a hexadecimal immediate value, including 0x prefix
     * @return Machine Code representation of the immediate value
     */
    protected int parseImmediate(String argument)
    {
      // Strip 0x prefix
      argument = argument.substring(2);
      return Integer.parseInt(argument, 16);
    }
  }
}
