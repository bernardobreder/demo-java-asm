import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

public class MainTest extends Main {

  @Test
  public void test() throws IOException, InterruptedException {
    executeAsmCode("cmp %rcx, %rax");
    executeAsmCode("cmp %rbx, %rax");
    executeAsmCode("cmp %rax, %rax");
    executeAsmCode("movq $1, %rax");
    executeAsmCode("movq $0xffff, %rax");
    executeAsmCode("movl $1, %eax");
    executeAsmCode("mov $1, %al");
    executeAsmCode("movq $1, %rbx");
    executeAsmCode("movl $1, %ebx");
    executeAsmCode("mov $1, %bl");
    executeAsmCode("movq $1, %rcx");
    executeAsmCode("movl $1, %ecx");
    executeAsmCode("mov $1, %cl");
    executeAsmCode("cmp %ecx, %eax");
    executeAsmCode("cmp %ebx, %eax");
    executeAsmCode("jne 1");
    executeAsmCode("je 1");
    executeAsmCode("sete %al");
    executeAsmCode("movzbl %al, %eax");
    executeAsmCode("movzbq %al, %rax");
    Assert.assertEquals(1, new Asm().mov_i32_a64(1).execute());
    Assert.assertEquals(1, new Asm().mov_i32_c64(1).mov_c64_a64().execute());
    Assert.assertEquals(2, new Asm().mov_i32_a64(1).mov_i32_c64(2)
      .mov_c64_a64().execute());
  }

  @Test
  public void mov() throws IOException, InterruptedException {
    Assert.assertEquals(1, asmAsmCodes("movq $1, %rax"));
    Assert.assertEquals(0xFF, asmAsmCodes("movq $255, %rax"));
    Assert.assertEquals(0xFF, asmAsmCodes("movl $255, %eax"));
    Assert.assertEquals(0xFF, asmAsmCodes("mov $255, %al"));
    Assert.assertEquals(81, asmAsmCodes("movq $43981, %rax",
      "movq $43900, %rcx", "subq %rcx, %rax"));
    Assert.assertEquals(81, asmAsmCodes("movl $43981, %eax",
      "movl $43900, %ecx", "subl %ecx, %eax"));
    Assert.assertEquals(1, asmAsmCodes("movq $1, %rcx", "movq %rcx, %rax"));
  }

  @Test
  public void jmp() throws IOException, InterruptedException {
    Assert.assertEquals(1, asmAsmCodes("movq $1, %rax", "movq $1, %rcx",
      "cmpq %rcx, %rax", "je 0x7", "movq $2, %rax"));
    Assert.assertEquals(2, asmAsmCodes("movq $1, %rax", "movq $1, %rcx",
      "cmpq %rcx, %rax", "jne 0x7", "movq $2, %rax"));
    Assert.assertEquals(1, asmAsmCodes("mov $1, %al", "mov $1, %cl",
      "cmp %cl, %al", "je 0x2", "mov $2, %al"));
  }

  @Test
  public void add() throws IOException, InterruptedException {
    Assert.assertEquals(3, asmAsmCodes("movq $1, %rax", "movq $2, %rcx",
      "addq %rcx, %rax"));
    Assert.assertEquals(3, asmAsmCodes("movl $1, %eax", "movl $2, %ecx",
      "addl %ecx, %eax"));
    Assert.assertEquals(3, asmAsmCodes("mov $1, %al", "mov $2, %cl",
      "add %cl, %al"));
  }

  @Test
  public void sub() throws IOException, InterruptedException {
    Assert.assertEquals(2, asmAsmCodes("movq $5, %rax", "movq $3, %rcx",
      "subq %rcx, %rax"));
    Assert.assertEquals(2, asmAsmCodes("movl $5, %eax", "movl $3, %ecx",
      "subl %ecx, %eax"));
    Assert.assertEquals(2, asmAsmCodes("mov $5, %al", "mov $3, %cl",
      "sub %cl, %al"));
  }

  @Test
  public void mul() throws IOException, InterruptedException {
    Assert.assertEquals(15, asmAsmCodes("movq $5, %rax", "movq $3, %rcx",
      "imulq %rcx, %rax"));
    Assert.assertEquals(15, asmAsmCodes("movl $5, %eax", "movl $3, %ecx",
      "imull %ecx, %eax"));
    Assert.assertEquals(15, asmAsmCodes("mov $5, %al", "mov $3, %cl",
      "mulb %cl"));
  }

  @Test
  public void div() throws IOException, InterruptedException {
    Assert.assertEquals(5, asmAsmCodes("movq $10, %rax", "movq $2, %rcx",
      "divq %rcx, %rax"));
    Assert.assertEquals(5, asmAsmCodes("movl $10, %eax", "movl $2, %ecx",
      "divl %ecx, %eax"));
    Assert.assertEquals(5, asmAsmCodes("mov $10, %eax", "mov $2, %cl",
      "div %cl"));
  }

  @Test
  public void bool() throws IOException, InterruptedException {
    Assert.assertEquals(0, asmAsmCodes("movq $10, %rax", "movq $2, %rcx",
      "sete %al"));
    Assert.assertEquals(0, asmAsmCodes("movq $10, %rax", "movq $10, %rcx",
      "sete %al"));
    Assert.assertEquals(0, asmAsmCodes("movq $10, %rax", "movq $10, %rcx",
      "sete %al", "movzbq %al, %rax"));
  }

  public static void main(String[] args) {
    System.out.println(BigInteger.probablePrime(2048, new Random(System
      .currentTimeMillis())));
    System.out.println("---");
    System.out.println(BigInteger.probablePrime(2048, new Random(System
      .currentTimeMillis())));
  }

}
