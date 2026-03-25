package com.bluff.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String id;
    private String name;
    private boolean cpu;
    private List<Integer> dice;
    private boolean eliminated;

    public Player(String id, String name, boolean cpu) {
        this.id = id;
        this.name = name;
        this.cpu = cpu;
        this.dice = new ArrayList<>();
        this.eliminated = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isCpu() {
        return cpu;
    }

    public List<Integer> getDice() {
        return dice;
    }

    public void setDice(List<Integer> dice) {
        this.dice = dice;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }
}
