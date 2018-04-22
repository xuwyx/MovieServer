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
    TextArea infoTxt;
    Connection c = null;
    Statement stmt;
    JTable table;
    String selectedId;
    private DefaultTableModel tableModel;   //表格模型对象
    private DefaultTableModel hallTableModel;   //表格模型对象

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
        JButton addHall = new JButton("Add Movie");
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
        table = new JTable(tableModel);
        hallTable = new JTable(hallTableModel);

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
                    showEditInfo(bytes, info);
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

        updateTable();
        updateHallTable();

        new Listening(this.port).start();
    }

    void showEditInfo(byte[] bytes, String info) {
        EditInfoWindow aWindow = new EditInfoWindow("Movie Information", bytes, info, this, selectedId);
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

    void updateHallTable() {
        try {
            String[][] data = {};
            String[] column = {"Hall"};
            tableModel = new DefaultTableModel(data, column) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            String sql = "SELECT * FROM HALL";
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
            try {
                while (true) {
                    int flag = 0;
                    socket = server.accept();
                    DataInputStream remoteIn = new DataInputStream(socket.getInputStream());
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    String buf = remoteIn.readUTF();
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
                            sql = "INSERT INTO USERS(ID, PASSWORD) VALUES (?, ?)";
                            pstmt = c.prepareStatement(sql);
                            pstmt.setString(1, id);
                            pstmt.setString(2, ps);
                            pstmt.executeUpdate();
                            infoTxt.append(id + " sign up.\n");
                            dataOut.writeUTF("Y");
                            dataOut.flush();
                            updateTable();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
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
                    if (buf.charAt(0) == 'S') {
                        String sql = "SELECT SEAT FROM HALL WHERE ID = ?";
                        PreparedStatement ps = c.prepareStatement(sql);
                        ps.setString(1, id);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            DataOutputStream dataOut = null;
                            for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
                                ClientOut c = (ClientOut) e.nextElement();
                                if (c.id.equals(id)) {
                                    dataOut = c.remoteOut;
                                    break;
                                }
                            }
                            dataOut.writeUTF("S" + rs.getString("SEAT"));
                            dataOut.flush();
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
                        ResultSet rs = ps.executeQuery();
                        String out = "P";
                        DataOutputStream dataOut = null;
                        for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
                            ClientOut c = (ClientOut) e.nextElement();
                            if (c.id.equals(id)) {
                                dataOut = c.remoteOut;
                                break;
                            }
                        }
                        if (rs.next()) {
                            byte[] pic = rs.getBytes("PICTURE");
                            int len = pic.length;
                            dataOut.writeUTF(out);
                            dataOut.writeInt(len);
                            dataOut.write(pic);
                            dataOut.writeUTF("I" + rs.getString("INFO") + "T:");
                            dataOut.writeInt(rs.getInt("TIME"));
                            dataOut.writeUTF(":P");
                            dataOut.writeDouble(rs.getDouble("PRICE"));
                            dataOut.flush();
                        }
                    } else if (buf.charAt(0) == 'P') {
                        String sql = "SELECT SEAT FROM HALL WHERE ID = ?";
                        PreparedStatement ps = c.prepareStatement(sql);
                        ps.setString(1, buf.substring(1, buf.indexOf(':')));
                        ResultSet rs = ps.executeQuery();
                        int seat = Integer.parseInt(buf.substring(buf.indexOf(':')+1));
                        if(rs.next()){
                            String s = rs.getString("SEAT");
                            s = s.substring(0,seat) + "1" + s.substring(seat+1);
                            sql = "UPDATE HALL SET SEAT= ? WHERE ID = ?";
                            ps = c.prepareStatement(sql);
                            ps.setString(1, s);
                            ps.setString(2, buf.substring(1, buf.indexOf(':')));
                            ps.executeUpdate();
                        }
                        sql = "INSERT INTO RECORD VALUES(?,?,?,NULL,?)";
                        ps = c.prepareStatement(sql);
                        ps.setString(1, id);
                        ps.setString(2, buf.substring(1, buf.indexOf(':')));
                        Timestamp ts = new Timestamp(System.currentTimeMillis());
                        ps.setTimestamp(3, ts);
                        ps.setInt(4,seat);
                        ps.executeUpdate();
                    }
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


    void updateMovie(byte[] pic, String info, String id) {
        try {
            if (pic.length > 0) {
                String sql = "UPDATE MOVIE SET PICTURE=?, INFO=? WHERE ID=?";
                PreparedStatement pstmt = c.prepareStatement(sql);
                pstmt.setBytes(1, pic);
                pstmt.setString(2, info);
                pstmt.setString(3, id);
                pstmt.executeUpdate();
            } else {
                String sql = "UPDATE MOVIE SET INFO=? WHERE ID=?";
                PreparedStatement pstmt = c.prepareStatement(sql);
                pstmt.setString(1, info);
                pstmt.setString(2, id);
                pstmt.executeUpdate();
            }
            updateTable();
        } catch (SQLException e) {
            e.printStackTrace();
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