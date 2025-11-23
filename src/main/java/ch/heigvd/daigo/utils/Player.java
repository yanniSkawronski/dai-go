package ch.heigvd.daigo.utils;

import java.util.Optional;

public class Player {
    private Integer id;
    private Optional<Integer> opponent_id;

    public Player(int id, int opponent_id) {
        this.id = id;
        this.opponent_id = Optional.of(opponent_id);
    }

    public Player(int id) {
        this.id = id;
        this.opponent_id = Optional.empty();
    }

    public int get_id() {
        return id;
    }

    public void set_opponent(Player opponent) {
        this.opponent_id = Optional.of(opponent.get_id());
        opponent.opponent_id = Optional.of(this.id);
    }
}
