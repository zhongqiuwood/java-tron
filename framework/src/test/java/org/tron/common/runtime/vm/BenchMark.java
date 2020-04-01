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
        //add
        opMap.put((byte)0x01, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x01, 0x60, 0x02, 0x56});
        //mul
        opMap.put((byte)0x02, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x02, 0x60, 0x02, 0x56});
        //byte: jumpdest, push2 0x1f00, push 1, byte, pop, push 0, jump
        opMap.put((byte)0x1a, new byte[]{0x5b, 0x61, 0x1f, 0x00, 0x60, 0x01, 0x1a, 0x50, 0x60, 0x00, 0x56});
        //not
        opMap.put((byte)0x19, new byte[]{0x60, 0x01, 0x5b, 0x19, 0x60, 0x02, 0x56});
        //xor
        opMap.put((byte)0x18, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0x18, 0x50, 0x60, 0x00, 0x56});
        //or
        opMap.put((byte)0x17, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0x17, 0x50, 0x60, 0x00, 0x56});
        //and
        opMap.put((byte)0x16, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0x16, 0x50, 0x60, 0x00, 0x56});
        //isZero
        opMap.put((byte)0x15, new byte[]{0x60, 0x1f, 0x5b, 0x15, 0x60, 0x02, 0x56});
        //eq
        opMap.put((byte)0X14, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0X14, 0x50, 0x60, 0x00, 0x56});
        //sgt
        opMap.put((byte)0X13, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0X13, 0x50, 0x60, 0x00, 0x56});
        //slt
        opMap.put((byte)0X12, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0X12, 0x50, 0x60, 0x00, 0x56});
        //gt
        opMap.put((byte)0X11, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0X11, 0x50, 0x60, 0x00, 0x56});
        //lt
        opMap.put((byte)0X10, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0X10, 0x50, 0x60, 0x00, 0x56});
        //SIGNEXTEND
        opMap.put((byte)0x0b, new byte[]{0x5b, 0x60, 0x70, 0x60, 0x0f, 0x0b, 0x50, 0x60, 0x00, 0x56});

        long billion = 1000000000L;
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
                System.out.println("op:" + OpCode.code(entry.getKey()) + " count:" + vm.count + " time:" + time);
            }
        }
    }

}
