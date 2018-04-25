package server;


import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class ServerWindow extends JFrame {
    private int port = 5001;
    ServerSocket server;
    private Vector clients = new Vector();
    JTable hallTable;
    JTable timeListTable;
    TextArea infoTxt;
    Connection c = null;
    Statement stmt;
    JTable table;
    String selectedId;
    private DefaultTableModel tableModel;   //表格模型对象
    private DefaultTableModel hallTableModel;   //表格模型对象
    private DefaultTableModel timeListTableModel;   //表格模型对象

    ServerWindow(String windowName) {
        super(windowName);
        JTabbedPane pane = new JTabbedPane();
        JPanel hallList = new JPanel(new BorderLayout());
        JPanel info = new JPanel(new GridLayout(1, 1));
        JPanel manage = new JPanel(new BorderLayout());
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");
        JButton add = new JButton("Add Movie");
        JButton editHall = new JButton("Edit");
        JButton deleteHall = new JButton("Delete");
        JButton addHall = new JButton("Add Movie Hall");
        Box addMovie = new Box(2);
        JTextField movieName = new JTextField();
        JTextField hallName = new JTextField();
        addMovie.add(movieName);
        addMovie.add(add);
        String[][] data = {};
        String[] column = {"Movie"};
        tableModel = new DefaultTableModel(data, column) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        String[] hallColumn = {"Hall"};
        hallTableModel = new DefaultTableModel(data, hallColumn) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        String[] timeListColumn = {"TimeList"};
        timeListTableModel = new DefaultTableModel(data, timeListColumn) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        hallTable = new JTable(hallTableModel);
        timeListTable = new JTable(timeListTableModel);

        infoTxt = new TextArea();
        hallList.add(hallTable, BorderLayout.CENTER);
        Box hallListUp = new Box(2);
        hallListUp.add(editHall);
        hallListUp.add(deleteHall);
        hallList.add(hallListUp, BorderLayout.NORTH);
        Box hallListDown = new Box(2);
        hallListDown.add(hallName);
        hallListDown.add(addHall);
        hallList.add(hallListDown, BorderLayout.SOUTH);
        manage.add(table, BorderLayout.CENTER);
        Box btn = new Box(2);
        btn.add(edit);
        btn.add(delete);
        manage.add(btn, BorderLayout.NORTH);
        manage.add(addMovie, BorderLayout.SOUTH);

        info.add(new JScrollPane(infoTxt));
        pane.add("Movie Hall List", hallList);
        pane.add("Information", info);
        pane.add("Manage Movie", manage);
        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        content.add(new Label("    LISTENING PORT: " + String.valueOf(port)), BorderLayout.NORTH);
        content.add(pane, BorderLayout.CENTER);

        connectSql();
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                String value = (String) tableModel.getValueAt(row, 0);
                selectedId = value;
                String sql = "SELECT * FROM MOVIE WHERE ID=?";
                try {
                    PreparedStatement ps = c.prepareStatement(sql);
                    ps.setString(1, selectedId);
                    ResultSet rs = ps.executeQuery();
                    byte[] bytes = rs.getBytes("PICTURE");
                    String info = rs.getString("INFO");
                    String time = String.valueOf(rs.getInt("TIME"));
                    String price = String.valueOf(rs.getDouble("PRICE"));
                    showEditInfo(bytes, info, time, price);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

            }
        });

        deleteHall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = hallTable.getSelectedRow();
                String value = (String) hallTableModel.getValueAt(row, 0);
                selectedId = value;
                String sql = "DELETE FROM HALL WHERE ID=?";
                try {
                    PreparedStatement ps = c.prepareStatement(sql);
                    ps.setString(1, selectedId);
                    ps.executeUpdate();
                    sql = "DELETE FROM TIMELIST WHERE ID=?";
                    ps.setString(1, selectedId);
                    ps.executeUpdate();
                    updateHallTable();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                String value = (String) tableModel.getValueAt(row, 0);
                selectedId = value;
                String sql = "DELETE FROM MOVIE WHERE ID=?";
                try {
                    PreparedStatement ps = c.prepareStatement(sql);
                    ps.setString(1, selectedId);
                    ps.executeUpdate();
                    updateTable();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

            }
        });

        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (movieName.getText().length() == 0) return;
                try {
                    String sql = "INSERT INTO MOVIE VALUES(?,NULL,NULL,0,0)";
                    PreparedStatement pstmt = c.prepareStatement(sql);
                    pstmt.setString(1, movieName.getText());
                    pstmt.executeUpdate();
                    updateTable();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        addHall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hallName.getText().length() == 0) return;
                try {
                    String sql = "INSERT INTO HALL VALUES(?,?)";
                    PreparedStatement pstmt = c.prepareStatement(sql);
                    pstmt.setString(1, hallName.getText());
                    pstmt.setString(2, "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
                    pstmt.executeUpdate();
                    updateHallTable();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        editHall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = hallTable.getSelectedRow();
                String hallID = (String) hallTable.getValueAt(row, 0);
                updateTimeListTable(hallID);
                showEditTimeList(hallID);
            }
        });

        updateTable();
        updateHallTable();

        new Listening(this.port).start();
    }

    void showEditTimeList(String hallID) {
        EditTimeList aWindow = new EditTimeList(hallID, this);
        Toolkit theKit = aWindow.getToolkit();
        Dimension wndSize = theKit.getScreenSize();  // Get screen size
        aWindow.setBounds(wndSize.width / 4, wndSize.height / 4,   // Position
                wndSize.width / 2, wndSize.height / 2);  // Size
        aWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        aWindow.setVisible(true);
    }

    void deleteTimeList(String hallID, String movieID, String time) {
        Timestamp ts = Timestamp.valueOf(time);
        String sql = "DELETE FROM TIMELIST WHERE HALL=? AND MOVIE=? AND TIME=?";
        try {
            PreparedStatement pstmt = c.prepareStatement(sql);
            pstmt.setString(1, hallID);
            pstmt.setString(2, movieID);
            pstmt.setTimestamp(3, ts);
            pstmt.executeUpdate();
            updateTimeListTable(hallID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void addTimeList(String hallID, String movieID, String time) {
        try {
            String sql = "SELECT * FROM MOVIE WHERE ID=?";
            PreparedStatement pstmt = c.prepareStatement(sql);
            pstmt.setString(1, movieID);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Movie not exsist.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Timestamp ts = Timestamp.valueOf(time);
            sql = "INSERT INTO TIMELIST VALUES(?,?,?)";
            pstmt = c.prepareStatement(sql);
            pstmt.setString(2, hallID);
            pstmt.setString(1, movieID);
            pstmt.setTimestamp(3, ts);
            pstmt.executeUpdate();
            updateTimeListTable(hallID);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "TIME format: 2018-4-1 23:22:22", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    void showEditInfo(byte[] bytes, String info, String time, String price) {
        EditInfoWindow aWindow = new EditInfoWindow("Movie Information", bytes, info, time, price, this, selectedId);
        Toolkit theKit = aWindow.getToolkit();
        Dimension wndSize = theKit.getScreenSize();  // Get screen size
        aWindow.setBounds(wndSize.width / 4, wndSize.height / 4,   // Position
                wndSize.width / 2, wndSize.height / 2);  // Size
        aWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        aWindow.setVisible(true);
    }

    void updateTable() {
        String[][] data = {};
        String[] column = {"Movie"};
        tableModel = new DefaultTableModel(data, column) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        String sql = "SELECT * FROM MOVIE";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String tmp = rs.getString("ID");
                String[] row = {tmp};
                tableModel.addRow(row);
            }
            table.setModel(tableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void updateTimeListTable(String hallID) {
        try {
            String[][] data = {};
            String[] column = {"Movie name", "Time"};
            timeListTableModel = new DefaultTableModel(data, column) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            String sql = "SELECT * FROM TIMELIST WHERE HALL=? ORDER BY TIME";
            PreparedStatement pstmt = c.prepareStatement(sql);
            pstmt.setString(1, hallID);
            System.out.println(hallID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String tmp = rs.getString("MOVIE");
                String tmp2 = rs.getTimestamp("TIME").toString();
                String[] row = {tmp, tmp2};
                timeListTableModel.addRow(row);
            }
            timeListTable.setModel(timeListTableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void updateHallTable() {
        try {
            String[][] data = {};
            String[] column = {"Hall"};
            hallTableModel = new DefaultTableModel(data, column) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            String sql = "SELECT * FROM HALL";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String tmp = rs.getString("ID");
                String[] row = {tmp};
                hallTableModel.addRow(row);
            }
            hallTable.setModel(hallTableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void connectSql() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            DatabaseMetaData meta = c.getMetaData();
            ResultSet rs = meta.getTables(null, null, "USERS", null);
            stmt = c.createStatement();
            if (!rs.next()) {
                String sql = "CREATE TABLE USERS (ID VARCHAR KEY NOT NULL, PASSWORD VARCHAR NOT NULL, BANKID VARCHAR)";
                stmt.executeUpdate(sql);
            }
            rs = meta.getTables(null, null, "MOVIE", null);
            if (!rs.next()) {
                String sql = "CREATE TABLE MOVIE (ID VARCHAR KEY NOT NULL, PICTURE BLOB, INFO VARCHAR, TIME INTEGER, PRICE DOUBLE)";
                stmt.executeUpdate(sql);
            }

            rs = meta.getTables(null, null, "ONLINE", null);
            if (rs.next()) {
                String sql = "DELETE FROM ONLINE";
                stmt.executeUpdate(sql);
            } else {
                String sql = "CREATE TABLE ONLINE (ID VARCHAR KEY NOT NULL)";
                stmt.executeUpdate(sql);
            }

            rs = meta.getTables(null, null, "HALL", null);
            if (!rs.next()) {
                String sql = "CREATE TABLE HALL (ID VARCHAR KEY NOT NULL, SEAT VARCHAR)";
                stmt.executeUpdate(sql);
            }

            rs = meta.getTables(null, null, "TIMELIST", null);
            if (!rs.next()) {
                String sql = "CREATE TABLE TIMELIST (MOVIE VARCHAR KEY NOT NULL, HALL VARCHAR KEY NOT NULL, TIME TIMESTAMP NOT NULL)";
                stmt.executeUpdate(sql);
            }

            rs = meta.getTables(null, null, "RECORD", null);
            if (!rs.next()) {
                String sql = "CREATE TABLE RECORD (USER VARCHAR KEY NOT NULL, HALL VARCHAR KEY NOT NULL, INTIME TIMESTAMP NOT NULL, OUTTIME TIMESTAMP, SEAT INT NOT NULL)";
                stmt.executeUpdate(sql);
            }

            rs = meta.getTables(null, null, "Account", null);
            if (!rs.next()) {
                String sql = "CREATE TABLE ACCOUNT (BANKID VARCHAR KEY NOT NULL, PASSWORD VARCHAR NOT NULL, MONEY DOUBLE)";
                stmt.executeUpdate(sql);
                sql = "INSERT INTO ACCOUNT VALUES(111,1111,11111)";
                stmt.executeUpdate(sql);
                sql = "INSERT INTO ACCOUNT VALUES(222,2222,22222)";
                stmt.executeUpdate(sql);
                sql = "INSERT INTO ACCOUNT VALUES(333,3333,33333)";
                stmt.executeUpdate(sql);
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

    }

    class ClientOut {
        String id;
        DataOutputStream remoteOut;

        ClientOut(String id, DataOutputStream remoteOut) {
            this.id = id;
            this.remoteOut = remoteOut;
        }
    }

    class Listening extends Thread {
        private int port;
        Socket socket;

        Listening(int p) {
            this.port = p;
            try {
                server = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized void run() {
            while (true) {
                try {

                    int flag = 0;
                    socket = server.accept();
                    System.out.println("connected");
                    DataInputStream remoteIn = new DataInputStream(socket.getInputStream());
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    String buf = remoteIn.readUTF();
                    System.out.println(buf);
                    String id = buf.substring(1, buf.indexOf(':'));
                    String ps = buf.substring(buf.indexOf(':') + 1);
                    if (buf.charAt(0) == 'L') {
                        String sql = "SELECT * FROM USERS WHERE ID=?";
                        PreparedStatement pstmt = c.prepareStatement(sql);
                        pstmt.setString(1, id);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            if (rs.getString("PASSWORD").equals(ps)) {
                                sql = "SELECT * FROM ONLINE WHERE ID=?";
                                pstmt = c.prepareStatement(sql);
                                pstmt.setString(1, id);
                                rs = pstmt.executeQuery();
                                if (rs.next()) {
                                    infoTxt.append(id + " repeatedly connected to server.\n");
                                    dataOut.writeUTF("R");
                                    dataOut.flush();
                                    continue;
                                }
                                infoTxt.append(id + " connected to server.\n");
                                dataOut.writeUTF("Y");
                                dataOut.flush();
                                ClientOut co = new ClientOut(id, dataOut);
                                clients.add(co);
                                sql = "INSERT INTO ONLINE(ID) VALUES(?)";
                                pstmt = c.prepareStatement(sql);
                                pstmt.setString(1, id);
                                pstmt.executeUpdate();
                                new ServerHelder(id, remoteIn).start();
                            } else {
                                infoTxt.append(id + " connected to server. Wrong password.\n");
                                dataOut.writeUTF("P");
                                dataOut.flush();
                            }
                        } else {
                            infoTxt.append(id + " connected to server. But not found in database.\n");
                            dataOut.writeUTF("I");
                            dataOut.flush();
                        }
                    } else if (buf.charAt(0) == 'S') {
                        String sql = "SELECT * FROM USERS WHERE ID=?";
                        PreparedStatement pstmt = c.prepareStatement(sql);
                        pstmt.setString(1, id);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            infoTxt.append(id + " sign up. Repeated ID.\n");
                            dataOut.writeUTF("I");
                            dataOut.flush();
                        } else {
                            sql = "INSERT INTO USERS(ID, PASSWORD, BANKID) VALUES (?, ?, NULL)";
                            pstmt = c.prepareStatement(sql);
                            pstmt.setString(1, id);
                            pstmt.setString(2, ps);
                            pstmt.executeUpdate();
                            infoTxt.append(id + " sign up.\n");
                            dataOut.writeUTF("Y");
                            dataOut.flush();
                            updateTable();
                        }
                    } else if (buf.charAt(0) == 'H') {
                        String hall = buf.substring(1);
                        String sql = "SELECT * FROM HALL WHERE ID = 1";
                        PreparedStatement pstmt = c.prepareStatement(sql);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            sql = "SELECT * FROM TIMELIST WHERE HALL = '1' ORDER BY TIME";
                            pstmt = c.prepareStatement(sql);
                            ResultSet rs1 = pstmt.executeQuery();

                            while (rs1.next()) {
                                String mov = rs1.getString("MOVIE");
                                String time1 = rs1.getTimestamp("TIME").toString();
                                sql = "SELECT * FROM MOVIE WHERE ID = ?";
                                pstmt = c.prepareStatement(sql);
                                pstmt.setString(1, mov);
                                ResultSet rs2 = pstmt.executeQuery();
                                int time2 = 0;
                                if (rs2.next()) {
                                    time2 = rs2.getInt("TIME");
                                }
                                byte[] pic = rs2.getBytes("PICTURE");
                                int len = pic.length;

                                dataOut.writeInt(len);
                                dataOut.write(pic);
                                dataOut.writeUTF(mov);
                                dataOut.writeUTF(time1.substring(time1.indexOf(" ") + 1));
                                dataOut.writeInt(time2);
                                dataOut.flush();
                            }
                            dataOut.writeInt(0);
                            new ServerHelder("1", remoteIn).start();
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ServerHelder extends Thread {
        String id;
        DataInputStream remoteIn;

        ServerHelder(String id, DataInputStream remoteIn) {
            this.id = id;
            this.remoteIn = remoteIn;
            setDaemon(true);
        }

        void sendMessage(String target, String msg) {
            DataOutputStream dataOut = null;
            for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
                ClientOut c = (ClientOut) e.nextElement();
                if (c.id.equals(target)) {
                    dataOut = c.remoteOut;
                    try {
                        dataOut.writeUTF(msg);
                        dataOut.flush();
                        infoTxt.append("Send message: "+msg+"\n");
                    } catch (IOException x) {
                        System.out.println(x.getMessage() + ": Failed to send message to client.");
                        clients.removeElement(c);
                    }
                    break;
                }
            }
        }

        public synchronized void run() {
            String target;
            String msg;
            String buf;
            try {
                while (true) {
                    buf = remoteIn.readUTF();
                    System.out.println(buf);
                    if (buf.charAt(0) == 'S') {
                        String user = buf.substring(1);
                        String sql = "SELECT * FROM RECORD WHERE OUTTIME IS NULL AND USER = ? AND HALL = ?";
                        PreparedStatement ps = c.prepareStatement(sql);
                        ps.setString(1, user);
                        ps.setString(2, id);
                        ResultSet rs = ps.executeQuery();
                        Timestamp ts1, ts2;
                        String s = "";
                        if (rs.next()) {
                            ts2 = new Timestamp(System.currentTimeMillis());
                            ts1 = rs.getTimestamp("INTIME");
                            int seat = rs.getInt("SEAT");
                            sql = "SELECT SEAT FROM HALL WHERE ID = ?";
                            ps = c.prepareStatement(sql);
                            ps.setString(1, id);
                            rs = ps.executeQuery();
                            if (rs.next()) {
                                s = rs.getString("SEAT");
                                s = s.substring(0, seat) + "0" + s.substring(seat + 1);
                                sql = "UPDATE HALL SET SEAT= ? WHERE ID = ?";
                                ps = c.prepareStatement(sql);
                                ps.setString(1, s);
                                ps.setString(2, id);
                                ps.executeUpdate();
                            }

//                            }
                            sql = "UPDATE RECORD SET OUTTIME = ? WHERE USER = ? AND HALL = ? AND INTIME = ?";
                            ps = c.prepareStatement(sql);
                            ps.setTimestamp(1, ts2);
                            ps.setString(2, user);
                            ps.setString(3, id);
                            ps.setTimestamp(4, ts1);
                            ps.executeUpdate();

                            sql = "SELECT * FROM TIMELIST WHERE TIME <= ? AND HALL = ? ORDER BY TIME DESC LIMIT 1";
                            ps = c.prepareStatement(sql);
                            ps.setTimestamp(1, ts1);
                            ps.setString(2, id);
                            rs = ps.executeQuery();
                            if (rs.next()) {

                                String mov = rs.getString("MOVIE");
                                Timestamp tms1 = rs.getTimestamp("TIME");
                                sql = "SELECT * FROM MOVIE WHERE ID = ?";
                                ps = c.prepareStatement(sql);
                                ps.setString(1, mov);
                                rs = ps.executeQuery();
                                int time1 = 0;
                                double price1 = 0;
                                if (rs.next()) {
                                    time1 = rs.getInt("TIME");
                                    price1 = rs.getDouble("PRICE");
                                }

                                double timelength = 0;
                                double price = 0;
                                timelength = (tms1.getTime() + 60 * 1000 * time1 - ts1.getTime()) / (60 * 1000);
                                if (timelength >= 0)
                                    price = price1 * timelength / (double) time1;
                                System.out.println("price1:"+price);
                                sql = "SELECT * FROM TIMELIST WHERE TIME >= ? AND TIME <= ? AND HALL = ? ORDER BY TIME";
                                ps = c.prepareStatement(sql);
                                ps.setTimestamp(1, ts1);
                                ps.setTimestamp(2, ts2);
                                ps.setString(3, id);
                                rs = ps.executeQuery();
                                while (rs.next()) {
                                    System.out.println("1");
                                    mov = rs.getString("MOVIE");
                                    tms1 = rs.getTimestamp("TIME");
                                    sql = "SELECT * FROM MOVIE WHERE ID = ?";
                                    ps = c.prepareStatement(sql);
                                    ps.setString(1, mov);
                                    ResultSet rs1 = ps.executeQuery();
                                    if (rs.next()) {
                                        price1 = rs1.getDouble("PRICE");
                                        price = price + price1;
                                        System.out.println("price2:"+price);

                                        time1 = rs1.getInt("TIME");
                                    }
                                }

                                timelength = tms1.getTime() + 60 * 1000 * time1 - ts2.getTime();
                                if (timelength > 0) {
                                    price = price - price1 * timelength / (60 * 1000 * time1);
                                    System.out.println("price3:"+price);
                                }
                                if(price < 0) price = 0;

                                sql = "SELECT BANKID FROM USERS WHERE ID = ?";
                                ps = c.prepareStatement(sql);
                                ps.setString(1, user);
                                rs = ps.executeQuery();
                                if (rs.next()) {
                                    s = rs.getString("BANKID");
                                }
                                sql = "UPDATE ACCOUNT SET MONEY = MONEY - ? WHERE BANKID = ?";
                                ps = c.prepareStatement(sql);
                                ps.setDouble(1, price);
                                ps.setString(2, s);
                                ps.executeUpdate();
                                String p = String.valueOf(price);
                                p = p.substring(0, p.indexOf('.')+3);
                                sendMessage(user, "M" + s + ":" + p);
                            }
                            else {
                                sql = "SELECT BANKID FROM USERS WHERE ID = ?";
                                ps = c.prepareStatement(sql);
                                ps.setString(1, user);
                                rs = ps.executeQuery();
                                if (rs.next()) {
                                    s = rs.getString("BANKID");
                                }
                                sendMessage(user, "M" + s + ":0");
                            }
                        } else {
                            sql = "SELECT BANKID FROM USERS WHERE ID = ?";
                            ps = c.prepareStatement(sql);
                            ps.setString(1, user);
                            rs = ps.executeQuery();
                            if (rs.next() && (rs.getString("BANKID") != null)) {
                                sql = "SELECT SEAT FROM HALL WHERE ID = ?";
                                PreparedStatement ps1 = c.prepareStatement(sql);
                                ps1.setString(1, id);
                                ResultSet rs1 = ps1.executeQuery();
                                if (rs1.next()) {
                                    DataOutputStream dataOut = null;
                                    for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
                                        ClientOut c = (ClientOut) e.nextElement();
                                        if (c.id.equals(buf.substring(1))) {
                                            dataOut = c.remoteOut;
                                            break;
                                        }
                                    }
                                    dataOut.writeUTF(rs1.getString("SEAT"));
                                    dataOut.flush();
                                }
                            } else {
                                sendMessage(buf.substring(1), "N");
                            }
                        }

                    } else if (buf.charAt(0) == 'C') {
                        String bankid = buf.substring(1, buf.indexOf(':'));
                        String ps = buf.substring(buf.indexOf(':') + 1);
                        String sql = "SELECT * FROM ACCOUNT WHERE BANKID = ?";
                        PreparedStatement pstmt = c.prepareStatement(sql);
                        pstmt.setString(1, bankid);

                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            if (rs.getString("PASSWORD").equals(ps)) {
                                sql = "SELECT * FROM USERS WHERE BANKID=?";
                                pstmt = c.prepareStatement(sql);
                                pstmt.setString(1, id);
                                rs = pstmt.executeQuery();
                                if (rs.next()) {
                                    sendMessage(id, "0");
                                } else {
                                    sql = "UPDATE USERS SET BANKID = ? WHERE id = ?";
                                    pstmt = c.prepareStatement(sql);
                                    pstmt.setString(1, bankid);
                                    pstmt.setString(2, id);

                                    pstmt.executeUpdate();
                                    sendMessage(id, "1");
                                }
                            } else {
                                //infoTxt.append(id + " connected to server. Wrong password.\n");
                                sendMessage(id, "0");
                            }
                        } else {
                            //infoTxt.append(id + " connected to server. But not found in database.\n");
                            sendMessage(id, "0");
                        }
                    } else if (buf.charAt(0) == 'L') {
                        String sql = "SELECT ID FROM MOVIE";
                        PreparedStatement ps = c.prepareStatement(sql);
                        ResultSet rs = ps.executeQuery();
                        String out = "L";
                        DataOutputStream dataOut = null;
                        for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
                            ClientOut c = (ClientOut) e.nextElement();
                            if (c.id.equals(id)) {
                                dataOut = c.remoteOut;
                                break;
                            }
                        }
                        while (rs.next()) {
                            out += rs.getString("ID");
                            out += ":";
                        }
                        dataOut.writeUTF(out.substring(0, out.lastIndexOf(':')));
                        dataOut.flush();
                    } else if (buf.charAt(0) == 'I') {
                        String sql = "SELECT * FROM MOVIE WHERE ID = ?";
                        PreparedStatement ps = c.prepareStatement(sql);
                        ps.setString(1, buf.substring(1));
                        ResultSet rs = ps.executeQuery();
//                        String out = "A";
                        DataOutputStream dataOut = null;
//                        System.out.println("client: "+id);
                        for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
                            ClientOut c = (ClientOut) e.nextElement();
//                            System.out.println(c.id);
                            if (c.id.equals(id)) {
                                dataOut = c.remoteOut;
                                break;
                            }
                        }
                        if (rs.next()) {
                            byte[] pic = rs.getBytes("PICTURE");
                            int len = pic.length;
//                            dataOut.writeUTF(out);
                            dataOut.writeInt(len);
                            dataOut.write(pic);
                            dataOut.writeUTF(rs.getString("INFO"));
                            dataOut.writeUTF(String.valueOf(rs.getInt("TIME")));
//                            dataOut.writeUTF(":P");
                            dataOut.writeUTF(String.valueOf(rs.getDouble("PRICE")));
                            dataOut.flush();
                        }
                    } else if (buf.charAt(0) == 'P') {
                        String sql = "SELECT SEAT FROM HALL WHERE ID = 1";
                        PreparedStatement ps = c.prepareStatement(sql);
//                        ps.setString(1, buf.substring(1));
                        ResultSet rs = ps.executeQuery();
                        int seat = Integer.parseInt(buf.substring(1));
                        if (rs.next()) {
                            String s = rs.getString("SEAT");
                            s = s.substring(0, seat) + "1" + s.substring(seat + 1);
                            sql = "UPDATE HALL SET SEAT= ? WHERE ID = 1";
                            ps = c.prepareStatement(sql);
                            ps.setString(1, s);
//                            ps.setString(2, buf.substring(1, buf.indexOf(':')));
                            ps.executeUpdate();
                        }
                        sql = "INSERT INTO RECORD VALUES(?,?,?,NULL,?)";
                        ps = c.prepareStatement(sql);
                        ps.setString(1, id);
                        ps.setString(2, "1");
                        Timestamp ts = new Timestamp(System.currentTimeMillis());
                        ps.setTimestamp(3, ts);
                        ps.setInt(4, seat);
                        ps.executeUpdate();
                        sendMessage(id, "Z");
                    } else if (buf.charAt(0) == 'Q' || buf.charAt(0) == 'Z') {
//                        sendMessage(id, "000001111100000111110000011111000001111100000111110000011111000001111100000111110000011111000001111100000111110000011111000001111100000111110000011111");
                    }
                    /*else if (buf.charAt(0) == 'M') {
                        String user = buf.substring(1, buf.indexOf(':'));
                    	String time = buf.substring(buf.indexOf(':') + 1);
                    	String sql = "SELECT SEAT FROM RECORD WHERE USER = ? AND HALL = ?";               
                    	PreparedStatement ps = c.prepareStatement(sql);
                    	ps.setString(1, user);
                    	ps.setString(2, id);
                    	ResultSet rs = ps.executeQuery();
                    	if(rs.next()){
                    		int seat = rs.getInt("SEAT");
                    		sql = "SELECT SEAT FROM HALL WHERE ID = ?";
                    		ps = c.prepareStatement(sql);
                    		ps.setString(1, id);
                    		rs = ps.executeQuery();
                    		if(rs.next()){
                                String s = rs.getString("SEAT");
                                s = s.substring(0,seat) + "0" + s.substring(seat+1);
                                sql = "UPDATE HALL SET SEAT= ? WHERE ID = ?";
                                ps = c.prepareStatement(sql);
                                ps.setString(1, s);
                                ps.setString(2, id);
                                ps.executeUpdate();
                            }
                    	}
                    	sql = "UPDATE RECORD SET OUTTIME = ? WHERE USER = ? AND HALL = ?";
                    	ps = c.prepareStatement(sql);
                    	ps.setString(1, time);
                    	ps.setString(2, user);
                    	ps.setString(3, id);
                    	ps.executeQuery();
                    	sql = "SELECT * FROM RECORD WHERE USER = ? AND HALL = ?";
                    	ps = c.prepareStatement(sql);
                    	ps.setString(1, user);
                    	ps.setString(2, id);
                    	rs = ps.executeQuery();
                    	if(rs.next()){
                    		String s1 = rs.getString("INTIME");
                    		String s2 = time;
                    		Timestamp ts1 = Timestamp.valueOf(s1);
                    		Timestamp ts2 = Timestamp.valueOf(s2);
                    		sql = "SELECT * FROM TIMELIST WHERE TIME <= ? AND HALL = ? ORDER BY TIME DESC LIMIT 1";
                    		ps = c.prepareStatement(sql);
                    		ps.setString(1, s1);
                    		ps.setString(1, id);
                    		rs = ps.executeQuery();
                    		String mov = rs.getString("MOVIE");
                    		String timestart1 = rs.getString("TIME");
                    		Timestamp tms1 = Timestamp.valueOf(timestart1);
                    		sql = "SELECT * FROM MOVIE WHERE ID = ?";
                    		ps = c.prepareStatement(sql);
                    		ps.setString(1, mov);
                    		rs = ps.executeQuery();
                    		int time1 = rs.getInt("TIME");
                    		double price1 = rs.getDouble("PRICE");
                    		double timelength = 0;
                    		double price = 0;
                    		timelength = (tms1.getTime() + 60*1000*time1 - ts1.getTime())/(60*1000);
                    		if(timelength >= 0) 
                    			price = price1*timelength/(double)time1;
                    		sql = "SELECT * FROM TIMELIST WHERE TIME >= ? AND TIME <= ? AND HALL = ? ORDER BY TIME";
                    		ps = c.prepareStatement(sql);
                    		ps.setString(1, s1);
                    		ps.setString(2, s2);
                    		ps.setString(3, id);
                    		rs = ps.executeQuery();                		
                    		while(rs.next()){
                    			mov = rs.getString("MOVIE");
                    			timestart1 = rs.getString("TIME");
                    			tms1 = Timestamp.valueOf(timestart1);
                    			sql = "SELECT * FROM MOVIE WHERE ID = ?";
                    			ps = c.prepareStatement(sql);
                    			ResultSet rs1 = ps.executeQuery();                   			
                    			price1 = rs1.getDouble("PRICE");
                    			price = price + price1;
                    			time1 = rs1.getInt("TIME");                  			
                    		}
                    		timelength = tms1.getTime()+ 60*1000*time1 - ts2.getTime();
                    		if(timelength > 0){
                    			price = price - price1*timelength/(60*1000*time1);
                    		}
                    		sql = "SELECT BANKID FROM USERS WHERE ID = ?";
                    		ps = c.prepareStatement(sql);
                    		ps.setString(1, user);
                    		rs = ps.executeQuery();
                    		String s = rs.getString("BANKID");
                    		sql = "UPDATE ACCOUNT SET MONEY = MONEY - ? WHERE BANKID = ?";
                    		ps = c.prepareStatement(sql);
                    		ps.setDouble(1, price);
                    		ps.setString(2, s);
                    		rs = ps.executeQuery();
                    		if(rs.next()){
                    			DataOutputStream dataOut = null;
                        		for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
                        			ClientOut c = (ClientOut) e.nextElement();
                        			if (c.id.equals(user)) {
                        				dataOut = c.remoteOut;
                        				break;
                        			}
                        		}
                        		dataOut.writeUTF("M" + s + ":" + price);
                       			dataOut.flush();
                    		}              			
                   		}              	
                    }*/
//                    if (buf.charAt(0) == 'P') {
//                        int size = remoteIn.readInt();
//                        byte[] data = new byte[size];
//                        int len = 0;
//                        while (len < size) {
//                            len += remoteIn.read(data, len, size - len);
//                        }
//                        String sql = "UPDATE USERINFO SET PICTURE= ? WHERE ID = ?";
//                        PreparedStatement ps = c.prepareStatement(sql);
//                        ps.setString(2, id);
//                        ps.setBytes(1, data);
//                        ps.executeUpdate();
//                        ps.close();
//                    } else if (buf.charAt(0) == 'F') {
//                        String sql = "UPDATE USERINFO SET INFO= ? WHERE ID = ?";
//                        PreparedStatement ps = c.prepareStatement(sql);
//                        ps.setString(2, id);
//                        ps.setString(1, buf.substring(1));
//                        ps.executeUpdate();
//                    } else if (buf.charAt(0) == 'I') {
//                        ResultSet rs;
//                        String sql = "SELECT * FROM USERINFO WHERE ID = ?";
//                        PreparedStatement ps = c.prepareStatement(sql);
//                        ps.setString(1, buf.substring(1));
//                        rs = ps.executeQuery();
//                        if (rs.next()) {
//                            DataOutputStream dataOut = null;
//                            for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
//                                ClientOut c = (ClientOut) e.nextElement();
//                                if (c.id.equals(id)) {
//                                    dataOut = c.remoteOut;
//                                    break;
//                                }
//                            }
//                            byte[] bytes = rs.getBytes("PICTURE");
//                            Boolean flag1 = false, flag2 = false;
//                            if (bytes != null) {
//                                int len = bytes.length;
//                                dataOut.writeUTF("P");
//                                dataOut.writeInt(len);
//                                dataOut.write(bytes);
//                                dataOut.flush();
//                                flag1 = true;
//                            }
//                            String info = rs.getString("INFO");
//                            if (info != null) {
//                                dataOut.writeUTF("F" + info);
//                                dataOut.flush();
//                                flag2 = true;
//                            }
//                            String out = "I";
//                            if (flag1) out += "Y";
//                            else out += "N";
//                            if (flag2) out += "Y";
//                            else out += "N";
//                            dataOut.writeUTF(out + buf.substring(1));
//                            dataOut.flush();
//                        }
//                    } else {
//                        target = buf.substring(0, buf.indexOf(':'));
//                        msg = buf.substring(buf.indexOf(':') + 1);
//                        System.out.println(buf);
//                        sendMessage(target, "M" + this.id + ":" + msg);
//                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage() + ": Connection to perr lost.");
                e.printStackTrace();
                for (Enumeration en = clients.elements(); en.hasMoreElements(); ) {
                    ClientOut co = (ClientOut) en.nextElement();
                    if (co.id.equals(this.id)) {
                        System.out.println(e.getMessage() + ": Failed to send message to client.");
                        infoTxt.append(this.id + " disconnected.\n");
                        clients.removeElement(co);
                        String sql = "DELETE FROM ONLINE WHERE ID=?";
                        try {
                            PreparedStatement ps = c.prepareStatement(sql);
                            ps.setString(1, id);
                            ps.executeUpdate();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    void updateMovie(byte[] pic, String info, String id, String time, String price) {
        try {
            if (pic.length > 0) {
                String sql = "UPDATE MOVIE SET PICTURE=?, INFO=?, TIME=?, PRICE=? WHERE ID=?";
                PreparedStatement pstmt = c.prepareStatement(sql);
                pstmt.setBytes(1, pic);
                pstmt.setString(2, info);
                pstmt.setInt(3, Integer.parseInt(time));
                pstmt.setDouble(4, Double.parseDouble(price));
                pstmt.setString(5, id);
                pstmt.executeUpdate();
            } else {
                String sql = "UPDATE MOVIE SET INFO=?, TIME=?, PRICE=? WHERE ID=?";
                PreparedStatement pstmt = c.prepareStatement(sql);
                pstmt.setString(1, info);
                pstmt.setInt(2, Integer.parseInt(time));
                pstmt.setDouble(3, Double.parseDouble(price));
                pstmt.setString(4, id);
                pstmt.executeUpdate();
            }
            updateTable();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Time should be an integer.\nPrice should be a double number.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        ServerWindow aWindow = new ServerWindow("Server");
        Toolkit theKit = aWindow.getToolkit();
        Dimension wndSize = theKit.getScreenSize();
        aWindow.setBounds(wndSize.width / 4, wndSize.height / 8,   // Position
                wndSize.width / 2, wndSize.height * 3 / 4);
        aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        aWindow.setVisible(true);
    }

}
