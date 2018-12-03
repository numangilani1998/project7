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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientMain extends Application {
	
	//VIEW
	
	public static BorderPane client_window = new BorderPane();
	public static BorderPane chat_box = new BorderPane();
	public static TextArea chat_field = new TextArea();
	public static TextField chat_input = new TextField();
	public static Button send_message = new Button("Send");
	public static MenuButton users_list = new MenuButton("Select user to chat with");
	public static Button exit_chat_room = new Button("Leave Chat Room");
	public static Button add_channel = new Button("Add Broadcast Channel");
	
	public static HBox chat_input_area = new HBox();
	
	public static TextArea curr_channel = new TextArea("Current channel: broadcast");
	
	public static HBox user_name_area = new HBox();
	public static TextField name = new TextField("Enter username here");
	public static Button create_name = new Button("Create username");
	
	//groupchat list
	public static VBox create_chats_box = new VBox(100);
	
	public static VBox group_chat_box = new VBox(10);
	public static MenuButton make_group = new MenuButton("Select groupchat members");
	public static Button make_groupchat = new Button("Create groupchat");
	
	public static MenuButton existing_groups = new MenuButton("Select groupchat channel");
	
	public static TextField server_ip = new TextField("Enter server IP here");
	public static Button connect = new Button("Connect to Server");
	public static HBox connection = new HBox();
	
	
	
	
	//CONTROLLER
	
	//public Socket my_socket;
	public String potential_name;
	public String client_name = "anon";
	public String current_channel = "broadcast";
	public ArrayList<String> chatting_users = new ArrayList<String>();
	
	public Socket my_socket;
	
	public ObjectOutputStream outgoing_objects;
	public ObjectInputStream incoming_objects;
	public Thread input_listener;


	public static void main(String[] args) throws Exception{
		
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		connection.getChildren().addAll(server_ip,connect);
		
		connect.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				String ip = server_ip.getText();
				
				try {
					
					my_socket = new Socket(ip,1337);
					outgoing_objects = new ObjectOutputStream(my_socket.getOutputStream());
					incoming_objects = new ObjectInputStream(my_socket.getInputStream());
					
					initializeStage();
					
					primaryStage.setScene(new Scene(client_window, 500, 400));
					primaryStage.show();
					
					input_listener.start();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
		
		primaryStage.setTitle("Numan's ChatRoom");
		primaryStage.setScene(new Scene(connection, 500, 400));
		primaryStage.show();
		
		input_listener = new Thread(new Runnable() {

			@Override
			public void run() {
				
				while(true) {
					
					try {
						ChatUnit received = (ChatUnit) incoming_objects.readObject();
						if(received.message == null) System.out.println(received.sender + ":" + received.channel);
						if(received.message.equals("go ahead")) {
							client_name = potential_name;
							chat_field.appendText("WELCOME TO NUMAN'S CHATROOM\n");
						} else if(received.sender.equals("LeBron")){
							
							if(!chatting_users.contains(received.message)) {
								MenuItem new_user = new MenuItem(received.message);
							
								chatting_users.add(received.message);
								
								new_user.setOnAction(new EventHandler<ActionEvent>() {

									@Override
									public void handle(ActionEvent event) {
										
										chat_field.appendText("CHATTING WITH " + received.message.toUpperCase() + "\n");
										
										current_channel = client_name + "#" + received.message;
										curr_channel.setText("Current channel: " + current_channel);
										
										String request = received.message;
										
										try {
											outgoing_objects.writeObject(new ChatUnit(request,client_name,"new unicast"));
										} catch (IOException e) {
											e.printStackTrace();
										}
										
									}
									
								});
								
								users_list.getItems().add(new_user);
								
								CheckMenuItem gc_item = new CheckMenuItem(received.message);
								
								make_group.getItems().add(gc_item);
								
								
							} else {
								//current_channel = client_name + "#" + received.message;
							}
							
							
						} else if(received.channel.equals("forget me")){
							
							MenuItem to_remove = null;
							CheckMenuItem to_remove_groups = null;
							
							for(MenuItem c : users_list.getItems()) {
								if(c.getText().equals(received.sender)) {
									to_remove = c;
									break;
								}
							}
							
							for(MenuItem c : make_group.getItems()) {
								if(c.getText().equals(received.sender)) {
									to_remove_groups = (CheckMenuItem) c;
									break;
								}
							}
							
							if(current_channel.contains(received.sender)) {
								chat_field.appendText("YOU'VE BEEN SWITCHED TO THE BROADCAST CHANNEL \n");
								current_channel = "broadcast";
								curr_channel.setText("Current channel: " + current_channel);
							}
							
							ArrayList<MenuItem> finished_groups = new ArrayList<MenuItem>();
							
							for(MenuItem c : existing_groups.getItems()) {
								
								if(c.getText().contains(received.sender)) {
									finished_groups.add(c);
								}
								
							}
							
							existing_groups.getItems().removeAll(finished_groups);
							
							users_list.getItems().remove(to_remove);
							make_group.getItems().remove(to_remove_groups);
							
						} else if(received.sender.equals("add to ur list")) {
							
							MenuItem new_group = new MenuItem(received.message);
							
							existing_groups.getItems().add(new_group);
							
							new_group.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(ActionEvent event) {
									
									current_channel = new_group.getText();
									curr_channel.setText("Current channel: " + current_channel);
									
									chat_field.appendText("CHATTING ON CHANNEL: " + received.message.toUpperCase() + "\n");
									
								}
								
							});
						} else if(received.message.equals("!@#")){
							
							try {
								outgoing_objects.writeObject(new ChatUnit("logging off",client_name,"Server"));
								//input_listener.stop();
								outgoing_objects.close();
								incoming_objects.close();
								Thread.sleep(10);
								my_socket.close();
								System.exit(0);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
						
						
						
							chat_field.appendText(received.sender + "@" + received.channel + ": " + received.message);
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				
			}
			
		});
		
	}
	
	public void initializeStage() {
		
		chat_field.setPrefSize(300,300);
		
		chat_field.setWrapText(true);
		
		chat_box.setCenter(chat_field);
		
		curr_channel.setPrefSize(200, 10);
		
		user_name_area.getChildren().addAll(name,create_name,curr_channel);
		
		client_window.setTop(user_name_area);
		
		MenuItem broadcast = new MenuItem("broadcast");
		
		broadcast.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				chat_field.appendText("On BROADCAST CHANNEL" + "\n");
				
				current_channel = "broadcast";
				curr_channel.setText("Current channel: " + current_channel);
								
			}
			
		});
		
		users_list.getItems().add(broadcast);
		
		create_name.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				potential_name = name.getText();
				ChatUnit msg = new ChatUnit(potential_name,client_name,"Server");
				
				try {
					outgoing_objects.writeObject(msg);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
		
		
		chat_input_area.getChildren().setAll(chat_input,send_message);
		
		chat_box.setBottom(chat_input_area);
		
		chat_box.setPrefSize(300, 300);
		
		send_message.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				try {
					
					if(client_name.equals("anon")) {
						outgoing_objects.writeObject(new ChatUnit("I goofed",client_name,"Server"));
						input_listener.stop();
						outgoing_objects.close();
						incoming_objects.close();
						Thread.sleep(10);
						my_socket.close();
						System.exit(0);
					}
					
					ChatUnit msg = new ChatUnit(chat_input.getText() + "\n",client_name,current_channel);
					outgoing_objects.writeObject(msg);
					chat_input.clear();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
			
		});
		
		client_window.setLeft(chat_box);
		
		group_chat_box.getChildren().addAll(make_group,make_groupchat);
		
		create_chats_box.getChildren().addAll(users_list,group_chat_box,existing_groups);
		
		
		
		client_window.setRight(create_chats_box);
		
		client_window.setBottom(exit_chat_room);
		
		make_groupchat.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				ArrayList<CheckMenuItem> to_add = new ArrayList<CheckMenuItem>();
				
				for(MenuItem m : make_group.getItems()) {
					
					CheckMenuItem user = (CheckMenuItem) m;
					
					if(user.isSelected()) {
						to_add.add(user);
					}
					
				}
				
				String group_chat_name = client_name;
				
				for(CheckMenuItem c : to_add) {
					group_chat_name += "#";
					group_chat_name += c.getText();
				}
				
				ChatUnit group_request = new ChatUnit(group_chat_name,client_name,"new multicast");
				
				try {
					outgoing_objects.writeObject(group_request);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
		});
		
		exit_chat_room.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				try {
					outgoing_objects.writeObject(new ChatUnit("logging off",client_name,"Server"));
					input_listener.stop();
					outgoing_objects.close();
					incoming_objects.close();
					Thread.sleep(10);
					my_socket.close();
					System.exit(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		});
		
	}


}
