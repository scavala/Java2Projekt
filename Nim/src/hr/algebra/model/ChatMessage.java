/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.model;

import java.io.Serializable;

/**
 *
 * @author zakesekresa
 */
public class ChatMessage implements Serializable{

    private final String content;
    private final String author;

    public ChatMessage(String content, String author) {
        this.content = content;
        this.author = author;
    }

    @Override
    public String toString() {
        return author + ": " + content;
    }

}
