package model;

import java.io.Serializable;
import java.util.List;

public class ExchangeInfo implements Serializable {

    private String otherUser;
    private List<Integer> iGive;
    private List<Integer> heGives;

    public ExchangeInfo(String otherUser, List<Integer> iGive, List<Integer> heGives) {
        this.otherUser = otherUser;
        this.iGive = iGive;
        this.heGives = heGives;
    }

    public String getOtherUser() {
        return otherUser;
    }

    public List<Integer> getIGive() {
        return iGive;
    }

    public List<Integer> getHeGives() {
        return heGives;
    }

    @Override
    public String toString() {
        return otherUser + " (" + Math.min(iGive.size(), heGives.size()) + " slicica)";
    }
}