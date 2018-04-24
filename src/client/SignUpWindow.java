package client;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SignUpWindow extends JFrame {
    private int		port = 5001;
    private String	host = "local";
    DataOutputStream	remoteOut;

    SignUpWindow(String windowName){
        super(windowName);
        JPanel panelUp = new JPanel(new BorderLayout());
        JPanel panelDown = new JPanel(new GridLayout(3,1));
        JTextField ipr = new JTextField("local");
        JTextField prt = new JTextField("5001");
        JTextField id = new JTextField();
        JTextField password = new JTextField();
        JButton signUp = new JButton("Sign Up");
        JButton login = new JButton("Back to Login");
        Box idBox = Box.createHorizontalBox();
        Box passwordBox = Box.createHorizontalBox();
        Box top = Box.createHorizontalBox();
        top.add(new JLabel("host ip"));
        top.add(ipr);
        top.add(new JLabel("port"));
        top.add(prt);
        panelUp.add(top, BorderLayout.NORTH);
        JPanel buttonPanel = new JPanel(new GridLayout(1,2));
        idBox.add(new JLabel("  ID:               "));
        idBox.add(id);
        passwordBox.add(new JLabel("  PASSWORD: "));
        passwordBox.add(password);
        buttonPanel.add(login);
        buttonPanel.add(signUp);
        panelDown.add(idBox);
        panelDown.add(passwordBox);
        panelDown.add(buttonPanel);
        Container content = getContentPane();      // Get content pane
        content.setLayout(new GridLayout(2,1));             // Set border layout manager
        content.add(panelUp);
        content.add(panelDown);

        signUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                host = ipr.getText();
                port = Integer.parseInt(prt.getText());
                signUpConnect(id.getText(), password.getText());
            }
        });
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                LoginWindow aWindow = new LoginWindow("Login");
                Toolkit theKit = aWindow.getToolkit();
                Dimension wndSize = theKit.getScreenSize();  // Get screen size
                aWindow.setBounds(wndSize.width*4/10, wndSize.height/3,   // Position
                        wndSize.width/5, wndSize.height/4);  // Size
                aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                aWindow.setVisible(true);
            }
        });
    }

    private void signUpConnect(String id, String password)
    {
        try {
            if(host.equals("local"))host = null;
            InetAddress serverAddr = InetAddress.getByName(host);
            Socket sock = new Socket(
                    serverAddr.getHostName(), port);
            remoteOut = new DataOutputStream(sock.getOutputStream());
            remoteOut.writeUTF("S" + id + ":" + password);
            remoteOut.flush();
            DataInputStream remoteIn =
                    new DataInputStream(sock.getInputStream());
            String s = remoteIn.readUTF();
            while(s.length()==0) s = remoteIn.readUTF();
            switch(s.charAt(0)){
                case 'Y':
                    JOptionPane.showMessageDialog(null, "Sign up successfully.", null, JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    LoginWindow aWindow = new LoginWindow("Login");
                    Toolkit theKit = aWindow.getToolkit();
                    Dimension wndSize = theKit.getScreenSize();  // Get screen size
                    aWindow.setBounds(wndSize.width*4/10, wndSize.height/3,   // Position
                            wndSize.width/5, wndSize.height/4);  // Size
                    aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    aWindow.setVisible(true);
                    break;
                case 'I':
                    JOptionPane.showMessageDialog(null, "Repeated ID.", "Error", JOptionPane.WARNING_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "ERROR", "Error", JOptionPane.WARNING_MESSAGE);
                    break;
            }
        } catch(IOException e){
            System.out.println(e.getMessage() + ": Failed to connect to server.");
        }
    }

    public static void main(String[] args){
        SignUpWindow aWindow = new SignUpWindow("Sign Up");
        Toolkit theKit = aWindow.getToolkit();
        Dimension wndSize = theKit.getScreenSize();  // Get screen size
        aWindow.setBounds(wndSize.width*4/10, wndSize.height/3,   // Position
                wndSize.width/5, wndSize.height/4);  // Size
        aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        aWindow.setVisible(true);                          // Display the window
    }
}
