package Game.GUI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import Game.State.GameGlobalState;

public class GridGUI {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel infoPanel;
    private JPanel mazePanel;

    public GridGUI(){

    }

    public void initialization( String name){
        mainFrame = new JFrame(name);
        mainFrame.setSize(800,800);
        mainFrame.setLayout(new GridLayout(1, 2));

        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });


        infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        mazePanel = new JPanel();
        mazePanel.setLayout(new BorderLayout());




        mainFrame.add(infoPanel);
        mainFrame.add(mazePanel);
        mainFrame.setVisible(true);

    }



    public boolean updateGlobalState(GameGlobalState updateState){

        return false;
    }
}
