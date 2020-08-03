package org.tron.core.vm.nativecontract.param;

import lombok.Data;
import org.tron.protos.Protocol;

import java.util.ArrayList;

@Data
public class VoteWitnessParam {

    private byte[] ownerAddress;

    private int votesCount;

    private ArrayList<Protocol.Vote> votesList;

}
