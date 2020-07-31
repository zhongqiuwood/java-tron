package org.tron.core.vm.nativecontract;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "nativecontract")
public class NativeContracts {

    private static final FreezeBalanceProcessor freezeBalanceProcessor = new FreezeBalanceProcessor();
}
