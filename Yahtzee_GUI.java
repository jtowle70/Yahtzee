package eecs285.proj4.Yahtzee;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URISyntaxException;

import javax.security.auth.kerberos.KerberosKey;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.xml.stream.events.EndDocument;

import org.w3c.dom.events.MouseEvent;

@SuppressWarnings("serial")
public class Yahtzee_GUI extends JFrame {
	private JButton roll_button;
	private JLabel roll_countJLabel;
	private JToggleButton [] dice_buttons;
	private Scoreboard playerScorecard;
	private Dice dice;
	private ImageIcon[] dice_pictures;
	private Yahtzee_Listener listener;
	private JButton [] score_buttons;
	private JLabel [] score_labels;
	private JPanel [] player_panels;
	private JLabel [] player_score_labels;
	private int roll_count;
	private int this_player_index;
	private String[] player_names;
	private boolean got_bonus;
	private ClientServerSocket client;
	
	private JPanel glass;
	//this is just a test
	
	private final Border BLACKLINE = BorderFactory.createLineBorder(Color.black);
	private final Border REDLINE= BorderFactory.createLineBorder(Color.red);
	private final String ROLL_COUNT_STRING = "Current Roll: ";
	private final Color background_Color = new Color(85, 200,50);
	private final Color button_Color = new Color(200,200,200);
	public Yahtzee_GUI(int num_players, int seed, String [] players, String in_player_name, ClientServerSocket inClient) {
		//Main Window
		super("YAHTZEE");
		setLayout(new BorderLayout());
		
		getContentPane().setBackground(background_Color);
		listener = new Yahtzee_Listener();
		Glass_Pane_Listener glass_listener = new Glass_Pane_Listener();
		roll_count = 0;
		client = inClient;
		got_bonus=false;
		glass = new JPanel();
		glass.setPreferredSize(getContentPane().getSize());
		glass.addMouseListener(glass_listener);
		setGlassPane(glass);
		glass.setOpaque(false);
		glass.setVisible(true);
		//dice 
		player_names = new String[num_players];
		
		for(int i=0;i<num_players;i++){
			player_names[i]= players[i]; 
			if(players[i].equals(in_player_name)) this_player_index = i;
		}
		roll_button = new JButton("Roll Dice");
		roll_button.setBackground(button_Color);
		roll_countJLabel = new JLabel(ROLL_COUNT_STRING + (roll_count+1));
		dice_buttons = new JToggleButton[5];
		dice = new Dice(seed);
		dice_pictures = new ImageIcon[6];
		//score
		score_buttons = new JButton[13];
		score_buttons[0] = new JButton("Ones");
		score_buttons[1] = new JButton("Twos");
		score_buttons[2] = new JButton("Threes");
		score_buttons[3] = new JButton("Fours");
		score_buttons[4] = new JButton("Fives");
		score_buttons[5] = new JButton("Sixes");
		score_buttons[6] = new JButton("3 of a Kind");
		score_buttons[7] = new JButton("4 of a Kind");
		score_buttons[8] = new JButton("Full House");
		score_buttons[9] = new JButton("Small Staight");
		score_buttons[10] = new JButton("Large Staight");
		score_buttons[11] = new JButton("Chance");
		score_buttons[12] = new JButton("Yahtzee!");
		for(int i=0; i < 13; i++){
			score_buttons[i].addActionListener(listener);
			score_buttons[i].setBackground(button_Color);
		}
		
		score_labels = new JLabel[14];
		for(int i=0;i<14;i++){
			score_labels[i] = new JLabel();
			score_labels[i].setHorizontalAlignment(SwingConstants.CENTER);
		}
		//players
		player_panels = new JPanel[num_players];
		player_score_labels = new JLabel[num_players];
		playerScorecard = new Scoreboard(in_player_name);
		
		//retrieve dice pictures
	   File myDir = null;
	   File [] fileList;
	   
	    try
	    {
	      myDir = new File(getClass().getClassLoader().getResource(
	             "eecs285/proj4/Yahtzee/Dice_pictures").toURI());
	    }
	    catch (URISyntaxException uriExcep)
	    {
	      System.out.println("Caught a URI syntax exception");
	      System.exit(4); //Just bail for simplicity in this project
	    }

	    fileList = myDir.listFiles();
	    
	    for(int i = 0; i < 6; i++) {
	    	dice_pictures[i] = new ImageIcon(fileList[i].toString());
	    }

	    
		//*******************************************************
	    
		//Dice Panel
		JPanel dice_panel = new JPanel(new BorderLayout());
		dice_panel.setOpaque(false);
		JPanel dice_pic_panel = new JPanel(new FlowLayout());
		dice_pic_panel.setOpaque(false);
		for(int i = 0; i < 5; i++){
			dice_buttons[i]= new JToggleButton();
	
			dice_pic_panel.add(dice_buttons[i]);
			dice_buttons[i].addActionListener(listener);
		}
		
		dice_panel.add(dice_pic_panel, BorderLayout.NORTH);
		JPanel rollPanel = new JPanel(new FlowLayout());
		rollPanel.setOpaque(false);
		rollPanel.add(roll_button);
		rollPanel.add(roll_countJLabel);
		dice_panel.add(rollPanel, BorderLayout.SOUTH);
		
		add(dice_panel, BorderLayout.SOUTH);
		update_dice();
		roll_button.addActionListener(listener);

		//players panels
		JPanel top_panel = new JPanel(new GridLayout(1, num_players));
		top_panel.setOpaque(false);
		player_panels = new JPanel[num_players];
		player_score_labels = new JLabel[num_players];
		
		for(int i=0; i < num_players; i++){
			player_panels[i]= new JPanel( new FlowLayout());
			player_panels[i].setBorder(
					BorderFactory.createTitledBorder(BLACKLINE, players[i]));
			player_score_labels[i] = new JLabel("0");
			player_panels[i].add(player_score_labels[i]);
			player_panels[i].setOpaque(false);
			top_panel.add(player_panels[i]);
		}
		add(top_panel, BorderLayout.NORTH);
		
		//scoreboard panel
		JPanel score_panel = new JPanel(new GridLayout(4, 7));
		score_panel.setOpaque(false);
		for(int i = 0; i < 6; i++) {
			score_panel.add(score_buttons[i]);
		}
		JLabel bonus_label = new JLabel("Bonus", SwingConstants.CENTER);
		score_panel.add(bonus_label);
		for(int i = 0; i < 6; i++) {
			score_panel.add(score_labels[i]);
		}
		score_panel.add(score_labels[13]);
		
		for(int i = 6; i < 13; i++) {
			score_panel.add(score_buttons[i]);
		}		
		for(int i=6; i<13; i++){
			score_panel.add(score_labels[i]);
		}
		add(score_panel, BorderLayout.CENTER);
		player_panels[0].setBorder(
								BorderFactory.createTitledBorder(REDLINE, player_names[0]));

	}
	
