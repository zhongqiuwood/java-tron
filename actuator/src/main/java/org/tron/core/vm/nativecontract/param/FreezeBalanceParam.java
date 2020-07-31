package org.tron.core.vm.nativecontract.param;

import lombok.Data;
import org.tron.protos.contract.Common;

@Data
public class FreezeBalanceParam {
    private byte[] ownerAddress;
    private long frozenDuration;
    private long frozenBalance;
    private byte[] receiverAddress;
    private Common.ResourceCode resource;
}
