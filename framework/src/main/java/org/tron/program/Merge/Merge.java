package org.tron.program.Merge;

import java.security.InvalidParameterException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.tron.common.storage.leveldb.LevelDbDataSourceImpl;
import org.tron.common.storage.rocksdb.RocksDbDataSourceImpl;
import org.tron.core.config.args.Args;
import org.tron.core.db.common.DbSourceInter;

public class Merge {

  public void merge() {
    System.out.println("merge start ...");
    String accountTrace = "account-trace";
    String balanceTrace = "balance-trace";

    System.out.println("merge " + accountTrace + " start ...");
    merge(accountTrace);
    System.out.println("merge " + accountTrace + " end ...");
    System.out.println("merge " + balanceTrace + " start ...");
    merge(balanceTrace);
    System.out.println("merge " + balanceTrace + " end ...");

    System.out.println("merge end ...");
    System.exit(0);
  }

  public void merge(String dbName) {
    String fromString = Args.getInstance().getFrom();
    if (StringUtils.isEmpty(fromString)) {
      throw new InvalidParameterException("from dir must be not empty");
    }

    String toString = Args.getInstance().getTo();
    if (StringUtils.isEmpty(toString)) {
      throw new InvalidParameterException("to dir must be not empty");
    }

    String dbEngine = Args.getInstance().getStorage().getDbEngine();

    DbSourceInter<byte[]> from;
    DbSourceInter<byte[]> to;
    if ("LEVELDB".equals(dbEngine)) {
      from = new LevelDbDataSourceImpl(fromString, dbName);
      to = new LevelDbDataSourceImpl(toString, dbName);
    } else {
      from = new RocksDbDataSourceImpl(fromString, dbName);
      to = new RocksDbDataSourceImpl(toString, dbName);
    }

    from.initDB();
    to.initDB();

    merge(from, to);

    from.closeDB();
    to.closeDB();
  }

  public void merge(DbSourceInter<byte[]> from, DbSourceInter<byte[]> to) {
    long count = 0;
    for (Map.Entry<byte[], byte[]> e : from) {
      to.putData(e.getKey(), e.getValue());
      count++;
    }

    System.out.println("from size:" + count + "/" + to.getTotal() + ", to size:" + to.getTotal());
  }
}
