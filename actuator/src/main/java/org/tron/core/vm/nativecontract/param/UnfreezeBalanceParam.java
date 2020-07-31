package org.tron.core.vm.nativecontract.param;

import lombok.Data;
import org.tron.protos.contract.Common;

@Data
public class UnfreezeBalanceParam {
    private byte[] ownerAddress;
    private Common.ResourceCode resource;
    private byte[] receiverAddress;
}