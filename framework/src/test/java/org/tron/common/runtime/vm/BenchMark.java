package org.tron.common.runtime.vm;

import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.tron.common.parameter.CommonParameter;
import org.tron.common.runtime.InternalTransaction;
import org.tron.core.config.args.Args;
import org.tron.core.vm.OpCode;
import org.tron.core.vm.VM;
import org.tron.core.vm.program.Program;
import org.tron.core.vm.program.Stack;
import org.tron.core.vm.program.invoke.ProgramInvokeMockImpl;
import org.tron.protos.Protocol.Transaction;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
public class BenchMark {

    private ProgramInvokeMockImpl invoke;
    private Program program;

    @BeforeClass
    public static void init() {
        CommonParameter.getInstance().setDebug(true);
    }

    @AfterClass
    public static void destroy() {
        Args.clearParam();
    }

    @Test
    public void testADD() throws Exception {
        VM vm = new VM();
        invoke = new ProgramInvokeMockImpl();
        //byte[] op = {0x5b, 0x60, 0x01, 0x60, 0x01, 0x01, 0x50, 0x60, 0x02, 0x56};
        byte[] op = {0x60, 0x01, 0x5b, 0x60, 0x01, 0x01,
                0x60, 0x02, 0x56};
        //push 1, jumpdest, push 1, add, push 2, jump
        Transaction trx = Transaction.getDefaultInstance();

        InternalTransaction interTrx = new InternalTransaction(trx, InternalTransaction.TrxType.TRX_UNKNOWN_TYPE);
        program = new Program(op, invoke, interTrx);

        //!program.isStopped() &&
        while (vm.timeAll < 1000000000L) {
            vm.step(program);
        }
        Field privateStringField = Program.class.
                getDeclaredField("stack");
        privateStringField.setAccessible(true);//允许访问私有字段
        Stack fieldValue = ((Stack) privateStringField.get(program));//获得私有字段值

        System.out.println("count:"+fieldValue.pop()+" time:"+vm.timeAll);
        //program.stack.elementData
    }

    @Test
    public void testADDByCountTime() throws Exception {
        VM vm = new VM();
        invoke = new ProgramInvokeMockImpl();
        byte[] op = {0x60, 0x01, 0x5b, 0x60, 0x01, 0x01,
                0x60, 0x02, 0x56};
        //push 1, jumpdest, push 1, add, push 2, jump
        Transaction trx = Transaction.getDefaultInstance();

        InternalTransaction interTrx = new InternalTransaction(trx, InternalTransaction.TrxType.TRX_UNKNOWN_TYPE);
        program = new Program(op, invoke, interTrx);

        while (vm.timeAll < 1000000000L) {
            vm.step(program);
        }

//        long count = fieldValue.pop().longValue();
        double time = (double)vm.timeAll / vm.count;
        System.out.println("count:"+vm.count+" time:"+time);
    }

