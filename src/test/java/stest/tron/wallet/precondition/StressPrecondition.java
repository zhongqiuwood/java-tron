package stest.tron.wallet.precondition;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.ExchangeList;
import org.tron.api.GrpcAPI.ProposalList;
import org.tron.api.WalletGrpc;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Configuration;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.ChainParameters;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.PublicMethed;
import stest.tron.wallet.common.client.utils.PublicMethedForMutiSign;

@Slf4j
public class StressPrecondition {

  protected String commonOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.commonOwnerAddress");
  protected String triggerOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.triggerOwnerAddress");
  protected String triggerOwnerKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.triggerOwnerKey");
  protected String commonOwnerPrivateKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.commonOwnerPrivateKey");
  protected String commonToAddress = Configuration.getByPath("stress.conf")
      .getString("address.commonToAddress");
  protected String commonToPrivateKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.commonToPrivateKey");
  protected String commonWitnessAddress = Configuration.getByPath("stress.conf")
      .getString("address.commonWitnessAddress");
  protected String commonWitnessPrivateKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.commonWitnessPrivateKey");

/*  protected String commonContractAddress1 = Configuration.getByPath("stress.conf")
      .getString("address.commonContractAddress1");
  protected String commonContractAddress2 = Configuration.getByPath("stress.conf")
      .getString("address.commonContractAddress2");
  protected String commonContractAddress3 = Configuration.getByPath("stress.conf")
      .getString("address.commonContractAddress3");
  protected String commontokenid = Configuration.getByPath("stress.conf")
      .getString("param.commontokenid");
  protected long commonexchangeid = Configuration.getByPath("stress.conf")
      .getLong("param.commonexchangeid");*/

  protected String delegateResourceAddress = Configuration.getByPath("stress.conf")
      .getString("address.delegateResourceAddress");
  protected String delegateResourceKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.delegateResourceKey");

  protected String assetIssueOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.assetIssueOwnerAddress");
  protected String assetIssueOwnerKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.assetIssueOwnerKey");
  protected String participateOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.participateOwnerAddress");
  protected String participateOwnerPrivateKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.participateOwnerPrivateKey");
  protected String exchangeOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.exchangeOwnerAddress");
  protected String exchangeOwnerKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.exchangeOwnerKey");
  private String mutiSignOwnerAddress = Configuration.getByPath("stress.conf")
      .getString("address.mutiSignOwnerAddress");
  private String mutiSignOwnerKey = Configuration.getByPath("stress.conf")
      .getString("privateKey.mutiSignOwnerKey");

  Long firstTokenInitialBalance = 500000000L;
  Long secondTokenInitialBalance = 500000000L;

  //TXtrbmfwZ2LxtoCveEhZT86fTss1w8rwJE
  String witnessKey001 = Configuration.getByPath("stress.conf").getString("permissioner.key1");
  //TWKKwLswTTcK5cp31F2bAteQrzU8cYhtU5
  String witnessKey002 = Configuration.getByPath("stress.conf").getString("permissioner.key2");
  //TT4MHXVApKfbcq7cDLKnes9h9wLSD4eMJi
  String witnessKey003 = Configuration.getByPath("stress.conf").getString("permissioner.key3");
  //TCw4yb4hS923FisfMsxAzQ85srXkK6RWGk
  String witnessKey004 = Configuration.getByPath("stress.conf").getString("permissioner.key4");
  //TLYUrci5Qw5fUPho2GvFv38kAK4QSmdhhN
  String witnessKey005 = Configuration.getByPath("stress.conf").getString("permissioner.key5");

  private final byte[] witness001Address = PublicMethed.getFinalAddress(witnessKey001);
  private final byte[] witness002Address = PublicMethed.getFinalAddress(witnessKey002);
  private final byte[] witness003Address = PublicMethed.getFinalAddress(witnessKey003);
  private final byte[] witness004Address = PublicMethed.getFinalAddress(witnessKey004);
  private final byte[] witness005Address = PublicMethed.getFinalAddress(witnessKey005);

  private ManagedChannel channelFull = null;

  private WalletGrpc.WalletBlockingStub blockingStubFull = null;


  private String oldAddress;
  private String newAddress;
  private String newContractAddress;
  private String fullnode = stest.tron.wallet.common.client.Configuration.getByPath("stress.conf")
      .getStringList("fullnode.ip.list")
      .get(0);
  ByteString assetIssueId;
  Optional<ExchangeList> listExchange;
  byte[] commonContractAddress1;
  byte[] commonContractAddress2;
  byte[] commonContractAddress3;
  byte[] commonContractAddress4;
  byte[] commonContractAddress5;
  byte[] commonContractAddress6;
  byte[] commonContractAddress7;
  byte[] commonContractAddress8;
  byte[] commonContractAddress9;
  byte[] commonContractAddress10;
  byte[] commonContractAddress11;


  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  /**
   * constructor.
   */

  @BeforeClass
  public void beforeClass() {
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }

