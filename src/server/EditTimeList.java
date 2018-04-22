package server;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EditTimeList extends JDialog {
    private ServerWindow serverWindow;

    JTextField movie, time;
    Boolean isPicture = false;
    EditTimeList(String hallID, ServerWindow serverWindow){
        this.serverWindow = serverWindow;
        JButton delete = new JButton("Delete");
        JButton add = new JButton("Add");
        Box up = new Box(1);
        up.add(delete);

        Box down1 = Box.createHorizontalBox();
        movie = new JTextField();
        time = new JTextField();
        down1.add(new JLabel("Movie:"));
        down1.add(movie);
        Box down2 = Box.createHorizontalBox();
        down2.add(new JLabel("Time:"));
        down2.add(time);
        Box down3 = Box.createHorizontalBox();
        down3.add(add);

        JPanel downP = new JPanel(new GridLayout(3,1));
        downP.add(down1);
        downP.add(down2);
        downP.add(down3);



        Container content = getContentPane();      // Get content pane
        content.setLayout(new BorderLayout());
        content.add(up, BorderLayout.NORTH);
        content.add(serverWindow.timeListTable, BorderLayout.CENTER);
        content.add(downP, BorderLayout.SOUTH);

        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                serverWindow.addTimeList(hallID, movie.getText(), time.getText());
            }
        });

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = serverWindow.timeListTable.getSelectedRow();
                String movie = (String) serverWindow.timeListTable.getValueAt(row, 0);
                String time = (String) serverWindow.timeListTable.getValueAt(row, 1);
                serverWindow.deleteTimeList(hallID, movie, time);
            }
        });
    }
}
