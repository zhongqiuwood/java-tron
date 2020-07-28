package org.tron.core.vm.nativecontract;

import com.google.protobuf.ByteString;
import org.tron.protos.Protocol;

import java.util.ArrayList;

public class VoteWinessCapusle {

    private ByteString address;

    private int votesCount;

    private Protocol.Vote vote;

    private ArrayList<Protocol.Vote> votesList;

    public ByteString getAddress() {
        return address;
    }

    public void setAddress(ByteString address) {
        this.address = address;
    }

    public int getVotesCount() {
        return votesCount;
    }

    public void setVotesCount(int votesCount) {
        this.votesCount = votesCount;
    }

    public Protocol.Vote getVote() {
        return vote;
    }

    public void setVote(Protocol.Vote vote) {
        this.vote = vote;
    }

    public ArrayList<Protocol.Vote> getVotesList() {
        return votesList;
    }

    public void setVotesList(ArrayList<Protocol.Vote> votesList) {
        this.votesList = votesList;
    }
}
