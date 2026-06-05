package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class UserData implements Serializable {

    private String username;
    private Set<Integer> duplicates;
    private Set<Integer> missing;

    public UserData(String username) {
        this.username = username;

        duplicates = new HashSet<>();
        missing = new HashSet<>();
    }

    public String getUsername() {
        return username;
    }

    public Set<Integer> getDuplicates() {
        return duplicates;
    }

    public Set<Integer> getMissing() {
        return missing;
    }

    public void setDuplicates(Set<Integer> duplicates) {
        this.duplicates = duplicates;
    }

    public void setMissing(Set<Integer> missing) {
        this.missing = missing;
    }
}