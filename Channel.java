/* CHAT ROOM <MyClass.java>
 * EE422C Project 7 submission by
 * Replace <...> with your actual data.
 * Numan Gilani
 * ng22457
 * 16345
 * Slip days used: <0>
 * Fall 2018
 */

package assignment7;

import java.io.BufferedReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;

import assignment7.ServerMain.ClientHandler;

public class Channel extends Observable {
	
	String channel_name;
	
	public Channel(String name) {
		
		channel_name = name;
		
		
		
	}
	
	@SuppressWarnings("deprecation")
	public void sendMessage(ChatUnit msg) {
		
		setChanged();
		notifyObservers(msg);
	}
	
	
	@SuppressWarnings("deprecation")
	public void addClient(ClientHandler c) {
		addObserver(c);
	}

}
