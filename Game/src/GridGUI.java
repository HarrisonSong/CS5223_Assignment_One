import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

public class GridGUI implements PropertyChangeListener{
    private JFrame mainFrame;

    private JPanel infoPanel;
    private JPanel mazePanel;

    private DefaultTableModel infoTable;
    JLabel[][] mazeLabels;

    private int N;

    private GameGlobalState globalState;
    public GridGUI(){}

    public void initialization(GameGlobalState globalState, String name, int mazeSize){
        System.out.println("Player " + name + " starts initializing the UI.");
        this.globalState = globalState;
        N = mazeSize;

        mainFrame = new JFrame(name);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setSize(screenSize.width, screenSize.height);
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

        Vector<String> columnNames = new Vector<>();
        columnNames.addElement("Player");
        columnNames.addElement("Score");
        columnNames.addElement("Type");
        infoTable = new DefaultTableModel(new Vector<>(), columnNames);
        JTable table = new JTable(infoTable);
        table.setRowHeight(20);
        table.setFont(new Font(table.getFont().getName(), Font.BOLD, 16));
        table.setEnabled(false);
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
        infoPanel.add(table, BorderLayout.CENTER);

        JPanel mazePanel = new JPanel();
        mazePanel.setLayout(new GridLayout(mazeSize, mazeSize, 0, 0));
        Border blackLine = BorderFactory.createLineBorder(Color.black);
        mazeLabels = new JLabel[mazeSize][mazeSize];
        for (int i = 0; i < mazeSize; i++) {
            for (int j = 0; j < mazeSize; j++) {
                mazeLabels[i][j] = new JLabel("", SwingConstants.CENTER);
                mazeLabels[i][j].setBorder(blackLine);
                mazePanel.add(mazeLabels[i][j]);
            }
        }

        mainFrame.add(infoPanel);
        mainFrame.add(mazePanel);
        mainFrame.setVisible(true);

        System.out.println("Player " + name + " has launched the UI.");

        UIUpdate();
        globalState.addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("Global state has changed");
        UIUpdate();
        System.out.println("****** GUI Global State **********");
        Map<String, Player> mm = globalState.getPlayersMap();
        for (Map.Entry<String, Player> entry : mm.entrySet()){
            System.out.printf(entry.getKey()+" [" + entry.getValue().getCurrentPosition().getRow() + ", "+entry.getValue().getCurrentPosition().getColumn()+"]\n");
        }
        System.out.println("*****************");
    }


    private void UIUpdate(){
        Vector infoVector = infoTable.getDataVector();
        infoVector.clear();
        clearMazeLabels();

        List<MazePair> treasuresLocation = this.globalState.getTreasuresLocation();
        for(int i = 0; i < treasuresLocation.size(); i++){
            MazePair location = treasuresLocation.get(i);
            String s = mazeLabels[N - 1 - location.getRow()][location.getColumn()].getText();
            mazeLabels[N - 1 - location.getRow()][location.getColumn()].setText(s + "*");
        }

        Iterator<Map.Entry<String, Player>> playerMapIterator = this.globalState.getPlayersMap().entrySet().iterator();
        while(playerMapIterator.hasNext()){
            Map.Entry<String, Player> entryNext = playerMapIterator.next();

            MazePair location = entryNext.getValue().getCurrentPosition();
            String s = mazeLabels[N - 1 -location.getRow()][location.getColumn()].getText();
            mazeLabels[N - 1 - location.getRow()][location.getColumn()].setText(s + entryNext.getKey());

            Vector<Object> row = new Vector<>();
            row.addElement(entryNext.getKey());
            row.addElement(entryNext.getValue().getScore());
            PlayerType type = entryNext.getValue().getType();
            if (type.equals(PlayerType.Standard)) {
                row.addElement("");
            } else {
                row.addElement(type.toString());
            }
            infoVector.addElement(row);
        }

        mazePanel.repaint();
    }

    private void clearMazeLabels(){
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                mazeLabels[i][j].setText("");
            }
        }
    }
}
