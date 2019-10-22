package org.tron.core.vm2.interpretor;

import java.math.BigInteger;
import org.tron.common.runtime.vm.DataWord;
import org.tron.core.vm2.ExceptionFactory;
import org.tron.core.vm2.VMConstant;

public class MemUtils {

  public static long calcMemEnergy(long oldMemSize, BigInteger newMemSize,
      long copySize, Op op) {
    long energyCost = 0;

    checkMemorySize(op, newMemSize);

    // memory drop consume calc
    long memoryUsage = (newMemSize.longValueExact() + 31) / 32 * 32;
    if (memoryUsage > oldMemSize) {
      long memWords = (memoryUsage / 32);
      long memWordsOld = (oldMemSize / 32);
      long memEnergy = (Costs.MEMORY * memWords + memWords * memWords / 512)
          - (Costs.MEMORY * memWordsOld + memWordsOld * memWordsOld / 512);
      energyCost += memEnergy;
    }

    if (copySize > 0) {
      long copyEnergy = Costs.COPY_ENERGY * ((copySize + 31) / 32);
      energyCost += copyEnergy;
    }
    return energyCost;
  }

  public static void checkMemorySize(Op op, BigInteger newMemSize) {
    if (newMemSize.compareTo(VMConstant.MEM_LIMIT) > 0) {
      throw ExceptionFactory.memoryOverflow(op.name());
    }
  }

  /**
   * Utility to calculate new total memory size needed for an operation. <br/> Basically just offset
   * + size, unless size is 0, in which case the result is also 0.
   *
   * @param offset starting position of the memory
   * @param size number of bytes needed
   * @return offset + size, unless size is 0. In that case memNeeded is also 0.
   */
  public static BigInteger memNeeded(DataWord offset, DataWord size) {
    return size.isZero() ? BigInteger.ZERO : offset.value().add(size.value());
  }

}
