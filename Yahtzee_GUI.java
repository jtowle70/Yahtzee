package eecs285.proj4.Yahtzee;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URISyntaxException;

import javax.security.auth.kerberos.KerberosKey;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout.Alignment;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.xml.stream.events.EndDocument;

import org.w3c.dom.events.MouseEvent;

import eecs285.proj4.Yahtzee.ClientServerSocket;
import eecs285.proj4.Yahtzee.Dice;
import eecs285.proj4.Yahtzee.DieButton;
import eecs285.proj4.Yahtzee.Scoreboard;

//import eecs285.proj4.Yahtzee.Yahtzee_GUI.yahtzee_scorecard;

@SuppressWarnings("serial")
// import eecs285.proj4.Yahtzee.Yahtzee_GUI.yahtzee_scorecard;
/*
   This is the main UI for the Yahtzee game. It creates a window
   which displays the scores, rolls, and possible moves for each
   player.
*/
public class Yahtzee_GUI extends JFrame
{
   private JButton roll_button;
   private JLabel roll_countJLabel;
   private DieButton[] dice_buttons;
   private Scoreboard playerScorecard;
   private Dice dice;
   private ImageIcon[] dice_pictures;
   private Yahtzee_Listener listener;
   private JButton[] score_buttons;
   private JPanel[] player_panels;
   private JLabel[] player_score_labels;
   private int roll_count;
   private int this_player_index;
   private int num_players;
   private String[] player_names;
   private boolean got_bonus;
   private boolean in_lock_down;
   private ClientServerSocket client;
   private yahtzee_scorecard card;
   private Yahtzee_Rules_Frame rule_frame; 
   private JButton show_rules; 

   // this is just a test

   private final Border BLACKLINE = BorderFactory.createLineBorder(Color.black);
   private final Border REDLINE = BorderFactory.createLineBorder(Color.red);
   private final String ROLL_COUNT_STRING = "Current Roll: ";
   private final Color background_Color = new Color(85, 200, 50);
   private final Color button_Color = new Color(200, 200, 200);

