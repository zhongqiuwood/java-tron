package org.tron.core.db;

import java.io.File;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.tron.common.application.TronApplicationContext;
import org.tron.common.utils.FileUtil;
import org.tron.core.Constant;
import org.tron.core.capsule.TransactionInfoCapsule;
import org.tron.core.capsule.TransactionRetCapsule;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.db.common.WrappedByteArray;
import org.tron.core.exception.BadItemException;
import org.tron.protos.Protocol.TransactionInfo;

public class DatabaseConvertTest {
  private static final String dbPath = "output-directory";
  TransactionRetStore transactionRetStore;
  TransactionHistoryStore transactionHistoryStore;
  private static TronApplicationContext context;

  static {
    Args.setParam(new String[]{"--output-directory", dbPath},
        Constant.TESTNET_CONF);
    context = new TronApplicationContext(DefaultConfig.class);
  }

  @Before
  public void init() {
    transactionRetStore = context.getBean(TransactionRetStore.class);
  }

  @After
  public void destroy() {
    Args.clearParam();
    context.destroy();
  }

  @Ignore
  @Test
  public void testConvertTransactionRetStore() {
    Map<WrappedByteArray, WrappedByteArray> hmap = transactionRetStore.getRevokingDB().getAllValues();
    hmap.values().stream().forEach(
        value-> {
          try {
            TransactionRetCapsule transactionRetCapsule = new TransactionRetCapsule(value.getBytes());
            for (TransactionInfo transactionInfo : transactionRetCapsule.getInstance().getTransactioninfoList()) {
              TransactionInfoCapsule capsule = new TransactionInfoCapsule(transactionInfo);
              transactionHistoryStore.put(capsule.getId(), capsule);
            }
          } catch (BadItemException e) {
          }
        }
    );
  }
}
