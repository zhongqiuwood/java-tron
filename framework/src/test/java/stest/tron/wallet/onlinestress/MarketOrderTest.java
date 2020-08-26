package stest.tron.wallet.onlinestress;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.List;
import java.util.Optional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.protos.Protocol.MarketOrder;
import org.tron.protos.Protocol.MarketOrderList;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.utils.PublicMethed;

public class MarketOrderTest {
  String[] addressList = {

  };



  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(1);

  /**
   * constructor.
   */

  @BeforeClass(enabled = true)
  public void beforeClass() {
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }

  @Test
  public void markerOrderCancle(){

    for(String key : addressList){
      byte[] addressByteArray = PublicMethed.getFinalAddress(key);
      List<MarketOrder> OrderList = PublicMethed
          .getMarketOrderByAccount(addressByteArray, blockingStubFull).get().getOrdersList();

      for(MarketOrder order : OrderList){

        byte[] orderId = order.getOrderId().toByteArray();
        String txid = PublicMethed
            .marketCancelOrder(addressByteArray, key, orderId, blockingStubFull);

      }
    }
  }


}
