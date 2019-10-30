package org.tron.core.vm2.interpretor;

public class OpConvertor {

  public static Op getOp(byte b) {
    return Op.code(b);
  }
}
