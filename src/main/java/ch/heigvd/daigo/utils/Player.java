package ch.heigvd.daigo.utils;

import java.util.Optional;

public class Player {
    private String name;
    private Optional<Player> opponent;
    private boolean inGame;

    public Player(String name) {
        this.name = name;
        this.opponent = Optional.empty();
        this.inGame = false;
    }

    public void create_game(){
        if(inGame || opponent.isPresent()) return;
        this.inGame = true;
    }

    public String get_name() {
        return name;
    }

    public boolean available() {
        return inGame && opponent.isEmpty();
    }

    public boolean set_opponent(Player other) {
        if(opponent.isPresent()) return false;
        if(other.opponent.isPresent()) return false;
        this.opponent = Optional.of(other);
        other.opponent = Optional.of(this);
        return true;
    }

    public boolean detach_opponent() {
        if(opponent.isEmpty() || opponent.get().opponent.isEmpty()) return false;
        opponent.get().opponent = Optional.empty();
        opponent = Optional.empty();
        return true;
    }
}
