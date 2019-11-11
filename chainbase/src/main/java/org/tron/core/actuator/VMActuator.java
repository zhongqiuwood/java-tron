package org.tron.core.actuator;

import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;

public interface VMActuator {

  void execute(Object object) throws ContractExeException;

  void validate(Object object) throws ContractValidateException;
}