  @Test(enabled = false)
  public void test001CreateProposal() {
    ChainParameters chainParameters = blockingStubFull
        .getChainParameters(EmptyMessage.newBuilder().build());
    Optional<ChainParameters> getChainParameters = Optional.ofNullable(chainParameters);
    logger.info(Long.toString(getChainParameters.get().getChainParameterCount()));
    for (Integer i = 0; i < getChainParameters.get().getChainParameterCount(); i++) {
      logger.info(getChainParameters.get().getChainParameter(i).getKey());
      logger.info(Long.toString(getChainParameters.get().getChainParameter(i).getValue()));
    }
    HashMap<Long, Long> proposalMap = new HashMap<Long, Long>();
/*    if (getChainParameters.get().getChainParameter(15).getValue() == 0) {
      proposalMap.put(15L, 1L);
    }
   if (getChainParameters.get().getChainParameter(16).getValue() == 0) {
      proposalMap.put(16L, 1L);
    }
    if (getChainParameters.get().getChainParameter(18).getValue() == 0) {
      proposalMap.put(18L, 1L);
    }*/
  /*if (getChainParameters.get().getChainParameter(22).getValue() == 0L) {
      logger.info("24 value is " + getChainParameters.get().getChainParameter(24).getValue());
      proposalMap.put(24L, 1L);
    }*/
    if (getChainParameters.get().getChainParameter(28).getValue() == 0L) {
      proposalMap.put(24L, 1L);
    }
/*    if (getChainParameters.get().getChainParameter(27).getValue() == 0L) {
      proposalMap.put(25L, 1L);
    }*/
    if (getChainParameters.get().getChainParameter(29).getValue() == 0L) {
      proposalMap.put(26L, 1L);
    }

    if (proposalMap.size() >= 1) {

      PublicMethed.createProposal(witness001Address, witnessKey001,
          proposalMap, blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      ProposalList proposalList = blockingStubFull.listProposals(EmptyMessage.newBuilder().build());
      Optional<ProposalList> listProposals = Optional.ofNullable(proposalList);
      final Integer proposalId = listProposals.get().getProposalsCount();
      PublicMethed.approveProposal(witness001Address, witnessKey001, proposalId,
          true, blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.approveProposal(witness002Address, witnessKey002, proposalId,
          true, blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.approveProposal(witness003Address, witnessKey003, proposalId,
          true, blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.approveProposal(witness004Address, witnessKey004, proposalId,
          true, blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.approveProposal(witness005Address, witnessKey005, proposalId,
          true, blockingStubFull);
      waitProposalApprove(29, blockingStubFull);
    }
  }

  @Test(enabled = true)
  public void test002SendCoinToStressAccount() {
    sendCoinToStressAccount(commonOwnerPrivateKey);
    sendCoinToStressAccount(triggerOwnerKey);
    sendCoinToStressAccount(commonToPrivateKey);
    sendCoinToStressAccount(assetIssueOwnerKey);
    sendCoinToStressAccount(participateOwnerPrivateKey);
    sendCoinToStressAccount(exchangeOwnerKey);
    sendCoinToStressAccount(mutiSignOwnerKey);
    logger.info(
        "commonOwnerAddress " + PublicMethed.queryAccount(commonOwnerPrivateKey, blockingStubFull)
            .getBalance());
    logger.info(
        "triggerOwnerAddress " + PublicMethed.queryAccount(triggerOwnerKey, blockingStubFull)
            .getBalance());
    logger.info("commonToAddress " + PublicMethed.queryAccount(commonToPrivateKey, blockingStubFull)
        .getBalance());
    logger.info(
        "assetIssueOwnerAddress " + PublicMethed.queryAccount(assetIssueOwnerKey, blockingStubFull)
            .getBalance());
    logger.info("participateOwnerAddress " + PublicMethed
        .queryAccount(participateOwnerPrivateKey, blockingStubFull).getBalance());
    logger.info("exchangeOwnerKey " + PublicMethed.queryAccount(exchangeOwnerKey, blockingStubFull)
        .getBalance());
    logger.info("mutiSignOwnerKey " + PublicMethed.queryAccount(mutiSignOwnerKey, blockingStubFull)
        .getBalance());
    PublicMethed
        .freezeBalanceGetEnergy(PublicMethed.getFinalAddress(triggerOwnerKey), 50000000000000L, 3,
            1, triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed
        .freezeBalanceGetEnergy(PublicMethed.getFinalAddress(triggerOwnerKey), 50000000000000L, 3,
            0, triggerOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test003DeploySmartContract1() {
    String contractName = "tokenTest";
    String code = "608060405260e2806100126000396000f300608060405260043610603e5763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416633be9ece781146043575b600080fd5b606873ffffffffffffffffffffffffffffffffffffffff60043516602435604435606a565b005b60405173ffffffffffffffffffffffffffffffffffffffff84169082156108fc029083908590600081818185878a8ad094505050505015801560b0573d6000803e3d6000fd5b505050505600a165627a7a72305820d7ac1a3b49eeff286b7f2402b93047e60deb6dba47f4f889d921dbcb3bb81f8a0029";
    String abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"toAddress\",\"type\":\"address\"},{\"name\":\"id\",\"type\":\"trcToken\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"TransferTokenTo\",\"outputs\":[],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"inputs\":[],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"constructor\"}]";
    commonContractAddress1 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L,
            0L, 100, 10000, "0",
            0, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey),
            blockingStubFull);

    newContractAddress = WalletClient.encode58Check(commonContractAddress1);

    oldAddress = readWantedText("stress.conf", "commonContractAddress1");
    newAddress = "  commonContractAddress1 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

  }

  @Test(enabled = true)
  public void test004DeploySmartContract2() {
    String contractName = "BTest";
    String code = "60806040526000805560c5806100166000396000f30060806040526004361060485763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166362548c7b8114604a578063890eba68146050575b005b6048608c565b348015605b57600080fd5b50d38015606757600080fd5b50d28015607357600080fd5b50607a6093565b60408051918252519081900360200190f35b6001600055565b600054815600a165627a7a723058204c4f1bb8eca0c4f1678cc7cc1179e03d99da2a980e6792feebe4d55c89c022830029";
    String abi = "[{\"constant\":false,\"inputs\":[],\"name\":\"setFlag\",\"outputs\":[],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"flag\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"constructor\"},{\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"fallback\"}]";
    commonContractAddress2 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L,
            0L, 100, 10000, "0",
            0, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey),
            blockingStubFull);

    newContractAddress = WalletClient.encode58Check(commonContractAddress2);

    oldAddress = readWantedText("stress.conf", "commonContractAddress2");
    newAddress = "  commonContractAddress2 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test005DeploySmartContract3() {
    String contractName = "TestSStore";
    String code = "608060405234801561001057600080fd5b5061045c806100206000396000f30060806040526004361061006d576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806304c58438146100725780634f2be91f1461009f578063812db772146100b657806393cd5755146100e3578063d1cd64e914610189575b600080fd5b34801561007e57600080fd5b5061009d600480360381019080803590602001909291905050506101a0565b005b3480156100ab57600080fd5b506100b4610230565b005b3480156100c257600080fd5b506100e1600480360381019080803590602001909291905050506102a2565b005b3480156100ef57600080fd5b5061010e600480360381019080803590602001909291905050506102c3565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561014e578082015181840152602081019050610133565b50505050905090810190601f16801561017b5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561019557600080fd5b5061019e61037e565b005b6000600190505b8181101561022c5760008060018154018082558091505090600182039060005260206000200160006040805190810160405280600881526020017f31323334353637380000000000000000000000000000000000000000000000008152509091909150908051906020019061021d92919061038b565b505080806001019150506101a7565b5050565b60008060018154018082558091505090600182039060005260206000200160006040805190810160405280600881526020017f61626364656667680000000000000000000000000000000000000000000000008152509091909150908051906020019061029e92919061038b565b5050565b6000600190505b81811115156102bf5780806001019150506102a9565b5050565b6000818154811015156102d257fe5b906000526020600020016000915090508054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156103765780601f1061034b57610100808354040283529160200191610376565b820191906000526020600020905b81548152906001019060200180831161035957829003601f168201915b505050505081565b6000808060010191505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106103cc57805160ff19168380011785556103fa565b828001600101855582156103fa579182015b828111156103f95782518255916020019190600101906103de565b5b509050610407919061040b565b5090565b61042d91905b80821115610429576000816000905550600101610411565b5090565b905600a165627a7a7230582087d9880a135295a17100f63b8941457f4369204d3ccc9ce4a1abf99820eb68480029";
    String abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"index\",\"type\":\"uint256\"}],\"name\":\"add2\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"add\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"index\",\"type\":\"uint256\"}],\"name\":\"fori2\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"args\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"fori\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";
    commonContractAddress3 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L,
            0L, 100, 10000, "0",
            0, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey),
            blockingStubFull);

    newContractAddress = WalletClient.encode58Check(commonContractAddress3);

    oldAddress = readWantedText("stress.conf", "commonContractAddress3");
    newAddress = "  commonContractAddress3 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test006CreateToken() {
    if (PublicMethed.queryAccount(assetIssueOwnerKey, blockingStubFull).getAssetIssuedID()
        .isEmpty()) {
      Long start = System.currentTimeMillis() + 20000;
      Long end = System.currentTimeMillis() + 1000000000;
      /*PublicMethed.createAssetIssue(PublicMethed.getFinalAddress(assetIssueOwnerKey), "xxd",
          50000000000000L,
          1, 1, start, end, 1, "wwwwww", "wwwwwwww", 100000L,
          100000L, 1L, 1L, assetIssueOwnerKey, blockingStubFull);
      logger.info("createAssetIssue");*/
      //PublicMethed.waitProduceNextBlock(blockingStubFull);
      //PublicMethed.waitProduceNextBlock(blockingStubFull);
    }

    assetIssueId = PublicMethed.queryAccount(assetIssueOwnerKey, blockingStubFull)
        .getAssetIssuedID();
    logger.info("AssetIssueId is " + ByteArray.toStr(assetIssueId.toByteArray()));
    String tokenid = "1000001";
    byte[] token = ByteArray.fromString(tokenid);
    logger.info(ByteArray.toStr(token));
//    logger.info("commonContractAddress1 is " + Wallet.encode58Check(commonContractAddress1));
    PublicMethed.transferAsset(commonContractAddress1, token, 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress1, token, 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress1, token, 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress1, token, 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress2, token, 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress2, token, 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress2, token, 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);
    PublicMethed.transferAsset(commonContractAddress2, token, 300000000000L,
        PublicMethed.getFinalAddress(assetIssueOwnerKey), assetIssueOwnerKey, blockingStubFull);

    /*String newTokenId = ByteArray.toStr(assetIssueId.toByteArray());
    String oldTokenIdString = readWantedText("stress.conf", "commontokenid");
    logger.info("oldTokenIdString " + oldTokenIdString);
    String newTokenIdInConfig = "commontokenid = " + newTokenId;
    logger.info("newTokenIdInConfig " + newTokenIdInConfig);
    replacAddressInConfig("stress.conf", oldTokenIdString, newTokenIdInConfig);*/
  }

  @Test(enabled = false)
  public void test007CreateExchange() {
    listExchange = PublicMethed.getExchangeList(blockingStubFull);
    Long exchangeId = 0L;
    assetIssueId = PublicMethed.queryAccount(exchangeOwnerKey, blockingStubFull).getAssetIssuedID();

    for (Integer i = 0; i < listExchange.get().getExchangesCount(); i++) {
      if (ByteArray.toHexString(listExchange.get().getExchanges(i)
          .getCreatorAddress().toByteArray()).equalsIgnoreCase(
          ByteArray.toHexString(PublicMethed.getFinalAddress(exchangeOwnerKey)))) {
        logger.info("id is " + listExchange.get().getExchanges(i).getExchangeId());
        exchangeId = listExchange.get().getExchanges(i).getExchangeId();
        break;
      }
    }

    if (exchangeId == 0L) {
      String trx = "_";
      byte[] b = trx.getBytes();
      PublicMethed.exchangeCreate(assetIssueId.toByteArray(), firstTokenInitialBalance,
          b, secondTokenInitialBalance, PublicMethed.getFinalAddress(exchangeOwnerKey),
          exchangeOwnerKey, blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      listExchange = PublicMethed.getExchangeList(blockingStubFull);
      for (Integer i = 0; i < listExchange.get().getExchangesCount(); i++) {
        if (ByteArray.toHexString(listExchange.get().getExchanges(i)
            .getCreatorAddress().toByteArray()).equalsIgnoreCase(
            ByteArray.toHexString(PublicMethed.getFinalAddress(exchangeOwnerKey)))) {
          logger.info("id is " + listExchange.get().getExchanges(i).getExchangeId());
          exchangeId = listExchange.get().getExchanges(i).getExchangeId();
          break;
        }
      }
    }

    String newExchangeId = "" + exchangeId;
    String oldExchangeIdString = readWantedText("stress.conf", "commonexchangeid");
    logger.info("oldExchangeIdString " + oldExchangeIdString);
    String newTokenIdInConfig = "commonexchangeid = " + newExchangeId;
    logger.info("newTokenIdInConfig " + newTokenIdInConfig);
    replacAddressInConfig("stress.conf", oldExchangeIdString, newTokenIdInConfig);
  }


  @Test(enabled = true)
  public void test008MutiSignUpdate() {
    String[] permissionKeyString = new String[5];
    String[] ownerKeyString = new String[1];
    permissionKeyString[0] = witnessKey001;
    permissionKeyString[1] = witnessKey002;
    permissionKeyString[2] = witnessKey003;
    permissionKeyString[3] = witnessKey004;
    permissionKeyString[4] = witnessKey005;

    ownerKeyString[0] = mutiSignOwnerKey;

    String accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":5,\"keys\":["
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey001) + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey002) + "\",\"weight\":1}"
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey003) + "\",\"weight\":1}"
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey004) + "\",\"weight\":1}"
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey005)
            + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":2,"
            + "\"operations\":\"3f3d1ec0032001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":["
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey001) + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethed.getAddressString(witnessKey002) + "\",\"weight\":1}"
            + "]}]}";

    logger.info(accountPermissionJson);
    PublicMethedForMutiSign.accountPermissionUpdate(accountPermissionJson,
        PublicMethed.getFinalAddress(mutiSignOwnerKey), mutiSignOwnerKey,
        blockingStubFull, ownerKeyString);

  }

  @Test(enabled = true)
  public void test009DeploySmartContract4() {
    String contractName = "TRC20_TRON";
    String abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[],\"name\":\"stop\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"burn\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":true,\"inputs\":[],\"name\":\"stopped\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":false,\"inputs\":[],\"name\":\"start\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":false,\"inputs\":[{\"name\":\"_name\",\"type\":\"string\"}],\"name\":\"setName\",\"outputs\":[],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"nonpayable\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\",\"stateMutability\":\"view\"},{\"inputs\":[],\"payable\":false,\"type\":\"constructor\",\"stateMutability\":\"nonpayable\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"_owner\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"_spender\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"}]";
    String code = "6060604052604060405190810160405280600681526020017f54726f6e697800000000000000000000000000000000000000000000000000008152506000908051906020019062000052929190620001b6565b50604060405190810160405280600381526020017f545258000000000000000000000000000000000000000000000000000000000081525060019080519060200190620000a1929190620001b6565b50600660025560006005556000600660006101000a81548160ff0219169083151502179055506000600660016101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555034156200011257fe5b5b33600660016101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555067016345785d8a000060058190555067016345785d8a0000600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055505b62000265565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620001f957805160ff19168380011785556200022a565b828001600101855582156200022a579182015b82811115620002295782518255916020019190600101906200020c565b5b5090506200023991906200023d565b5090565b6200026291905b808211156200025e57600081600090555060010162000244565b5090565b90565b61111480620002756000396000f300606060405236156100ce576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806306fdde03146100d057806307da68f514610169578063095ea7b31461017b57806318160ddd146101d257806323b872dd146101f8578063313ce5671461026e57806342966c681461029457806370a08231146102b457806375f12b21146102fe57806395d89b4114610328578063a9059cbb146103c1578063be9a655514610418578063c47f00271461042a578063dd62ed3e14610484575bfe5b34156100d857fe5b6100e06104ed565b604051808060200182810382528381815181526020019150805190602001908083836000831461012f575b80518252602083111561012f5760208201915060208101905060208303925061010b565b505050905090810190601f16801561015b5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561017157fe5b61017961058b565b005b341561018357fe5b6101b8600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610603565b604051808215151515815260200191505060405180910390f35b34156101da57fe5b6101e26107cb565b6040518082815260200191505060405180910390f35b341561020057fe5b610254600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803573ffffffffffffffffffffffffffffffffffffffff169060200190919080359060200190919050506107d1565b604051808215151515815260200191505060405180910390f35b341561027657fe5b61027e610b11565b6040518082815260200191505060405180910390f35b341561029c57fe5b6102b26004808035906020019091905050610b17565b005b34156102bc57fe5b6102e8600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610c3f565b6040518082815260200191505060405180910390f35b341561030657fe5b61030e610c57565b604051808215151515815260200191505060405180910390f35b341561033057fe5b610338610c6a565b6040518080602001828103825283818151815260200191508051906020019080838360008314610387575b80518252602083111561038757602082019150602081019050602083039250610363565b505050905090810190601f1680156103b35780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34156103c957fe5b6103fe600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610d08565b604051808215151515815260200191505060405180910390f35b341561042057fe5b610428610f31565b005b341561043257fe5b610482600480803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091905050610fa9565b005b341561048c57fe5b6104d7600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061101e565b6040518082815260200191505060405180910390f35b60008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105835780601f1061055857610100808354040283529160200191610583565b820191906000526020600020905b81548152906001019060200180831161056657829003601f168201915b505050505081565b3373ffffffffffffffffffffffffffffffffffffffff16600660019054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161415156105e457fe5b6001600660006101000a81548160ff0219169083151502179055505b5b565b6000600660009054906101000a900460ff1615151561061e57fe5b3373ffffffffffffffffffffffffffffffffffffffff1660001415151561064157fe5b60008214806106cc57506000600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054145b15156106d85760006000fd5b81600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925846040518082815260200191505060405180910390a3600190505b5b5b92915050565b60055481565b6000600660009054906101000a900460ff161515156107ec57fe5b3373ffffffffffffffffffffffffffffffffffffffff1660001415151561080f57fe5b81600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020541015151561085e5760006000fd5b600360008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205482600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205401101515156108ee5760006000fd5b81600460008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020541015151561097a5760006000fd5b81600360008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254019250508190555081600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254039250508190555081600460008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825403925050819055508273ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040518082815260200191505060405180910390a3600190505b5b5b9392505050565b60025481565b80600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205410151515610b665760006000fd5b80600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825403925050819055508060036000600073ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254019250508190555060003373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef836040518082815260200191505060405180910390a35b50565b60036020528060005260406000206000915090505481565b600660009054906101000a900460ff1681565b60018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610d005780601f10610cd557610100808354040283529160200191610d00565b820191906000526020600020905b815481529060010190602001808311610ce357829003601f168201915b505050505081565b6000600660009054906101000a900460ff16151515610d2357fe5b3373ffffffffffffffffffffffffffffffffffffffff16600014151515610d4657fe5b81600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205410151515610d955760006000fd5b600360008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205482600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020540110151515610e255760006000fd5b81600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254039250508190555081600360008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825401925050819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040518082815260200191505060405180910390a3600190505b5b5b92915050565b3373ffffffffffffffffffffffffffffffffffffffff16600660019054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16141515610f8a57fe5b6000600660006101000a81548160ff0219169083151502179055505b5b565b3373ffffffffffffffffffffffffffffffffffffffff16600660019054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614151561100257fe5b8060009080519060200190611018929190611043565b505b5b50565b6004602052816000526040600020602052806000526040600020600091509150505481565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061108457805160ff19168380011785556110b2565b828001600101855582156110b2579182015b828111156110b1578251825591602001919060010190611096565b5b5090506110bf91906110c3565b5090565b6110e591905b808211156110e15760008160009055506001016110c9565b5090565b905600a165627a7a723058204858328431ff0a4e0db74ff432e5805ce4bcf91a1c59650a93bd7c1aec5e0fe10029";
    commonContractAddress4 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L,
            0L, 100, 10000, "0",
            0, null, triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey),
            blockingStubFull);

    newContractAddress = WalletClient.encode58Check(commonContractAddress4);

    oldAddress = readWantedText("stress.conf", "commonContractAddress4");
    newAddress = "  commonContractAddress4 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test010DeploySmartContract5() {
    String contractName = "Trigger";
    String code = stest.tron.wallet.common.client.Configuration.getByPath("stress.conf")
        .getString("code.code_veryLarge");
    String abi = stest.tron.wallet.common.client.Configuration.getByPath("stress.conf")
        .getString("abi.abi_veryLarge");

    commonContractAddress5 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L, 0L, 100, 10000, "0", 0, null,
            triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    newContractAddress = WalletClient.encode58Check(commonContractAddress5);

    oldAddress = readWantedText("stress.conf", "commonContractAddress5");
    newAddress = "  commonContractAddress5 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

  }

  @Test(enabled = true)
  public void test011DeploySmartContract6() {
    String contractName = "Trigger";
    String abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"addr\",\"type\":\"address\"}],\"name\":\"test\",\"outputs\":[{\"name\":\"i\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"addrs\",\"type\":\"address[]\"}],\"name\":\"test\",\"outputs\":[{\"name\":\"i\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506101df8061003a6000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506004361061006c577c01000000000000000000000000000000000000000000000000000000006000350463bb29998e8114610071578063d57498ea146100b6575b600080fd5b6100a46004803603602081101561008757600080fd5b503573ffffffffffffffffffffffffffffffffffffffff16610159565b60408051918252519081900360200190f35b6100a4600480360360208110156100cc57600080fd5b8101906020810181356401000000008111156100e757600080fd5b8201836020820111156100f957600080fd5b8035906020019184602083028401116401000000008311171561011b57600080fd5b919080806020026020016040519081016040528093929190818152602001838360200280828437600092019190915250929550610178945050505050565b6000805b6103e85a11156101725750600101813f61015d565b50919050565b600080805b83518110156101ac576000848281518110151561019657fe5b602090810290910101513f92505060010161017d565b939250505056fea165627a7a7230582033651916fb1624df072a51c976207dd49ce0af4f3479f46a4f81f293afcc5f2b0029";
    commonContractAddress6 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L, 0L, 100, 10000, "0", 0, null,
            triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    newContractAddress = WalletClient.encode58Check(commonContractAddress6);

    oldAddress = readWantedText("stress.conf", "commonContractAddress6");
    newAddress = "  commonContractAddress6 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test012DeploySmartContract7() {
    String contractName = "Demo";
    String abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"hash\",\"type\":\"bytes32\"},{\"name\":\"signatures\",\"type\":\"bytes[]\"},{\"name\":\"addresses\",\"type\":\"address[]\"}],\"name\":\"testPure\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"pure\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"hash\",\"type\":\"bytes32\"},{\"name\":\"signatures\",\"type\":\"bytes[]\"},{\"name\":\"addresses\",\"type\":\"address[]\"}],\"name\":\"testArray\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506105a28061003a6000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50600436106100505760003560e01c8063022ac30f14610055578063da586d8814610055575b600080fd5b61006861006336600461022d565b61007e565b60405161007591906103e1565b60405180910390f35b60006009848484604051600081526020016040526040516100a1939291906103ef565b6020604051602081039080840390855afa1580156100c3573d6000803e3d6000fd5b5050604051601f19015195945050505050565b80356100e18161051b565b6100ea816104af565b9392505050565b600082601f83011261010257600080fd5b813561011561011082610453565b61042c565b9150818183526020840193506020810190508385602084028201111561013a57600080fd5b60005b83811015610166578161015088826100d6565b845250602092830192919091019060010161013d565b5050505092915050565b600082601f83011261018157600080fd5b813561018f61011082610453565b81815260209384019390925082018360005b8381101561016657813586016101b788826101de565b84525060209283019291909101906001016101a1565b80356101d881610532565b92915050565b600082601f8301126101ef57600080fd5b81356101fd61011082610474565b9150808252602083016020830185838301111561021957600080fd5b6102248382846104d5565b50505092915050565b60008060006060848603121561024257600080fd5b600061024e86866101cd565b935050602084013567ffffffffffffffff81111561026b57600080fd5b61027786828701610170565b925050604084013567ffffffffffffffff81111561029457600080fd5b6102a0868287016100f1565b9150509250925092565b60006102b683836102ca565b505060200190565b60006100ea83836103a9565b6102d3816104af565b82525050565b60006102e4826104a2565b6102ee81856104a6565b93506102f98361049c565b8060005b8381101561032757815161031188826102aa565b975061031c8361049c565b9250506001016102fd565b509495945050505050565b600061033d826104a2565b61034781856104a6565b9350836020820285016103598561049c565b8060005b85811015610393578484038952815161037685826102be565b94506103818361049c565b60209a909a019992505060010161035d565b5091979650505050505050565b6102d3816104ba565b60006103b4826104a2565b6103be81856104a6565b93506103ce8185602086016104e1565b6103d781610511565b9093019392505050565b602081016101d882846103a0565b606081016103fd82866103a0565b818103602083015261040f8185610332565b9050818103604083015261042381846102d9565b95945050505050565b60405181810167ffffffffffffffff8111828210171561044b57600080fd5b604052919050565b600067ffffffffffffffff82111561046a57600080fd5b5060209081020190565b600067ffffffffffffffff82111561048b57600080fd5b506020601f91909101601f19160190565b60200190565b5190565b90815260200190565b60006101d8826104bd565b90565b6001600160a01b031690565b6001600160a81b031690565b82818337506000910152565b60005b838110156104fc5781810151838201526020016104e4565b8381111561050b576000848401525b50505050565b601f01601f191690565b610524816104c9565b811461052f57600080fd5b50565b610524816104ba56fea36474726f6e58200355930b389b97929c271e573071a5f1134174ac32dbba3dbe50f490f42f565a6c6578706572696d656e74616cf564736f6c637827302e352e392d646576656c6f702e323031392e382e32312b636f6d6d69742e31393035643732660064";
    commonContractAddress7 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L, 0L, 100, 10000, "0", 0, null,
            triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    newContractAddress = WalletClient.encode58Check(commonContractAddress7);

    oldAddress = readWantedText("stress.conf", "commonContractAddress7");
    newAddress = "  commonContractAddress7 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test013DeploySmartContract8() {
    String contractName = "Demo";
    String abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"hash\",\"type\":\"bytes32\"},{\"name\":\"signatures\",\"type\":\"bytes[]\"},{\"name\":\"addresses\",\"type\":\"address[]\"}],\"name\":\"testPure\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"pure\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"hash\",\"type\":\"bytes32\"},{\"name\":\"signatures\",\"type\":\"bytes[]\"},{\"name\":\"addresses\",\"type\":\"address[]\"}],\"name\":\"testArray\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506105a28061003a6000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50600436106100505760003560e01c8063022ac30f14610055578063da586d8814610055575b600080fd5b61006861006336600461022d565b61007e565b60405161007591906103e1565b60405180910390f35b60006009848484604051600081526020016040526040516100a1939291906103ef565b6020604051602081039080840390855afa1580156100c3573d6000803e3d6000fd5b5050604051601f19015195945050505050565b80356100e18161051b565b6100ea816104af565b9392505050565b600082601f83011261010257600080fd5b813561011561011082610453565b61042c565b9150818183526020840193506020810190508385602084028201111561013a57600080fd5b60005b83811015610166578161015088826100d6565b845250602092830192919091019060010161013d565b5050505092915050565b600082601f83011261018157600080fd5b813561018f61011082610453565b81815260209384019390925082018360005b8381101561016657813586016101b788826101de565b84525060209283019291909101906001016101a1565b80356101d881610532565b92915050565b600082601f8301126101ef57600080fd5b81356101fd61011082610474565b9150808252602083016020830185838301111561021957600080fd5b6102248382846104d5565b50505092915050565b60008060006060848603121561024257600080fd5b600061024e86866101cd565b935050602084013567ffffffffffffffff81111561026b57600080fd5b61027786828701610170565b925050604084013567ffffffffffffffff81111561029457600080fd5b6102a0868287016100f1565b9150509250925092565b60006102b683836102ca565b505060200190565b60006100ea83836103a9565b6102d3816104af565b82525050565b60006102e4826104a2565b6102ee81856104a6565b93506102f98361049c565b8060005b8381101561032757815161031188826102aa565b975061031c8361049c565b9250506001016102fd565b509495945050505050565b600061033d826104a2565b61034781856104a6565b9350836020820285016103598561049c565b8060005b85811015610393578484038952815161037685826102be565b94506103818361049c565b60209a909a019992505060010161035d565b5091979650505050505050565b6102d3816104ba565b60006103b4826104a2565b6103be81856104a6565b93506103ce8185602086016104e1565b6103d781610511565b9093019392505050565b602081016101d882846103a0565b606081016103fd82866103a0565b818103602083015261040f8185610332565b9050818103604083015261042381846102d9565b95945050505050565b60405181810167ffffffffffffffff8111828210171561044b57600080fd5b604052919050565b600067ffffffffffffffff82111561046a57600080fd5b5060209081020190565b600067ffffffffffffffff82111561048b57600080fd5b506020601f91909101601f19160190565b60200190565b5190565b90815260200190565b60006101d8826104bd565b90565b6001600160a01b031690565b6001600160a81b031690565b82818337506000910152565b60005b838110156104fc5781810151838201526020016104e4565b8381111561050b576000848401525b50505050565b601f01601f191690565b610524816104c9565b811461052f57600080fd5b50565b610524816104ba56fea36474726f6e58200355930b389b97929c271e573071a5f1134174ac32dbba3dbe50f490f42f565a6c6578706572696d656e74616cf564736f6c637827302e352e392d646576656c6f702e323031392e382e32312b636f6d6d69742e31393035643732660064";
    commonContractAddress8 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L, 0L, 100, 10000, "0", 0, null,
            triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    newContractAddress = WalletClient.encode58Check(commonContractAddress8);

    oldAddress = readWantedText("stress.conf", "commonContractAddress8");
    newAddress = "  commonContractAddress8 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test014DeploySmartContract9() {
    String contractName = "Demo";
    String abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"hash\",\"type\":\"bytes32\"},{\"name\":\"signatures\",\"type\":\"bytes[]\"},{\"name\":\"addresses\",\"type\":\"address[]\"}],\"name\":\"testPure\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"pure\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"hash\",\"type\":\"bytes32\"},{\"name\":\"signatures\",\"type\":\"bytes[]\"},{\"name\":\"addresses\",\"type\":\"address[]\"}],\"name\":\"testArray\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506105a28061003a6000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50600436106100505760003560e01c8063022ac30f14610055578063da586d8814610055575b600080fd5b61006861006336600461022d565b61007e565b60405161007591906103e1565b60405180910390f35b60006009848484604051600081526020016040526040516100a1939291906103ef565b6020604051602081039080840390855afa1580156100c3573d6000803e3d6000fd5b5050604051601f19015195945050505050565b80356100e18161051b565b6100ea816104af565b9392505050565b600082601f83011261010257600080fd5b813561011561011082610453565b61042c565b9150818183526020840193506020810190508385602084028201111561013a57600080fd5b60005b83811015610166578161015088826100d6565b845250602092830192919091019060010161013d565b5050505092915050565b600082601f83011261018157600080fd5b813561018f61011082610453565b81815260209384019390925082018360005b8381101561016657813586016101b788826101de565b84525060209283019291909101906001016101a1565b80356101d881610532565b92915050565b600082601f8301126101ef57600080fd5b81356101fd61011082610474565b9150808252602083016020830185838301111561021957600080fd5b6102248382846104d5565b50505092915050565b60008060006060848603121561024257600080fd5b600061024e86866101cd565b935050602084013567ffffffffffffffff81111561026b57600080fd5b61027786828701610170565b925050604084013567ffffffffffffffff81111561029457600080fd5b6102a0868287016100f1565b9150509250925092565b60006102b683836102ca565b505060200190565b60006100ea83836103a9565b6102d3816104af565b82525050565b60006102e4826104a2565b6102ee81856104a6565b93506102f98361049c565b8060005b8381101561032757815161031188826102aa565b975061031c8361049c565b9250506001016102fd565b509495945050505050565b600061033d826104a2565b61034781856104a6565b9350836020820285016103598561049c565b8060005b85811015610393578484038952815161037685826102be565b94506103818361049c565b60209a909a019992505060010161035d565b5091979650505050505050565b6102d3816104ba565b60006103b4826104a2565b6103be81856104a6565b93506103ce8185602086016104e1565b6103d781610511565b9093019392505050565b602081016101d882846103a0565b606081016103fd82866103a0565b818103602083015261040f8185610332565b9050818103604083015261042381846102d9565b95945050505050565b60405181810167ffffffffffffffff8111828210171561044b57600080fd5b604052919050565b600067ffffffffffffffff82111561046a57600080fd5b5060209081020190565b600067ffffffffffffffff82111561048b57600080fd5b506020601f91909101601f19160190565b60200190565b5190565b90815260200190565b60006101d8826104bd565b90565b6001600160a01b031690565b6001600160a81b031690565b82818337506000910152565b60005b838110156104fc5781810151838201526020016104e4565b8381111561050b576000848401525b50505050565b601f01601f191690565b610524816104c9565b811461052f57600080fd5b50565b610524816104ba56fea36474726f6e58200355930b389b97929c271e573071a5f1134174ac32dbba3dbe50f490f42f565a6c6578706572696d656e74616cf564736f6c637827302e352e392d646576656c6f702e323031392e382e32312b636f6d6d69742e31393035643732660064";
    commonContractAddress9 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L, 0L, 100, 10000, "0", 0, null,
            triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    newContractAddress = WalletClient.encode58Check(commonContractAddress9);

    oldAddress = readWantedText("stress.conf", "commonContractAddress9");
    newAddress = "  commonContractAddress9 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test015DeploySmartContract10() {
    String contractName = "Demo";
    String abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"hash\",\"type\":\"bytes32\"},{\"name\":\"signatures\",\"type\":\"bytes[]\"},{\"name\":\"addresses\",\"type\":\"address[]\"}],\"name\":\"testPure\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"pure\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"hash\",\"type\":\"bytes32\"},{\"name\":\"signatures\",\"type\":\"bytes[]\"},{\"name\":\"addresses\",\"type\":\"address[]\"}],\"name\":\"testArray\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506105a28061003a6000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50600436106100505760003560e01c8063022ac30f14610055578063da586d8814610055575b600080fd5b61006861006336600461022d565b61007e565b60405161007591906103e1565b60405180910390f35b60006009848484604051600081526020016040526040516100a1939291906103ef565b6020604051602081039080840390855afa1580156100c3573d6000803e3d6000fd5b5050604051601f19015195945050505050565b80356100e18161051b565b6100ea816104af565b9392505050565b600082601f83011261010257600080fd5b813561011561011082610453565b61042c565b9150818183526020840193506020810190508385602084028201111561013a57600080fd5b60005b83811015610166578161015088826100d6565b845250602092830192919091019060010161013d565b5050505092915050565b600082601f83011261018157600080fd5b813561018f61011082610453565b81815260209384019390925082018360005b8381101561016657813586016101b788826101de565b84525060209283019291909101906001016101a1565b80356101d881610532565b92915050565b600082601f8301126101ef57600080fd5b81356101fd61011082610474565b9150808252602083016020830185838301111561021957600080fd5b6102248382846104d5565b50505092915050565b60008060006060848603121561024257600080fd5b600061024e86866101cd565b935050602084013567ffffffffffffffff81111561026b57600080fd5b61027786828701610170565b925050604084013567ffffffffffffffff81111561029457600080fd5b6102a0868287016100f1565b9150509250925092565b60006102b683836102ca565b505060200190565b60006100ea83836103a9565b6102d3816104af565b82525050565b60006102e4826104a2565b6102ee81856104a6565b93506102f98361049c565b8060005b8381101561032757815161031188826102aa565b975061031c8361049c565b9250506001016102fd565b509495945050505050565b600061033d826104a2565b61034781856104a6565b9350836020820285016103598561049c565b8060005b85811015610393578484038952815161037685826102be565b94506103818361049c565b60209a909a019992505060010161035d565b5091979650505050505050565b6102d3816104ba565b60006103b4826104a2565b6103be81856104a6565b93506103ce8185602086016104e1565b6103d781610511565b9093019392505050565b602081016101d882846103a0565b606081016103fd82866103a0565b818103602083015261040f8185610332565b9050818103604083015261042381846102d9565b95945050505050565b60405181810167ffffffffffffffff8111828210171561044b57600080fd5b604052919050565b600067ffffffffffffffff82111561046a57600080fd5b5060209081020190565b600067ffffffffffffffff82111561048b57600080fd5b506020601f91909101601f19160190565b60200190565b5190565b90815260200190565b60006101d8826104bd565b90565b6001600160a01b031690565b6001600160a81b031690565b82818337506000910152565b60005b838110156104fc5781810151838201526020016104e4565b8381111561050b576000848401525b50505050565b601f01601f191690565b610524816104c9565b811461052f57600080fd5b50565b610524816104ba56fea36474726f6e58200355930b389b97929c271e573071a5f1134174ac32dbba3dbe50f490f42f565a6c6578706572696d656e74616cf564736f6c637827302e352e392d646576656c6f702e323031392e382e32312b636f6d6d69742e31393035643732660064";
    commonContractAddress10 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L, 0L, 100, 10000, "0", 0, null,
            triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    newContractAddress = WalletClient.encode58Check(commonContractAddress10);

    oldAddress = readWantedText("stress.conf", "commonContractAddress10");
    newAddress = "  commonContractAddress10 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true)
  public void test016DeploySmartContract11() {
    String contractName = "ecrecoverValidateSign";
    String abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"hash\",\"type\":\"bytes32\"},{\"name\":\"sig\",\"type\":\"bytes\"},{\"name\":\"signer\",\"type\":\"address\"}],\"name\":\"validateSign\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506103998061003a6000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50600436106100455760003560e01c80634401c0191461004a575b600080fd5b61005d6100583660046101b4565b610073565b60405161006a919061023a565b60405180910390f35b600061008684848463ffffffff61008e16565b949350505050565b600061009a84846100b7565b6001600160a01b0316826001600160a01b03161490509392505050565b60208101516040820151604183015160009260ff90911691601b8310156100df57601b830192505b600186848484604051600081526020016040526040516101029493929190610248565b6020604051602081039080840390855afa158015610124573d6000803e3d6000fd5b5050506020604051035193505050505b92915050565b803561014581610312565b61014e816102d5565b9392505050565b803561013481610329565b600082601f83011261017157600080fd5b813561018461017f826102ad565b610286565b915080825260208301602083018583830111156101a057600080fd5b6101ab838284610306565b50505092915050565b6000806000606084860312156101c957600080fd5b60006101d58686610155565b935050602084013567ffffffffffffffff8111156101f257600080fd5b6101fe86828701610160565b925050604061020f8682870161013a565b9150509250925092565b610222816102e0565b82525050565b610222816102e5565b61022281610300565b602081016101348284610219565b608081016102568287610228565b6102636020830186610231565b6102706040830185610228565b61027d6060830184610228565b95945050505050565b60405181810167ffffffffffffffff811182821017156102a557600080fd5b604052919050565b600067ffffffffffffffff8211156102c457600080fd5b506020601f91909101601f19160190565b6000610134826102e8565b151590565b90565b6001600160a01b031690565b6001600160a81b031690565b60ff1690565b82818337506000910152565b61031b816102f4565b811461032657600080fd5b50565b61031b816102e556fea36474726f6e58206b345fcc15d1b39d287a37b487aa5f813cab50b40fd07789a7963585ca2b528d6c6578706572696d656e74616cf564736f6c637827302e352e392d646576656c6f702e323031392e382e32312b636f6d6d69742e31393035643732660064";
    commonContractAddress11 = PublicMethed
        .deployContract(contractName, abi, code, "", 1000000000L, 0L, 100, 10000, "0", 0, null,
            triggerOwnerKey, PublicMethed.getFinalAddress(triggerOwnerKey), blockingStubFull);
    newContractAddress = WalletClient.encode58Check(commonContractAddress11);

    oldAddress = readWantedText("stress.conf", "commonContractAddress11");
    newAddress = "  commonContractAddress11 = " + newContractAddress;
    logger.info("oldAddress " + oldAddress);
    logger.info("newAddress " + newAddress);
    replacAddressInConfig("stress.conf", oldAddress, newAddress);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  /**
   * constructor.
   */

  @AfterClass
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  public void sendCoinToStressAccount(String key) {
    if (PublicMethed.queryAccount(key, blockingStubFull).getBalance() <= 879998803847L) {
      PublicMethed.sendcoin(PublicMethed.getFinalAddress(key), 879998803847L, witness004Address,
          witnessKey004, blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
    }
  }


  public void replacAddressInConfig(String path, String oldAddress, String newAddress) {
    try {
      File file = new File(path);
      FileReader in = new FileReader(file);
      BufferedReader bufIn = new BufferedReader(in);
      CharArrayWriter tempStream = new CharArrayWriter();
      String line = null;
      while ((line = bufIn.readLine()) != null) {
        line = line.replaceAll(oldAddress, newAddress);
        tempStream.write(line);
        tempStream.append(System.getProperty("line.separator"));
      }
      bufIn.close();
      FileWriter out = new FileWriter(file);
      tempStream.writeTo(out);
      out.close();
      System.out.println("====path:" + path);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String readWantedText(String url, String wanted) {
    try {
      FileReader fr = new FileReader(url);
      BufferedReader br = new BufferedReader(fr);
      String temp = "";
      while (temp != null) {
        temp = br.readLine();
        if (temp != null && temp.contains(wanted)) {
          System.out.println(temp);
          return temp;
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  public static void waitProposalApprove(Integer proposalIndex,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Long currentTime = System.currentTimeMillis();
    while (System.currentTimeMillis() <= currentTime + 610000) {
      ChainParameters chainParameters = blockingStubFull
          .getChainParameters(EmptyMessage.newBuilder().build());
      Optional<ChainParameters> getChainParameters = Optional.ofNullable(chainParameters);
      if (getChainParameters.get().getChainParameter(proposalIndex).getValue() == 1L) {
        logger.info("Proposal has been approval");
        return;
      }
      PublicMethed.waitProduceNextBlock(blockingStubFull);
    }


  }


}


