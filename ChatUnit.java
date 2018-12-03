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

import java.io.Serializable;
import java.util.ArrayList;

public class ChatUnit implements Serializable {

	String message;
	String channel;
	String sender;
	
	public ChatUnit(String message, String sender, String channel) {
		
		this.message = message;
		this.sender = sender;
		this.channel = channel;
		
	}
}
