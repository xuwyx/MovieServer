package client;

import java.awt.*;
import javax.swing.*;

public class PersonalInfoWindow extends JDialog{
    PersonalInfoWindow(String title, byte[] head, String info){
        JPanel panelUp = new JPanel(new BorderLayout());
        JPanel panelDown = new JPanel(new BorderLayout());

        ImageIcon headImage = null;
        if(head != null) {
            headImage = new ImageIcon(head);
            headImage.setImage(headImage.getImage().getScaledInstance(80, 80, Image.SCALE_DEFAULT));
        }
        JTextArea personalInfo = new JTextArea(info);
        personalInfo.setEditable(false);

        panelUp.add(new JLabel("Head Image:"), BorderLayout.NORTH);
        panelUp.add(new JLabel(headImage), BorderLayout.CENTER);

        panelDown.add(new JLabel("Personal Description:"), BorderLayout.NORTH);
        panelDown.add(personalInfo, BorderLayout.CENTER);

        Container content = getContentPane();      // Get content pane
        content.setLayout(new GridLayout(2,1));
        content.add(panelUp);
        content.add(panelDown);
    }
}
