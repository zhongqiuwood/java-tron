package org.tron.core.vm.nativecontract;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;

public interface IContractProcessor {

    boolean execute(Object result) throws ContractExeException;

    boolean validate() throws ContractValidateException;

    ByteString getOwnerAddress() throws InvalidProtocolBufferException;

    long calcFee();
}
