package model;

import java.io.Serializable;
import java.util.List;

public class ExchangeRequest implements Serializable {

    private String fromUser;
    private String toUser;
    private List<Integer> fromUserStickers;
    private List<Integer> toUserStickers;
    private boolean accepted;

    public ExchangeRequest(String fromUser, String toUser, List<Integer> fromUserStickers, List<Integer> toUserStickers) {
        this(fromUser, toUser, fromUserStickers, toUserStickers, false);
    }

    public ExchangeRequest(String fromUser, String toUser, List<Integer> fromUserStickers, List<Integer> toUserStickers, boolean accepted) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.fromUserStickers = fromUserStickers;
        this.toUserStickers = toUserStickers;
        this.accepted = accepted;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public List<Integer> getFromUserStickers() {
        return fromUserStickers;
    }

    public List<Integer> getToUserStickers() {
        return toUserStickers;
    }

    public boolean isAccepted() {
        return accepted;
    }
}