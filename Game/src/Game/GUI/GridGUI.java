package Game.GUI;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

import Common.mazePair;
import Game.Player.Player;
import Game.Player.PlayerType;
import Game.State.GameGlobalState;

public class GridGUI implements PropertyChangeListener{
    private JFrame mainFrame;

    private JPanel infoPanel;
    private JPanel mazePanel;

    private DefaultTableModel infoTable;
    JLabel[][] mazeLabels;

    private int N;

    private GameGlobalState ggs;

    public GridGUI(){

    }

    public void initialization(GameGlobalState ggs, String name, int mazeSize){
        this.ggs = ggs;
        N = mazeSize;

        mainFrame = new JFrame(name);
        mainFrame.setSize(1600,800);
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

        Vector<String> columnNames = new Vector<String>();
        columnNames.addElement("Player");
        columnNames.addElement("Score");
        columnNames.addElement("Type");
        infoTable = new DefaultTableModel(new Vector<Object>(), columnNames);
        JTable table = new JTable(infoTable);
        table.setEnabled(false);
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
        infoPanel.add(table, BorderLayout.CENTER);
        // right panel, the maze grid showing players and treasure location
        JPanel mazePanel = new JPanel();
        mazePanel.setLayout(new GridLayout(mazeSize, mazeSize, 0, 0));
        Border blackline = BorderFactory.createLineBorder(Color.black);
        mazeLabels = new JLabel[mazeSize][mazeSize];
        for (int i = 0; i < mazeSize; i++)
        {
            for (int j = 0; j < mazeSize; j++)
            {
                mazeLabels[i][j] = new JLabel("", SwingConstants.CENTER);
                mazeLabels[i][j].setBorder(blackline);
                mazePanel.add(mazeLabels[i][j]);
            }
        }

        //JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoPanel, mazePanel);
        //mainFrame.getContentPane().add(splitPane);

        mainFrame.add(infoPanel);
        mainFrame.add(mazePanel);
        mainFrame.setVisible(true);

        updateGlobalState();
        ggs.addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("Listening");
        updateGlobalState();
    }


    private void updateGlobalState(){

        Vector infoVector = infoTable.getDataVector();
        infoVector.clear();
        clearMazeLabels();
        Map<String, Player> playerMap = this.ggs.getPlayersMap();
        List<mazePair> treasuresLocation = this.ggs.getTreasuresLocation();
        for (Map.Entry<String, Player> entry : playerMap.entrySet()) {
            Vector<Object> row = new Vector<Object>();
            row.addElement(entry.getKey());
            row.addElement(entry.getValue().getScore());
            PlayerType pt = entry.getValue().getType();
            if(pt!= PlayerType.Standard){
                row.addElement(pt.toString());
            }else
            {
                row.addElement("");
            }

            infoVector.addElement(row);
        }
        infoTable.fireTableDataChanged();

        for(int i=0; i<treasuresLocation.size(); i++){
            mazePair t = treasuresLocation.get(i);
            String s = mazeLabels[N-1-t.getRow()][t.getColumn()].getText();
            mazeLabels[N-1-t.getRow()][t.getColumn()].setText(s+"*");
        }
        for (Map.Entry<String, Player> entry : playerMap.entrySet()) {
            mazePair t= entry.getValue().getCurrentPosition();
            String s = mazeLabels[N-1-t.getRow()][t.getColumn()].getText();
            mazeLabels[N-1-t.getRow()][t.getColumn()].setText(s+entry.getKey());
        }

    }

    private void clearMazeLabels(){
        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < N; j++)
            {
                mazeLabels[i][j].setText("");
            }
        }
    }
}