   public Yahtzee_GUI(int _num_players, int seed, String[] players,
         String in_player_name, ClientServerSocket inClient)
   {
      // Main Window
      super("YAHTZEE");
      setLayout(new BorderLayout());
      num_players = _num_players;

      in_lock_down = true;
      getContentPane().setBackground(background_Color);
      listener = new Yahtzee_Listener();
      roll_count = 0;
      client = inClient;
      got_bonus = false;
      // dice
      player_names = new String[num_players];

      for (int i = 0; i < num_players; i++) {
         player_names[i] = players[i];
         if (players[i].equals(in_player_name))
            this_player_index = i;
      }
      card = new yahtzee_scorecard(players[this_player_index]);
      roll_button = new JButton("Roll Dice");
      roll_button.setBackground(button_Color);
      roll_countJLabel = new JLabel("Not Your Turn");
      dice_buttons = new DieButton[5];
      dice = new Dice(seed);
      dice_pictures = new ImageIcon[6];
      // score
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
      for (int i = 0; i < 13; i++) {
         score_buttons[i].addActionListener(listener);
         score_buttons[i].setBackground(button_Color);
      }

      // players
      player_panels = new JPanel[num_players];
      playerScorecard = new Scoreboard(in_player_name);

      // retrieve dice pictures
      File myDir = null;
      File[] fileList;

      try {
         myDir = new File(getClass().getClassLoader()
               .getResource("eecs285/proj4/Yahtzee/Dice_pictures").toURI());
      } catch (URISyntaxException uriExcep) {
         System.out.println("Caught a URI syntax exception");
         System.exit(4); // Just bail for simplicity in this project
      }

      fileList = myDir.listFiles();

      //Adding the dice images
      for (int i = 0; i < 6; i++) {
         dice_pictures[i] = new ImageIcon(fileList[i].toString());
      }

      // *******************************************************

      // Dice Panel
      JPanel dice_panel = new JPanel(new BorderLayout());
      dice_panel.setOpaque(false);
      JPanel dice_pic_panel = new JPanel(new FlowLayout());
      dice_pic_panel.setOpaque(false);
      for (int i = 0; i < 5; i++) {
         dice_buttons[i] = new DieButton(dice_pictures, 6);
         ;

         dice_pic_panel.add(dice_buttons[i]);
         dice_buttons[i].addActionListener(listener);
      }

      dice_panel.add(dice_pic_panel, BorderLayout.NORTH);
      JPanel rollPanel = new JPanel(new FlowLayout());
      rollPanel.setOpaque(false);
      rollPanel.add(roll_button);
      rollPanel.add(roll_countJLabel);

      //Creating the help button to display the rules when clicked
      JPanel help_panel = new JPanel(new BorderLayout());
      help_panel.add(rollPanel, BorderLayout.CENTER);
      rule_frame = new Yahtzee_Rules_Frame();
      rule_frame.display.addWindowListener(new Rules_Window_Listener());
                                                                         
      show_rules = new JButton("Game Info");
      show_rules.addActionListener(new Rules_Listener());
      help_panel.add(show_rules, BorderLayout.EAST); 
      help_panel.setBackground(new Color(85, 200, 50)); 

      dice_panel.add(help_panel, BorderLayout.SOUTH); 

      add(dice_panel, BorderLayout.SOUTH);
      update_dice();
      roll_button.addActionListener(listener);

      // players panels
      JPanel top_panel = new JPanel(new GridLayout(1, num_players));
      top_panel.setOpaque(false);
      player_panels = new JPanel[num_players];
      player_score_labels = new JLabel[num_players];

      for (int i = 0; i < num_players; i++) {
         player_panels[i] = new JPanel(new FlowLayout());
         player_panels[i].setBorder(BorderFactory.createTitledBorder(BLACKLINE,
               players[i]));
         player_score_labels[i] = new JLabel("Score: 000");
         player_panels[i].add(player_score_labels[i]);
         player_panels[i].setOpaque(false);
         top_panel.add(player_panels[i]);
      }
      add(top_panel, BorderLayout.NORTH);

      // scoreboard panel
      JPanel score_panel = new JPanel(new GridLayout(4, 7));
      score_panel.setOpaque(false);
      for (int i = 0; i < 6; i++) {
         score_panel.add(score_buttons[i]);
      }
      JLabel bonus_label = new JLabel("Bonus", SwingConstants.CENTER);
      score_panel.add(bonus_label);

      for (int i = 6; i < 13; i++) {
         score_panel.add(score_buttons[i]);
      }

      player_panels[0].setBorder(BorderFactory.createTitledBorder(REDLINE,
            player_names[0]));

      JLabel leftTempLabel = new JLabel();
      leftTempLabel.setBackground(new Color(85, 200, 50));
      leftTempLabel.setOpaque(true);
      JLabel rightTempLabel = new JLabel();
      rightTempLabel.setBackground(new Color(85, 200, 50));
      rightTempLabel.setOpaque(true);
      JPanel mid = new JPanel(new GridLayout(1, 3));
      mid.add(leftTempLabel);
      mid.add(card);
      mid.add(rightTempLabel);
      add(mid, BorderLayout.CENTER);
   }
   
