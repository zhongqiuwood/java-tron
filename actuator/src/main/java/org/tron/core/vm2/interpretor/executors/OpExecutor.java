package org.tron.core.vm2.interpretor.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.core.vm2.ContractContext;
import org.tron.core.vm2.interpretor.Op;

public interface OpExecutor {

  Logger logger = LoggerFactory.getLogger("VM2");

  void exec(Op op, ContractContext context);



}
