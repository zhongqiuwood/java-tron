package org.tron.core.vm.nativecontract.param;

import lombok.Data;
import org.tron.protos.Protocol;

import java.util.ArrayList;

@Data
public class VoteWitnessParam {

    private byte[] owneraddress;

    private int votesCount;

    private Protocol.Vote vote;

    private ArrayList<Protocol.Vote> votesList;

}
