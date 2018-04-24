package client;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginWindow extends JFrame {
    private int		port = 5001;
    private String	host = "local";
    DataOutputStream	remoteOut;
    DataInputStream remoteIn;

    private void loginConnect(String id, String password)
    {
        try {
            if(host.equals("local"))host = null;
            InetAddress serverAddr = InetAddress.getByName(host);
            Socket sock = new Socket(
                    serverAddr.getHostName(), port);
            remoteOut = new DataOutputStream(sock.getOutputStream());
            remoteOut.writeUTF("L" + id + ":" + password);
            remoteOut.flush();
            remoteIn = new DataInputStream(sock.getInputStream());
            String s = remoteIn.readUTF();
            while(s.length()==0) s = remoteIn.readUTF();
            switch(s.charAt(0)){
                case 'Y':
                    ChatWindow aWindow = new ChatWindow("QQ Window 2018", id, sock, remoteOut, remoteIn);

                    Toolkit theKit = aWindow.getToolkit();       // Get the window toolkit
                    Dimension wndSize = theKit.getScreenSize();  // Get screen size

                    aWindow.setBounds(wndSize.width/4, wndSize.height/8,   // Position
                            wndSize.width/2, wndSize.height*3/4);  // Size
                    aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    aWindow.setVisible(true);
                    dispose();
                    break;
                case 'I':
                    JOptionPane.showMessageDialog(null, "Wrong ID.", "Error", JOptionPane.WARNING_MESSAGE);
                    break;
                case 'P':
                    JOptionPane.showMessageDialog(null, "Wrong Password", "Error", JOptionPane.WARNING_MESSAGE);
                    break;
                case 'R':
                    JOptionPane.showMessageDialog(null, "Repeated Login.", "Error", JOptionPane.WARNING_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "ERROR", "Error", JOptionPane.WARNING_MESSAGE);
                    break;
            }
        } catch(IOException e){
            System.out.println(e.getMessage() + ": Failed to connect to server.");
        }
    }

    LoginWindow(String windowName){
        super(windowName);
        JPanel panelUp = new JPanel(new BorderLayout());
        JPanel panelDown = new JPanel(new GridLayout(3,1));
        JTextField id = new JTextField();
        JPasswordField password = new JPasswordField();
        JButton signUp = new JButton("Sign Up");
        JButton login = new JButton("Login");
        JTextField ipr = new JTextField("local");
        JTextField prt = new JTextField("5001");
        Box top = Box.createHorizontalBox();
        top.add(new JLabel("host ip"));
        top.add(ipr);
        top.add(new JLabel("port"));
        top.add(prt);
        panelUp.add(top, BorderLayout.NORTH);
        Box idBox = Box.createHorizontalBox();
        Box passwordBox = Box.createHorizontalBox();
        JPanel buttonPanel = new JPanel(new GridLayout(1,2));
        idBox.add(new JLabel("  ID:               "));
        idBox.add(id);
        passwordBox.add(new JLabel("  PASSWORD: "));
        passwordBox.add(password);
        buttonPanel.add(signUp);
        buttonPanel.add(login);
        panelDown.add(idBox);
        panelDown.add(passwordBox);
        panelDown.add(buttonPanel);
        Container content = getContentPane();      // Get content pane
        content.setLayout(new GridLayout(2,1));             // Set border layout manager
        content.add(panelUp);
        content.add(panelDown);

        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                host = ipr.getText();
                port = Integer.parseInt(prt.getText());
                loginConnect(id.getText(), String.valueOf(password.getPassword()));
            }
        });
        signUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignUpWindow aWindow = new SignUpWindow("Sign Up");
                Toolkit theKit = aWindow.getToolkit();
                Dimension wndSize = theKit.getScreenSize();  // Get screen size
                aWindow.setBounds(wndSize.width*4/10, wndSize.height/3,   // Position
                        wndSize.width/5, wndSize.height/4);  // Size
                aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                aWindow.setVisible(true);
                dispose();
            }
        });
    }

    public static void main(String[] args){
        LoginWindow aWindow = new LoginWindow("Login");
        Toolkit theKit = aWindow.getToolkit();
        Dimension wndSize = theKit.getScreenSize();  // Get screen size
        aWindow.setBounds(wndSize.width*4/10, wndSize.height/3,   // Position
                wndSize.width/5, wndSize.height/4);  // Size
        aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        aWindow.setVisible(true);                          // Display the window
    }
}
