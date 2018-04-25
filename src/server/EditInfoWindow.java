package server;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class EditInfoWindow extends JDialog implements ActionListener{
    private ServerWindow serverWindow;
    private String movieid;
    JTextField path;
    JFileChooser jfc;
    JPanel panelUp;
    JPanel panelDown;
    JLabel l;
    JTextField timeText;
    JTextField priceText;
    JTextArea Info;
    Boolean isPicture = false;
    EditInfoWindow(String title, byte[] head, String info, String time, String price, ServerWindow serverWindow, String movieid){
        this.serverWindow = serverWindow;
        this.movieid = movieid;
        panelUp = new JPanel(new BorderLayout());
        panelDown = new JPanel(new GridLayout(5, 1));
        ImageIcon headImage = null;
        if(head != null) {
            headImage = new ImageIcon(head);
            headImage.setImage(headImage.getImage().getScaledInstance(100, 150, Image.SCALE_DEFAULT));
        }
        JScrollPane jsp;
        Info = new JTextArea(info);
        timeText = new JTextField(time);
        priceText = new JTextField(price);

        Box up = Box.createHorizontalBox();
        jfc = new JFileChooser();
        path = new JTextField();
        JButton upload = new JButton("Upload");
        up.add(path);
        up.add(upload);
        JButton save = new JButton("Save");

        l = new JLabel(headImage);
        l.setHorizontalAlignment(JLabel.CENTER);

        panelUp.add(new JLabel("Head Image:"), BorderLayout.NORTH);
        panelUp.add(l, BorderLayout.CENTER);
        panelUp.add(up, BorderLayout.SOUTH);

        panelDown.add(new JLabel("时长(min):"));
        panelDown.add(timeText);
        panelDown.add(new JLabel("价格(元):"));
        panelDown.add(priceText);
        panelDown.add(new JLabel("简介:"));
        JPanel panelDown2 = new JPanel(new BorderLayout());
        Info.setLineWrap(true);
        jsp = new JScrollPane(Info);
        panelDown2.add(jsp, BorderLayout.CENTER);
        panelDown2.add(save, BorderLayout.SOUTH);


        Container content = getContentPane();      // Get content pane
        content.setLayout(new GridLayout(3,1));
        content.add(panelUp);
        content.add(panelDown);
        content.add(panelDown2);

        upload.addActionListener(this);
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Save();
            }
        });
    }

    public void Save(){
        String fname = path.getText();
        try {
            if(isPicture) {
                FileInputStream fis = new FileInputStream(fname);
                int size = fis.available();
                byte[] data = new byte[size];
                fis.read(data);
                serverWindow.updateMovie(data, Info.getText(), movieid, timeText.getText(), priceText.getText());
            }
            else serverWindow.updateMovie(new byte[0], Info.getText(), movieid, timeText.getText(), priceText.getText());
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            String fname = path.getText();
            File f = new File(fname);
            boolean flag = isImage(f);
            if(!flag){
                JOptionPane.showMessageDialog(null, "Not an image.","Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            panelUp.remove(l);
            ImageIcon headImage = new ImageIcon(fname);
            headImage.setImage(headImage.getImage().getScaledInstance(100, 150, Image.SCALE_DEFAULT));
            l = new JLabel(headImage);
            l.setHorizontalAlignment(JLabel.CENTER);
            panelUp.add(l, BorderLayout.CENTER);
            panelUp.revalidate();
            isPicture = true;
        }
        catch (Exception e1){
            e1.printStackTrace();
        }
    }

    private boolean isImage(File file)
    {
        boolean flag = false;
        try
        {
            ImageInputStream is = ImageIO.createImageInputStream(file);
            if(null == is)
            {
                return flag;
            }
            is.close();
            flag = true;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return flag;
    }
}
