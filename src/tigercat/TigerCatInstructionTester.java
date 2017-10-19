package tigercat;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import tigercat.instruction.Instruction;
import tigercat.instruction.InstructionArgumentCountException;
import tigercat.instruction.InstructionSyntaxError;
import tigercat.instruction.InvalidDataWidthException;
import tigercat.instruction.InvalidOpcodeException;
import tigercat.instruction.InvalidRegisterException;

public class TigerCatInstructionTester
{

  @Test
  public void testConvertIntToByteArray1()
  {
    int test = 0x55AA55AA;
    Byte[] toCheck = Instruction.convertIntToByteArray(test);
    
    Assert.assertArrayEquals(new Byte[]{0x55, (byte) 0xAA, 0x55, (byte) 0xAA}, toCheck);
  }

  @Test
  public void testConvertIntToByteArray2()
  {
    int test = 0xFEDCBA98;
    Byte[] toCheck = Instruction.convertIntToByteArray(test);
    
    Assert.assertArrayEquals(new Byte[]{(byte) 0xFE, (byte) 0xDC, (byte) 0xBA, (byte) 0x98}, toCheck);
  }

  @Test
  public void testCreateInstructionNoMap() throws InstructionArgumentCountException, InvalidOpcodeException, InstructionSyntaxError, InvalidRegisterException, InvalidDataWidthException
  {
    Instruction toCheck = Instruction.createInstruction("addd %arg1 %arg1 %arg1", null);
  }

  @Test
  public void testCreateInstructionWithMap() throws InstructionArgumentCountException, InvalidOpcodeException, InstructionSyntaxError, InvalidRegisterException, InvalidDataWidthException
  {
    Instruction toCheck = Instruction.createInstruction("addd %arg1 %arg1 %arg1", new HashMap<String, Label>());
  }

  @Test
  public void testGetSizeNoMap() throws InstructionArgumentCountException, InvalidOpcodeException, InstructionSyntaxError, InvalidRegisterException, InvalidDataWidthException
  {
    Instruction toCheck = Instruction.createInstruction("addd %arg1 %arg1 %arg1", null);
    Assert.assertEquals((Integer)2, toCheck.getSize());
  }

  @Test
  public void testGetSizeWithMap() throws InstructionArgumentCountException, InvalidOpcodeException, InstructionSyntaxError, InvalidRegisterException, InvalidDataWidthException
  {
    Instruction toCheck = Instruction.createInstruction("addd %arg1 %arg1 %arg1", new HashMap<String, Label>());
    Assert.assertEquals((Integer)2, toCheck.getSize());
  }

  @Test
  public void testGetMachineCodeWithMap() throws InstructionArgumentCountException, InvalidOpcodeException, InstructionSyntaxError, InvalidRegisterException, InvalidDataWidthException
  {
    Instruction toCheck = Instruction.createInstruction("addd %arg1 %arg1 %arg1", new HashMap<String, Label>());
    Assert.assertArrayEquals(new Byte[]{0x06, (byte) 0x92, 0x00, 0x00}, toCheck.getMachineCode());
  }

  @Test
  public void testGetADDCDMachineCode() throws InstructionArgumentCountException, InvalidOpcodeException, InstructionSyntaxError, InvalidRegisterException, InvalidDataWidthException
  {
    Instruction toCheck = Instruction.createInstruction("addcd %arg1 %arg1 %arg1", new HashMap<String, Label>());
    Assert.assertArrayEquals(new Byte[]{0x0E, (byte) 0x92, 0x00, 0x00}, toCheck.getMachineCode());
  }

  @Test
  public void testGetADDDMachineCode() throws InstructionArgumentCountException, InvalidOpcodeException, InstructionSyntaxError, InvalidRegisterException, InvalidDataWidthException
  {
    Instruction toCheck = Instruction.createInstruction("addd %arg1 %arg1 %arg1", new HashMap<String, Label>());
    Assert.assertArrayEquals(new Byte[]{0x06, (byte) 0x92, 0x00, 0x00}, toCheck.getMachineCode());
  }

  @Test
  public void testGetSUBCDMachineCode() throws InstructionArgumentCountException, InvalidOpcodeException, InstructionSyntaxError, InvalidRegisterException, InvalidDataWidthException
  {
    Instruction toCheck = Instruction.createInstruction("subcd %arg1 %arg1 %arg1", new HashMap<String, Label>());
    Assert.assertArrayEquals(new Byte[]{0x1E, (byte) 0x92, 0x00, 0x00}, toCheck.getMachineCode());
  }

  @Test
  public void testGetSUBDMachineCode() throws InstructionArgumentCountException, InvalidOpcodeException, InstructionSyntaxError, InvalidRegisterException, InvalidDataWidthException
  {
    Instruction toCheck = Instruction.createInstruction("subd %arg1 %arg1 %arg1", new HashMap<String, Label>());
    Assert.assertArrayEquals(new Byte[]{0x16, (byte) 0x92, 0x00, 0x00}, toCheck.getMachineCode());
  }

  @Test
  public void testGetMOVWMachineCode() throws InstructionArgumentCountException, InvalidOpcodeException, InstructionSyntaxError, InvalidRegisterException, InvalidDataWidthException
  {
    Instruction toCheck = Instruction.createInstruction("movw %a1l %r1l", new HashMap<String, Label>());
    Assert.assertArrayEquals(Instruction.createInstruction("addw %zero %a1l %r1l", new HashMap<String, Label>()).getMachineCode(), toCheck.getMachineCode());
  }

}