   /*
      Listens for a click on the help button and displays 
      the game rules
   */
   public class Rules_Listener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         if (Yahtzee_Rules_Frame.open == false) {
            rule_frame.display.setVisible(true);
         }
      }
   }

   /*
      Altering the close button action on the help frame.
   */
   public class Rules_Window_Listener implements WindowListener
   { // add this

      public void windowClosing(WindowEvent e)
      {
         Yahtzee_Rules_Frame.open = false;
         rule_frame.display.setVisible(false);
      }

      @Override
      public void windowActivated(WindowEvent arg0)
      {
         // TODO Auto-generated method stub

      }

      @Override
      public void windowClosed(WindowEvent arg0)
      {
         // TODO Auto-generated method stub

      }

      @Override
      public void windowDeactivated(WindowEvent arg0)
      {
         // TODO Auto-generated method stub

      }

      @Override
      public void windowDeiconified(WindowEvent arg0)
      {
         // TODO Auto-generated method stub

      }

      @Override
      public void windowIconified(WindowEvent arg0)
      {
         // TODO Auto-generated method stub

      }

      @Override
      public void windowOpened(WindowEvent arg0)
      {
         // TODO Auto-generated method stub

      }
   }

   /*
      Controls the actions taken when a button is clicked,
      keeping track of the actions of the player
   */
   public class Yahtzee_Listener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         if (in_lock_down == false) {
            if (e.getSource() == roll_button) {
               if (roll_count < 3) {
                  roll_dice();
               }
               roll_count++;
            } else {
               if (!dice_buttons_pressed(e)) {
                  score_buttons_pressed(e);
               }
            }
         }
      }
   }

  
   //A function to reciece data from the server.
   void get_Server_data()
   {
      while (true) {
         String recieved_string = "";
         while ("".equals(recieved_string)) {
            recieved_string = client.recvString();
         }
         if ("Update Score".equals(recieved_string)) {
            String last_player_nameString = client.recvString();
            String messageString = client.recvString();
            if (!last_player_nameString.equals(player_names[this_player_index])) {
               JOptionPane.showMessageDialog(this, messageString);
            }
            for (int i = 0; i < player_names.length; i++) {
               int score = client.recvInt();
               String score_str = Integer.toString(score);
               if (score_str.length() == 2) {
                  score_str = "Score: 0" + score_str;
               } else if (score_str.length() == 1) {
                  score_str = "Score: 00" + score_str;
               } else {
                  score_str = "Score: " + score_str;
               }
               player_score_labels[i].setText(score_str);
               if (player_score_labels[i].getText().length() != 10) {
                  System.out.println("Not fucking long enough");
               }
            }

            String in_player_turn = client.recvString();
            for (int i = 0; i < player_names.length; i++) {
               if (player_names[i].equals(in_player_turn)) {
                  player_panels[i].setBorder(BorderFactory.createTitledBorder(
                        REDLINE, player_names[i]));

                  if (i - 1 < 0)
                     i = player_names.length;
                  player_panels[i - 1].setBorder(BorderFactory
                        .createTitledBorder(BLACKLINE, player_names[i - 1]));
                  break;
               }
            }
            paint(getGraphics());
            if (player_names[this_player_index].equals(in_player_turn)) {
               break;
            }
            if ("Game Over".equals(in_player_turn)) {
               String winner_name = client.recvString();
               int winner_score = client.recvInt();
               JOptionPane.showMessageDialog(this, "Congradulations "
                     + winner_name + "!!\nFinal Score of " + winner_score);
               setVisible(false);
               dispose();
            }
         }
      }
      start_turn();

   }

   //Beggining the turn of the next player
   void start_turn()
   {
      in_lock_down = false;
      roll_dice();
      roll_count = 1;
   }

   //Update the values of the die after a player rolls
   //or at the beggining of a turn
   private void roll_dice()
   {
      dice.roll();
      update_dice();
      int[] possible_scores = playerScorecard.get_possible_scores(dice
            .get_dice_values());

      update_labels(possible_scores);
   }

   //Lock or unlock the die depending on what the player
   //wishes to keep or reroll
   private boolean dice_buttons_pressed(ActionEvent e)
   {
      for (int i = 0; i < 5; i++) {
         if (e.getSource() == dice_buttons[i]) {
            dice.toggle_lock_die(i);
            return true;
         }
      }
      return false;
   }

   //Updates the players score according to what move 
   //the player made. Disable that move in future turns
   //and end the players turn
   private void score_buttons_pressed(ActionEvent e)
   {
      for (int j = 0; j < 13; j++) {
         if (e.getSource() == card.scores[j]) {
            if (card.scores[j].isEnabled()) {
               card.scores[j].setEnabled(false);
               playerScorecard.insert_new_score(j,
                     Integer.parseInt(card.scores[j].getText()));
               /*
                * player_score_labels[this_player_index].setText(
                * playerScorecard.get_score()+"");
                */
               card.scores[j].setForeground(Color.black);
               end_turn(j);
            }
         }
      }
   }

   //Updates the display of the die, animating the roll.
   private void update_dice()
   {
      int[] dice_values = dice.get_dice_values();
      int j = 0;
      for (int i = 0; i < 5; i++) {
         if (dice.is_die_locked(i) == false) {
            dice_buttons[i].animate(dice_values[i], 12 + (j * 12));
            j++;
         }
      }
      roll_countJLabel.setText(ROLL_COUNT_STRING + (roll_count + 1));
   }

   //Updates the possible moves a player can make, letting them
   //know the possible scores by highlighting the number in red
   private void update_labels(int[] possible_scores)
   {
      for (int i = 0; i < 13; i++) {
         if (possible_scores[i] != -1) {
            card.scores[i].setText(possible_scores[i] + "");
            card.scores[i].setForeground(Color.red);
         }
      }
   }

   //Check if a player has earned the bonus.
   private boolean update_check_bonus()
   {
      if (playerScorecard.check_bonus()) {
         card.bonusScore.setText("35");
         return true;
      }
      return false;
   }

   //Call an end to the current players turn,
   //unlocking the die and disabling button
   //clicks
   private void end_turn(int index)
   {
      for (int i = 0; i < 5; i++) {
         if (dice.is_die_locked(i)) {
            dice_buttons[i].doClick();
         }
      }
      roll_countJLabel.setText("Not Your Turn");
      in_lock_down = true;
      if (got_bonus == false) {
         got_bonus = update_check_bonus();
      }
      roll_count = 0;
      // put a glass panel over the UI so that nothing can be touched.
      // send back to the network, name and current score.
      client.sendString("Send Score");
      client.sendString(player_names[this_player_index]);
      client.sendString(card.labels[index + 1].getText().trim() + " for "
            + card.scores[index].getText().trim());
      client.sendInt(playerScorecard.get_score());
      get_Server_data();
   }

   /*
      This class controls the GUI of the scorecard displayed 
      in the main frame. Displaying scores and possible moves
      to the user as they play the game.
   */
   private class yahtzee_scorecard extends JPanel
   {

      public JLabel[] labels = new JLabel[14];
      public final Border BLACKLINE = BorderFactory
            .createLineBorder(Color.black);
      public final Dimension size = new Dimension(120, 30);
      private Yahtzee_Listener listener = new Yahtzee_Listener();
      public JButton[] scores;
      public JLabel bonusLabel;
      public JLabel bonusScore;

      public yahtzee_scorecard(String name)
      {
         add(new JLabel("  "));
         setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
         JPanel choices = new JPanel(new GridLayout(15, 1));
         //Adding the labels to the card
         labels[0] = new JLabel();
         labels[1] = new JLabel(" Ones");
         labels[2] = new JLabel(" Twos");
         labels[3] = new JLabel(" Threes");
         labels[4] = new JLabel(" Fours");
         labels[5] = new JLabel(" Fives");
         labels[6] = new JLabel(" Sixes");
         labels[7] = new JLabel(" Three of a Kind    ");
         labels[8] = new JLabel(" Four of a Kind");
         labels[9] = new JLabel(" Full House");
         labels[10] = new JLabel(" Small Straight");
         labels[11] = new JLabel(" Large Straight");
         labels[12] = new JLabel(" Chance");
         labels[13] = new JLabel(" Yahtzee!");
         bonusLabel = new JLabel(" Bonus ");
         bonusScore = new JLabel("0");
         bonusLabel.setBorder(BLACKLINE);
         bonusLabel.setBackground(Color.WHITE);
         bonusLabel.setOpaque(true);
         bonusScore.setBorder(BLACKLINE);
         bonusScore.setBackground(Color.WHITE);
         bonusScore.setOpaque(true);
         bonusScore.setHorizontalAlignment(SwingConstants.CENTER);

         for (int i = 0; i < 14; i++) {
            labels[i].setBorder(BLACKLINE);
            labels[i].setBackground(Color.WHITE);
            labels[i].setOpaque(true);
            labels[i].setPreferredSize(size);
            if (i == 7)
               choices.add(bonusLabel);
            choices.add(labels[i]);
         }
         add(choices);
         //Adding the buttons to the card
         scores = new JButton[13];
         JPanel player = new JPanel(new GridLayout(15, 1));
         JLabel p = new JLabel(" " + name + " ");
         p.setPreferredSize(new Dimension(90, 30));
         p.setHorizontalAlignment(SwingConstants.CENTER);
         p.setBorder(BLACKLINE);
         p.setBackground(Color.WHITE);
         p.setOpaque(true);
         player.add(p);
         for (int j = 0; j < 13; j++) {
            scores[j] = new JButton();
            scores[j].setBackground(Color.white);
            scores[j].setBorder(BLACKLINE);
            scores[j].setOpaque(true);
            scores[j].addActionListener(listener);
            scores[j].setEnabled(true);
            if (j == 6)
               player.add(bonusScore);
            player.add(scores[j]);
         }
         add(player);
         setBackground(new Color(85, 200, 50));
         add(new JLabel(" "));
      }
   }
}
