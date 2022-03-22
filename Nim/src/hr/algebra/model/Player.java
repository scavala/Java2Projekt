/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

/**
 *
 * @author Sime
 */
public class Player implements Externalizable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String id;

    public Player(String name) {
        this.name = name;
        id = UUID.randomUUID().toString();

    }

    public Player() {
    }

    @Override
    public boolean equals(Object obj) {

        return obj instanceof Player ? this.id.equals(((Player) obj).id) : false;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(id);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        id = in.readUTF();
    }

}
