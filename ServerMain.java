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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ServerMain extends Application {
	
	private VBox window = new VBox();
	private Button quit = new Button("End session");
	private TextArea console = new TextArea();
	
	private int client_id = 0;
	
	private boolean first_client = true;
	
	private Channel broadcast;
	
	private HashMap<String,Channel> channel_map;
	
	private ArrayList<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();
	
	public static void main(String[] args) {
		
		ServerMain chatroom = new ServerMain();
		launch(args);
		
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		initializeStage();
		initializeServer();
		
		primaryStage.setTitle("Numan's ChatRoom Server");
		
		primaryStage.setScene(new Scene(window,400,500));
		primaryStage.show();
		
	}
	
	public void initializeStage() {
		
		window.getChildren().addAll(console,quit);
		
		console.setWrapText(true);
		
		console.setPrefSize(300, 300);
		
		quit.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				for(ClientHandler c : clientHandlers) {
					
					c.sendObject(new ChatUnit("!@#","Server","Server"));
					
				}
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.exit(0);
				
			}
			
		});
	}
	
	public void initializeServer() {
		
		broadcast = new Channel("broadcast");
		
		channel_map = new HashMap<String,Channel>();
		
		channel_map.put("broadcast", broadcast);
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					
					ServerSocket serverSocket = new ServerSocket(1337);
					
					while(true) {
						
						Socket clientSocket = serverSocket.accept();
						
						client_id++;
						
						ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
						ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
												
						ClientHandler new_user = new ClientHandler(client_id, input, output,null,clientSocket);
						
						//clientHandlers.add(new_user);
						
						broadcast.addClient(new_user);
						
						Thread new_user_thread = new Thread(new_user);
						
						new_user_thread.start();
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		}).start();
		
	}

	
	public class ClientHandler implements Runnable, Observer {
		
		public int client_id;
		
		public String client_name;
		
		public ArrayList<Channel> chats = new ArrayList<Channel>();
		
		public ObjectInputStream input;
		public ObjectOutputStream output;
		
		public Socket clientSocket;
		
		public ClientHandler(int id, ObjectInputStream input, ObjectOutputStream output,String user_name,Socket s) {
		
				client_id = id;
				this.input = input;
				client_name = user_name;
				this.output = output;
				
				this.clientSocket = s;

				chats.add(broadcast);
			
			
		}


		@Override
		public void run() {
			
			try {
				
				boolean go = true;
				boolean taken = false;
				
				ChatUnit user_name = (ChatUnit) input.readObject();
				
				if(user_name.message.equals("logging off") || user_name.message.equals("I goofed")) {
					output.close();
					input.close();
					clientSocket.close();
					
					go = false;
					taken = true;
					return;
				}

				

				for(ClientHandler c : clientHandlers) {
					if(c.client_name.equals(user_name.message)) {
						output.writeObject(new ChatUnit("username is taken","Server","Server"));
						taken = true;
						go = false;
					}
				} 
				
				if(!taken) {
					output.writeObject(new ChatUnit("go ahead","Server","Server"));
					client_name = user_name.message;
					//System.out.println(user_name.message + " has logged on");
					console.appendText(user_name.message + " has logged on\n");
					
					broadcast.sendMessage(new ChatUnit(user_name.message,"LeBron","broadcast"));
				}

				for(ClientHandler c : clientHandlers) {
					this.sendObject(new ChatUnit(c.client_name,"LeBron","newuser"));
				}
				
				clientHandlers.add(this);
				
			
				while(go) {
					
					
					
					@SuppressWarnings("deprecation")
					ChatUnit received = (ChatUnit) input.readObject();
					
					if(received.message.equals("logging off")) {
						input.close();
						output.close();
						for(Channel c : chats) {
							c.deleteObserver(this);
						}
						//System.out.println(client_name + " has logged off");
						console.appendText(client_name + " has logged off\n");
						
						broadcast.sendMessage(new ChatUnit("bbb",client_name,"forget me"));
						
						clientHandlers.remove(this);
						
						break;
					} else if(received.channel.equals("new unicast")) {
						
						Channel unicast = new Channel(client_name + "#" + received.message);
						chats.add(unicast);
						String unicast_name = client_name + "#" + received.message;
						
						ClientHandler partner = null;
						
						for(ClientHandler c : clientHandlers) {
							if(c.client_name.equals(received.message)) {
								partner = c;
								break;
							}
						}
						
						unicast.addObserver(this);
						unicast.addObserver(partner);
						
						channel_map.put(unicast_name, unicast);
						continue;
						
					} else if(received.channel.equals("new multicast")){
						
						//create a new channel named received.message, add all members as observers (using spliterator on #)
						
						String[] members = received.message.split("#");
						
						Channel group_chat = new Channel(received.message);
						
						chats.add(group_chat);
						
						ClientHandler member = null;
						
						for(String user : members) {
							
							for(ClientHandler c : clientHandlers) {
								if(user.equals(c.client_name)) {
									member = c;
									break;
								}
							}
							
							group_chat.addClient(member);
							
						}
						
						channel_map.put(received.message, group_chat);
						
						group_chat.sendMessage(new ChatUnit(received.message,"add to ur list","Server"));
						
						//System.out.println("groupchat functionality here...");
						
					} else {
					
						String channel_name = received.channel;
						Channel channel = channel_map.get(channel_name);
						
						channel.sendMessage(received);
						
						//System.out.print(received.sender + "@" + channel_name + ": " + received.message);
						console.appendText(received.sender + "@" + channel_name + ": " + received.message + "\n");
					}
					
					
					
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		
		}
		
		public void sendObject(ChatUnit o) {
			try {
				output.writeObject(o);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		@Override
		public void update(Observable o, Object arg) {
			
			sendObject((ChatUnit) arg);
			
		}
	}

}
