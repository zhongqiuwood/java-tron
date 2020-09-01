package org.tron.common.runtime.vm;

import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.tron.common.parameter.CommonParameter;
import org.tron.common.runtime.InternalTransaction;
import org.tron.core.config.args.Args;
import org.tron.core.vm.OpCode;
import org.tron.core.vm.VM;
import org.tron.core.vm.program.Program;
import org.tron.core.vm.program.invoke.ProgramInvokeMockImpl;
import org.tron.protos.Protocol.Transaction;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
public class RefineBenchMarkTest {

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
    public void test() throws Exception {

        LinkedHashMap<Byte, byte[]> opMap = new LinkedHashMap<>();

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
        opMap.put((byte)0x0a, new byte[]{0x60, 0x03, 0x5b, 0x60, 0x0b, 0x0a, 0x60, 0x02, 0x56});
        //SIGNEXTEND
        opMap.put((byte)0x0b, new byte[]{0x60, 0x01, 0x5b, 0x60, 0x01, 0x0b, 0x60, 0x02, 0x56});
        //and
        opMap.put((byte)0x16, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0x16, 0x50, 0x60, 0x00, 0x56});
        //or
        opMap.put((byte)0x17, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0x17, 0x50, 0x60, 0x00, 0x56});
        //xor
        opMap.put((byte)0x18, new byte[]{0x5b, 0x60, 0x01, 0x60, 0x01, 0x18, 0x50, 0x60, 0x00, 0x56});

        long count = 500000;
        long billion = 1000000000L;
        long billion5 = 5000000000L;
        long billion10 = 10000000000L;
        long billion20 = 20000000000L;
        long billion40 = 40000000000L;

        String msg;
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("OriginalBenchMark.txt")) ;
        for (Map.Entry<Byte, byte[]> entry : opMap.entrySet()) {
            bufferedWriter.write("-------->" + OpCode.code(entry.getKey()) + "\n");
            for(int i = 1; i <= 10; i++) {
                VM vm = new VM();
                vm.targetOp = entry.getKey();
                invoke = new ProgramInvokeMockImpl();
                Transaction trx = Transaction.getDefaultInstance();
                InternalTransaction interTrx = new InternalTransaction(trx, InternalTransaction.TrxType.TRX_UNKNOWN_TYPE);
                program = new Program(entry.getValue(), invoke, interTrx);

                while (vm.count < count) {
                    vm.step(program);
                }

                double time = ((double) vm.timeAll) / vm.count;
                //System.out.println(String.format("\"%s(0x%02x)\"\t\t\t\t%d\t\t\t\t%f",
                //       OpCode.code(entry.getKey()),entry.getKey(), vm.count,time));
                msg = String.format("%d,%s(0x%02x),%d,%d,%f",
                        i, OpCode.code(entry.getKey()), entry.getKey(), vm.timeAll, vm.count,
                        time);
                bufferedWriter.write(msg + "\n");
            }
        }
        bufferedWriter.close();
    }
}
