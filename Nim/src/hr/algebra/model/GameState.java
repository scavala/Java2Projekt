/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.model;

import java.awt.Point;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Objects;

/**
 *
 * @author Sime
 */
public class GameState implements Externalizable {

    private static final long serialVersionUID = 1L;

    private Collection<Point> burnedMatches;
    private Player playerOnTurn;

    public GameState() {
    }

    public GameState(Collection<Point> burnedMatches, Player playerOnTurn) {
        Objects.requireNonNull(burnedMatches);
        Objects.requireNonNull(playerOnTurn);

        this.burnedMatches = burnedMatches;
        this.playerOnTurn = playerOnTurn;
    }

    public Collection<Point> getBurnedMatches() {
        return burnedMatches;
    }

    public Player getPlayerOnTurn() {
        return playerOnTurn;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(playerOnTurn);
        out.writeObject(burnedMatches);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        playerOnTurn = (Player) in.readObject();
        burnedMatches = (Collection<Point>) in.readObject();
    }

}