    @Test
    public void test() throws Exception {

        LinkedHashMap<Byte, byte[]> opMap = new LinkedHashMap<>();
        //stop
        opMap.put((byte)0x00, new byte[]{0x5b, 0x00, 0x60, 0x00, 0x56});
        //add
        opMap.put((byte)0x01, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x01, 0x60, 0x02, 0x56});
        //mul
        opMap.put((byte)0x02, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x02, 0x60, 0x02, 0x56});
        //sub
        opMap.put((byte)0x03, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x03, 0x60, 0x02, 0x56});
        //div
        opMap.put((byte)0x04, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x04, 0x60, 0x02, 0x56});
        //sdiv
        opMap.put((byte)0x05, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x05, 0x60, 0x02, 0x56});
        //mod
        opMap.put((byte)0x06, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x06, 0x60, 0x02, 0x56});
        //smod
        opMap.put((byte)0x07, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x07, 0x60, 0x02, 0x56});
        //addmod
        opMap.put((byte)0x08, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x60, 0x01, 0x08, 0x60, 0x02, 0x56});
        //mulmod
        opMap.put((byte)0x09, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x60, 0x01, 0x09, 0x60, 0x02, 0x56});
        //exp
        opMap.put((byte)0x0a, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x0a, 0x60, 0x02, 0x56});
        //SIGNEXTEND
        opMap.put((byte)0x0b, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x0b, 0x60, 0x02, 0x56});
        //lt
        opMap.put((byte)0X10, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0X10, 0x50, 0x60, 0x00, 0x56});
        //gt
        opMap.put((byte)0X11, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0X11, 0x50, 0x60, 0x00, 0x56});
        //slt
        opMap.put((byte)0X12, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0X12, 0x50, 0x60, 0x00, 0x56});
        //sgt
        opMap.put((byte)0X13, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0X13, 0x50, 0x60, 0x00, 0x56});
        //eq
        opMap.put((byte)0X14, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0X14, 0x50, 0x60, 0x00, 0x56});
        //isZero
        opMap.put((byte)0x15, new byte[]{0x60, 0x1f, 0x5b, 0x15, 0x60, 0x02, 0x56});
        //and
        opMap.put((byte)0x16, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0x16, 0x50, 0x60, 0x00, 0x56});
        //or
        opMap.put((byte)0x17, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0x17, 0x50, 0x60, 0x00, 0x56});
        //xor
        opMap.put((byte)0x18, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0x18, 0x50, 0x60, 0x00, 0x56});
        //not
        opMap.put((byte)0x19, new byte[]{0x60, 0x01, 0x5b, 0x19, 0x60, 0x02, 0x56});
        //byte
        opMap.put((byte)0x1a, new byte[]{0x5b, 0x61, 0x1f, 0x00, 0x60, 0x01, 0x1a, 0x50, 0x60, 0x00, 0x56});
        //SHL(0x1b)
        opMap.put((byte)0x1b, new byte[]{0x5b, 0x61, 0x1f, 0x00, 0x60, 0x01, 0x1b, 0x50, 0x60, 0x00, 0x56});
        //SHR(0x1c)
        opMap.put((byte)0x1c, new byte[]{0x5b, 0x61, 0x1f, 0x00, 0x60, 0x01, 0x1c, 0x50, 0x60, 0x00, 0x56});
        //SAR(0x1d)
        opMap.put((byte)0x1d, new byte[]{0x5b, 0x61, 0x1f, 0x00, 0x60, 0x01, 0x1d, 0x50, 0x60, 0x00, 0x56});
                /*Cryptographic Operations*/
        //SHA3(0x20)
                /*Environmental Information */
        //ADDRESS(0x30)
        //BALANCE(0x31)
        //ORIGIN(0x32)
        //CALLER(0x33)
        //CALLVALUE(0x34)
        //CALLDATALOAD(0x35)
        //CALLDATASIZE(0x36)
        //CALLDATACOPY(0x37)
        //CODESIZE(0x38)
        //CODECOPY(0x39)
        //RETURNDATASIZE(0x3d)
        //RETURNDATACOPY(0x3e)
        //GASPRICE(0x3a)
        //EXTCODESIZE(0x3b)
        //EXTCODECOPY(0x3c)
        //EXTCODEHASH(0x3f)
                /*Block Information */
        //BLOCKHASH(0x40)
        //COINBASE(0x41)
        //TIMESTAMP(0x42)
        //NUMBER(0x43)
        //DIFFICULTY(0x44)
        //GASLIMIT(0x45)
                /*Memory, Storage and Flow Operations */
        //POP(0x50)
        //MLOAD(0x51)
        //MSTORE(0x52)
        //MSTORE8(0x53)
        //SLOAD(0x54)
        //SSTORE(0x55)
        //JUMP(0x56)
        //JUMPI(0x57)
        //PC(0x58)
        //MSIZE(0x59)
        //GAS(0x5a)
        //JUMPDEST(0x5b)
                /*Push Operations */
        //PUSH1(0x60)
        //PUSH2(0x61)
        //PUSH3(0x62)
        //PUSH4(0x63)
        //PUSH5(0x64)
        //PUSH6(0x65)
        //PUSH7(0x66)
        //PUSH8(0x67)
        //PUSH9(0x68)
        //PUSH10(0x69)
        //PUSH11(0x6a)
        //PUSH12(0x6b)
        //PUSH13(0x6c)
        //PUSH14(0x6d)
        //PUSH15(0x6e)
        //PUSH16(0x6f)
        //PUSH17(0x70)
        //PUSH18(0x71)
        //PUSH19(0x72)
        //PUSH20(0x73)
        //PUSH21(0x74)
        //PUSH22(0x75)
        //PUSH23(0x76)
        //PUSH24(0x77)
        //PUSH25(0x78)
        //PUSH26(0x79)
        //PUSH27(0x7a)
        //PUSH28(0x7b)
        //PUSH29(0x7c)
        //PUSH30(0x7d)
        //PUSH31(0x7e)
        //PUSH32(0x7f)
                /*Duplicate Nth item from the stack */
        //DUP1(0x80)
        //DUP2(0x81)
        //DUP3(0x82)
        //DUP4(0x83)
        //DUP5(0x84)
        //DUP6(0x85)
        //DUP7(0x86)
        //DUP8(0x87)
        //DUP9(0x88)
        //DUP10(0x89)
        //DUP11(0x8a)
        //DUP12(0x8b)
        //DUP13(0x8c)
        //DUP14(0x8d)
        //DUP15(0x8e)
        //DUP16(0x8f)
                /*Swap the Nth item from the stack with the top */
        //SWAP1(0x90)
        //SWAP2(0x91)
        //SWAP3(0x92)
        //SWAP4(0x93)
        //SWAP5(0x94)
        //SWAP6(0x95)
        //SWAP7(0x96)
        //SWAP8(0x97)
        //SWAP9(0x98)
        //SWAP10(0x99)
        //SWAP11(0x9a)
        //SWAP12(0x9b)
        //SWAP13(0x9c)
        //SWAP14(0x9d)
        //SWAP15(0x9e)
        //SWAP16(0x9f)
        //LOG0(0xa0)
        //LOG1(0xa1)
        //LOG2(0xa2)
        //LOG3(0xa3)
        //LOG4(0xa4)
                /*System operations */
        //CALLTOKEN(0xd0)
        //TOKENBALANCE(0xd1)
        //CALLTOKENVALUE(0xd2)
        //CALLTOKENID(0xd3)
        //ISCONTRACT(0xd4)
        //CREATE(0xf0)
        //CALL(0xf1)
        //CALLCODE(0xf2)
        //RETURN(0xf3)
        //DELEGATECALL(0xf4)
        //CREATE2(0xf5)
        //STATICCALL(0xfa)
        //REVERT(0xfd)
        //SUICIDE(0xff)

        long billion = 1000000000L;
        long billion5 = 5000000000L;
        long billion10 = 10000000000L;
        long billion20 = 20000000000L;
        long billion40 = 40000000000L;

        for(int i = 1; i <= 1; i++) {
            System.out.println("第"+i+"次");
            for (Map.Entry<Byte, byte[]> entry : opMap.entrySet()) {
                VM vm = new VM();
                vm.targetOp = entry.getKey();
                invoke = new ProgramInvokeMockImpl();
                Transaction trx = Transaction.getDefaultInstance();
                InternalTransaction interTrx = new InternalTransaction(trx, InternalTransaction.TrxType.TRX_UNKNOWN_TYPE);
                program = new Program(entry.getValue(), invoke, interTrx);

                while (vm.timeAll < billion) {
                    vm.step(program);
                }

                double time = ((double) vm.timeAll) / vm.count;
                System.out.println(String.format("\"%s(0x%02x)\"\t\t\t\t%d\t\t\t\t%f",OpCode.code(entry.getKey()),entry.getKey(), vm.count,time));
            }
        }
    }

}
