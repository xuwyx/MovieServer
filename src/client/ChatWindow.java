package client;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ChatWindow extends JFrame {
    private String	id = "";
    private String talkingId = "";
    TextArea	 txt;
    TextArea sendTxt;
    Socket sock;
    DataOutputStream	remoteOut;
    DataInputStream	remoteIn;
    JTable table;
    private DefaultTableModel tableModel;   //表格模型对象
    Connection c;
    Statement stmt;
    byte[] data = null;
    String info;
    Boolean edit = false;
    JPanel panelRight;
    JLabel l;
    JLabel infoTxt;
    Dimension wndSize;


    ChatWindow(String title, String id, Socket sock, DataOutputStream remoteOut, DataInputStream remoteIn)
    {
        super(title);
        this.sock = sock;
        this.remoteOut = remoteOut;
        this.remoteIn = remoteIn;
        this.id = id;

        Toolkit theKit = this.getToolkit();
        wndSize = theKit.getScreenSize();  // Get screen size

        Box top = Box.createHorizontalBox();
        top.add(new JLabel("Welcome! " + id));

        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Online List"), BorderLayout.NORTH);
        String [][] data = {};
        String [] column = {"Online"};
        tableModel = new DefaultTableModel(data,column){
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        table = new JTable(tableModel);
        left.add(new JScrollPane(table));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        left.add(table, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        sendTxt = new TextArea();
        bottom.add(sendTxt, BorderLayout.CENTER);
        JPanel sendButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton send = new JButton("Send");
        sendButton.add(send);
        bottom.add(sendButton, BorderLayout.SOUTH);

        txt = new TextArea();

        JMenuBar menuBar = new JMenuBar();
        JMenu infoMenu = new JMenu("Information");
        JMenu editMenu = new JMenu("Edit");
        JMenuItem personalInfo = new JMenuItem("Personal Information");
        JMenuItem editPersonalInfo = new JMenuItem("Edit Information");
        JMenuItem clearRecord = new JMenuItem("Clear Chat Record");
        infoMenu.add(personalInfo);
        infoMenu.add(editPersonalInfo);
        editMenu.add(clearRecord);
        menuBar.add(infoMenu);
        menuBar.add(editMenu);
        setJMenuBar(menuBar);

        panelRight = new JPanel(new GridLayout(2,1));
        l = new JLabel();
        l.setHorizontalAlignment(JLabel.CENTER);
        panelRight.add(l);
        infoTxt = new JLabel();
        infoTxt.setHorizontalAlignment(JLabel.CENTER);
        infoTxt.setVerticalAlignment(JLabel.CENTER);
        Box infoBox = Box.createVerticalBox();
        infoBox.add(new JLabel("His Description:"));
        infoBox.add(infoTxt);

        panelRight.add(infoBox);
        JPanel center = new JPanel(new BorderLayout());
        txt.setEditable(false);
        center.add(new JScrollPane(txt),BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);
        Container content = getContentPane();      // Get content pane
        content.setLayout(new BorderLayout());             // Set border layout manager
        content.add(top, BorderLayout.NORTH);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbs = new GridBagConstraints();
        JPanel panel = new JPanel();
        panel.setLayout(gbl);
        panel.add(left);
        panel.add(center);
        panel.add(panelRight);
        gbs.fill=GridBagConstraints.BOTH;gbs.gridwidth=1;gbs.gridheight=1;
        gbs.insets=new Insets(5, 5, 5, 5);gbs.weightx=1;gbs.weighty=1;
        gbs.gridx=0;gbs.gridy=0;
        gbl.setConstraints(left, gbs);
        gbs.fill=GridBagConstraints.BOTH;gbs.gridwidth=1;gbs.gridheight=1;
        gbs.insets=new Insets(5, 5, 5, 5);gbs.weightx=1;gbs.weighty=1;
        gbs.gridx=1;gbs.gridy=0;
        gbl.setConstraints(center, gbs);
        gbs.fill=GridBagConstraints.BOTH;gbs.gridwidth=1;gbs.gridheight=1;
        gbs.insets=new Insets(5, 5, 5, 5);gbs.weightx=1;gbs.weighty=1;
        gbs.gridx=2;gbs.gridy=0;
        gbl.setConstraints(panelRight, gbs);

        content.add(panel, BorderLayout.CENTER);
        connectSql();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(e.getClickCount() == 2){
                    try {
                        int row = ((JTable)e.getSource()).rowAtPoint(e.getPoint());
                        int col = ((JTable)e.getSource()).columnAtPoint(e.getPoint());
                        String value = (String)tableModel.getValueAt(row, 0);
                        talkingId = value;
                        showTxt(value);
                        remoteOut.writeUTF("I" + talkingId);
                        String sql = "UPDATE MSG_"+id+" SET MSG=0 WHERE ID=?";
                        PreparedStatement ps = c.prepareStatement(sql);
                        ps.setString(1, id);
                        ps.executeUpdate();
                        tableModel.setValueAt("0",row,1);
                        table.setModel(tableModel);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (Exception e2){

                    }
                }
            }
        });
        send.addActionListener(new sendActionListener());
        clearRecord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!talkingId.equals(""))
                    clearTable(talkingId);
            }
        });
        personalInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    edit = false;
                    remoteOut.writeUTF("I" + id);
                    remoteOut.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        editPersonalInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit = true;
                try {
                    remoteOut.writeUTF("I" + id);
                    remoteOut.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        new MultiChatReceive(remoteIn).start();
    }

    class sendActionListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            if(talkingId.equals("")){
                JOptionPane.showMessageDialog(null, "Please select a person to talk to.", "Warning", JOptionPane.WARNING_MESSAGE);
                sendTxt.setText("");
            }
            else {
                try {
                    String msg = sendTxt.getText();
                    String msgTime = Calendar.getInstance().getTime().toString();
                    remoteOut.writeUTF(talkingId+":"+msg);
                    sendTxt.setText("");
                    txt.append(id+"("+msgTime+"):\n"+msg+"\n");
                    String sql = "INSERT INTO USER_" + id + "(SEND, RECEIVE, MSGTIME, MESSAGE) " +
                            "VALUES(?,?,?,?)";
                    PreparedStatement ps = c.prepareStatement(sql);
                    ps.setString(1, id);
                    ps.setString(2, talkingId);
                    ps.setString(3,msgTime);
                    ps.setString(4,msg);
                    ps.executeUpdate();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    void clearTable(String id){
        try {
            String sql = "DELETE FROM USER_" + this.id + " WHERE SEND=? OR RECEIVE=?";
            txt.setText("");
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1,id);
            ps.setString(2,id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void showTxt(String id){
        String sql = "SELECT * FROM USER_"+this.id+" WHERE SEND=? OR RECEIVE=? ORDER BY MSGTIME";
        String msg = "";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, id);
            ps.setString(2, id);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                msg +=  rs.getString("SEND") + "("+rs.getString("MSGTIME")+"):\n"+rs.getString("MESSAGE")+"\n";
            }
            txt.setText(msg);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void connectSql(){
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:qqRecord"+id+".db");
            DatabaseMetaData meta = c.getMetaData();
            ResultSet rs = meta.getTables(null, null, "USER_"+id, null);
            stmt = c.createStatement();
            if(!rs.next()){
                String sql = "CREATE TABLE USER_" + id + " (SEND VARCHAR NOT NULL, RECEIVE VARCHAR NOT NULL, MSGTIME DATE, MESSAGE VARCHAR NOT NULL)";
                stmt.executeUpdate(sql);
            }
            rs = meta.getTables(null, null, "MSG_"+id, null);
            stmt = c.createStatement();
            if(!rs.next()){
                String sql = "CREATE TABLE MSG_" + id + " (ID VARCHAR NOT NULL, MSG INT NOT NULL)";
                stmt.executeUpdate(sql);
            }
            else {
                String sql = "DELETE FROM MSG_" + id;
                stmt.executeUpdate(sql);
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    void updateOnline(String onlineList){
        String [][] data = {};
        String [] column = {"Online", "Msg"};
        tableModel = new DefaultTableModel(data,column){
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        String tmp;
        boolean flag = true;
        if(talkingId != ""){
            flag = false;
        }
        while(!onlineList.equals("")){
            tmp = onlineList.substring(0, onlineList.indexOf('\n'));
            if(flag == false && tmp.equals(talkingId)) flag = true;
            if(!tmp.equals(this.id)) {
                String sql = "SELECT MSG FROM MSG_"+id+" WHERE ID=?";
                int Msg = 0;
                try {
                    PreparedStatement ps = c.prepareStatement(sql);
                    ps.setString(1, tmp);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()){
                        Msg = rs.getInt("MSG");
                    }
                    else Msg = 0;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String[] row = {tmp, String.valueOf(Msg)};
                tableModel.addRow(row);
            }
            onlineList = onlineList.substring(onlineList.indexOf('\n') + 1);
            if(onlineList == null) break;
        }
        if(flag == false)talkingId = "";
        table.setModel(tableModel);
    }

    public static void main(String[] args)
    {
        ChatWindow aWindow = new ChatWindow("The Window 2015", null, null, null, null);

        Toolkit theKit = aWindow.getToolkit();       // Get the window toolkit
        Dimension wndSize = theKit.getScreenSize();  // Get screen size

        aWindow.setBounds(wndSize.width/4, wndSize.height/8,   // Position
                wndSize.width/2, wndSize.height*3/4);  // Size
        aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        aWindow.setVisible(true);                          // Display the window
    }

    class MultiChatReceive extends Thread
    {
        DataInputStream remoteIn;
        MultiChatReceive(DataInputStream remoteIn)
        {   this.remoteIn = remoteIn;
            setDaemon(true);                 // Thread is daemon
        }

        public synchronized void run()
        {
            try{
                while(true){
                    String s = remoteIn.readUTF();
                    String send;
                    String sql;
                    String tm;
                    int size;
                    int len = 0;
                    if(s.length()==0)continue;
                    switch(s.charAt(0)){
                        case 'O':
                            updateOnline(s.substring(1));
                            break;
                        case 'M':
                            send = s.substring(1, s.indexOf(':'));
                            tm = Calendar.getInstance().getTime().toString();
                            sql = "INSERT INTO USER_" + id + "(SEND, RECEIVE, MSGTIME, MESSAGE) " +
                                    "VALUES(?,?,?,?)";
                            PreparedStatement ps = c.prepareStatement(sql);
                            ps.setString(1, send);
                            ps.setString(2,id);
                            ps.setString(3,tm);
                            ps.setString(4,s.substring(s.indexOf(':')+1));
                            ps.executeUpdate();
                            if(talkingId.equals(send)) txt.append(send+"("+tm+"):\n"+s.substring(s.indexOf(':')+1)+"\n");
                            else {
                                sql = "SELECT * FROM MSG_" + id + " WHERE ID=?";
                                ps = c.prepareStatement(sql);
                                ps.setString(1, send);
                                ResultSet rs = ps.executeQuery();
                                if (rs.next()) {
                                    sql = "UPDATE MSG_" + id + " SET MSG=MSG+1 WHERE ID=?";
                                    ps = c.prepareStatement(sql);
                                    ps.setString(1, send);
                                    ps.executeUpdate();
                                }
                                else {
                                    sql = "INSERT INTO MSG_"+id+" VALUES (?,1)";
                                    ps = c.prepareStatement(sql);
                                    ps.setString(1, send);
//                                    ps.setInt(2, 1);
                                    ps.executeUpdate();
                                }
                                for (int i = 0; i < tableModel.getRowCount(); i++) {
                                    if (tableModel.getValueAt(i, 0).equals(send)) {
                                        int j = Integer.parseInt((String) table.getValueAt(i, 1)) + 1;
                                        tableModel.setValueAt(String.valueOf(j), i, 1);
                                        table.setModel(tableModel);
                                        break;
                                    }
                                }
                            }
                            break;
                        case 'P':
                            size = remoteIn.readInt();
                            data = new byte[size];
                            while (len < size) {
                                len += remoteIn.read(data, len, size - len);
                            }
                            break;
                        case 'F':
                            info = s.substring(1);
                            break;
                        case 'I':
                            if(s.substring(3).equals(id)){
                                if(!edit){
                                    if(s.charAt(1) == 'N') data = null;
                                    if(s.charAt(2) == 'N') info = null;
                                    PersonalInfoWindow aWindow = new PersonalInfoWindow("Personal Information", data, info);

                                    aWindow.setBounds(wndSize.width*4/10, wndSize.height/3,   // Position
                                            wndSize.width/5, wndSize.height/4);  // Size
                                    aWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                    aWindow.setVisible(true);
                                }
                                else {
                                    if(s.charAt(1) == 'N') data = null;
                                    if(s.charAt(2) == 'N') info = null;
                                    EditInfoWindow aWindow = new EditInfoWindow("Personal Information", data, info, remoteOut);
                                    aWindow.setBounds(wndSize.width / 4, wndSize.height / 4,   // Position
                                            wndSize.width / 2, wndSize.height / 2);  // Size
                                    aWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                    aWindow.setVisible(true);
                                }
                            }
                            else if(s.substring(3).equals(talkingId)){
                                if(s.charAt(1) == 'N') data = null;
                                if(s.charAt(2) == 'N') info = null;
                                if(data != null) {
                                    ImageIcon headImage = new ImageIcon(data);
                                    headImage.setImage(headImage.getImage().getScaledInstance(80, 80, Image.SCALE_DEFAULT));
                                    l.setIcon(headImage);
                                }
                                else l.setIcon(null);
                                infoTxt.setText(info);
                            }
                            break;
                        default:
                            txt.setText(s+"\n"+txt.getText());
                            break;
                    }
                }
            } catch(IOException e){
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