	public class Yahtzee_Listener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == roll_button){
				if(roll_count <3){
					roll_dice();
				}
				roll_count++;
			}else{
				if(!dice_buttons_pressed(e)){
					score_buttons_pressed(e);
				}
			}
		}
	}
	
	public class Glass_Pane_Listener implements MouseListener{
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
	}
	
	//we need a function to listen for news from the network
	//Then it will update all the scores and whose players turn it is
	//if it is this Gui player turn, it will call start turn.
	
	void get_Server_data(){
		while(true){
			String recieved_string = "";
			while("".equals(recieved_string)){
				recieved_string = client.recvString();
			}
			if("Update Score".equals(recieved_string)){
				for(int i =0; i < player_names.length ; i++){
					int score = client.recvInt();
					String score_str = Integer.toString(score);
					player_score_labels[i].setText(score_str);
					
				}
				
				String in_player_turn = client.recvString();
				for(int i=0; i < player_names.length; i++){
					if(player_names[i].equals(in_player_turn)){
						player_panels[i].setBorder(
								BorderFactory.createTitledBorder(REDLINE, player_names[i]));
						
						if(i-1 < 0) i = player_names.length;
						player_panels[i-1].setBorder(
								BorderFactory.createTitledBorder(BLACKLINE, player_names[i-1]));
						break;
					}
				}
				paint(getGraphics());
				if(player_names[this_player_index].equals(in_player_turn)){
					break;
				}
			}
		}
		start_turn();
		
	}
	
	void start_turn(){
		//remove the glass panel,
		//roll the dice, and set the roll button correctly.
		glass.setVisible(false);
		roll_dice();
		roll_count=1;
	}
	
	private void roll_dice(){
		dice.roll();
		update_dice();
		int [] possible_scores = playerScorecard.get_possible_scores(
				dice.get_dice_values());
		
		update_labels(possible_scores);
	}
	
	private boolean dice_buttons_pressed(ActionEvent e){
		for(int i=0;i<5;i++){
			if(e.getSource() == dice_buttons[i]){
				dice.toggle_lock_die(i);
				return true;
			}
		}
		return false;
	}
	
	private void score_buttons_pressed(ActionEvent e){
		for(int i=0;i<13;i++){
			if(e.getSource() == score_buttons[i]){
				if(score_buttons[i].isEnabled()){
					score_buttons[i].setEnabled(false);
					playerScorecard.insert_new_score(i, 
							Integer.parseInt(score_labels[i].getText()));
					player_score_labels[this_player_index].setText(
							Integer.toString(playerScorecard.get_score()));
					score_labels[i].setForeground(Color.gray);
					end_turn();
				}
			}
		}
	}
	
	private void update_dice(){
		int[] dice_values = dice.get_dice_values();
		for(int i=0;i<5;i++){
			dice_buttons[i].setIcon(dice_pictures[dice_values[i]-1]);
		}
		roll_countJLabel.setText(ROLL_COUNT_STRING + (roll_count+1));
	}
	
	
	private void update_labels(int [] possible_scores){
		for(int i = 0; i < 13; i++){
			if(possible_scores[i]!=-1){
				score_labels[i].setText(Integer.toString(possible_scores[i]));
			}
		}
	}
	
	private boolean update_check_bonus(){
		if(playerScorecard.check_bonus()){
			player_score_labels[this_player_index].setText(
					Integer.toString(playerScorecard.get_score()));
			score_labels[13].setText("35");
			return true;
		}
		return false;
	}
	
	private void end_turn(){
		for(int i=0; i<5; i++){
			if(dice.is_die_locked(i)){
				dice_buttons[i].doClick();
			}
		}
		
		if(got_bonus==false){
			got_bonus = update_check_bonus();
		}
		roll_count=0;
		//put a glass panel over the UI so that nothing can be touched. 
		//send back to the network, name and current score.
		glass.setVisible(true);
		client.sendString("Send Score");
		client.sendString(player_names[this_player_index]);
		client.sendInt(playerScorecard.get_score());
		get_Server_data();
	}
	

	
}
