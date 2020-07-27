package org.tron.core.vm.nativecontract;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;

public interface IContractProcessor {

    boolean execute(Object contract) throws ContractExeException;

    boolean validate(Object contract) throws ContractValidateException;

    ByteString getOwnerAddress() throws InvalidProtocolBufferException;

    long calcFee();
}
