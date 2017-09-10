package Game.UI;

import javax.swing.*;
import java.awt.*;

public class game {

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("MazeGame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GridLayout experimentLayout = new GridLayout(5,5);
        frame.setLayout(experimentLayout);

        frame.add(new JLabel("Player 1"));
        frame.add(new JLabel("Player 2"));
        frame.add(new JLabel("Player 3"));
        frame.add(new JLabel("Player 4"));
        frame.add(new JLabel("treasure"));

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {



        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
