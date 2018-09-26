import java.io.*;
import java.net.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.*;

class TCPClient extends JFrame implements ActionListener, MouseListener {
    JPanel panel;
    JTextField txt;
    JButton up,down;
    JLabel error, servFiles;
    String dirName;
    Socket clientSocket;
    InputStream inFromServer;
    OutputStream outToServer;
    BufferedInputStream bis;
    PrintWriter pw;
    String name, file, path;
    String hostAddr;
    int portNumber;
    int c;
    int size = 9022386;
    JList<String> filelist;
    String[] names = new String[10000];
    int number_of_files; // number of files on the server retrieved

    public TCPClient(String dir, String host, int port) {
        super("CLIENT INTERFACE");

        dirName = dir;
        hostAddr = host;
        portNumber = port;
        BufferedImage myPicture = null;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel(null);
        panel.setVisible(true);
        panel.setSize(600,300);
        panel.setBackground(Color.black);

        Font font = new Font("Monaco", Font.BOLD, 30);
        JLabel title;

        title = new JLabel("CLIENT INTERFACE");
        title.setFont(font);
        title.setForeground(Color.white);
        title.setBounds(300, 50, 400, 50);
        panel.add(title);

        Font labelfont = new Font("Helvetica Neue", Font.PLAIN, 20);
        JLabel subTitle;
        subTitle = new JLabel("Enter File Name :");
        subTitle.setFont(labelfont);
        subTitle.setForeground(Color.darkGray);
        subTitle.setBounds(100, 450, 200, 50);
        panel.add(subTitle);

        txt = new JTextField();
        txt.setBounds(400, 450, 500, 50);
        panel.add(txt);

        up = new JButton("Upload");
        up.setBounds(250, 550, 200, 50);
        up.setFont(new Font("Arial", Font.BOLD, 14));
        up.setBackground(Color.LIGHT_GRAY);
        up.setForeground(Color.WHITE);
        panel.add(up);

        down = new JButton("Download");
        down.setBounds(550, 550, 200, 50);
        up.setFont(new Font("Arial", Font.BOLD, 14));
        down.setBackground(Color.lightGray);
        down.setForeground(Color.WHITE);
        panel.add(down);

        error = new JLabel("");
        error.setFont(labelfont);
        error.setBounds(200, 650, 600, 50);
        panel.add(error);

        up.addActionListener(this);
        down.addActionListener(this);

        up.setForeground(Color.black);
        down.setForeground(Color.black);

        try {
            clientSocket = new Socket(hostAddr, portNumber);
            inFromServer = clientSocket.getInputStream();
            pw = new PrintWriter(clientSocket.getOutputStream(), true);
            outToServer = clientSocket.getOutputStream();
            ObjectInputStream oin = new ObjectInputStream(inFromServer);
            String s = (String) oin.readObject();
            System.out.println(s);

            number_of_files = Integer.parseInt((String) oin.readObject());
            System.out.println(number_of_files);

            String[] temp_names = new String[number_of_files];

            for(int i=0; i<number_of_files; i++) {
                String filename = (String) oin.readObject();
                System.out.println(filename);
                names[i] = filename;
                temp_names[i] = filename;
            }

            Arrays.sort(temp_names);

            servFiles = new JLabel("Files in the Server Directory :");
            servFiles.setBounds(350, 125, 400, 50);
            panel.add(servFiles);

            filelist = new JList<>(temp_names);
            filelist.setVisibleRowCount(10);
            filelist.setFixedCellHeight(50);
            filelist.setFixedCellWidth(140);
            filelist.setBackground(Color.DARK_GRAY);
            filelist.setForeground(Color.BLACK);
            filelist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane scroll = new JScrollPane(filelist,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setBounds(300, 200, 400, 200);
            filelist.setBackground(Color.DARK_GRAY);
            filelist.setForeground(Color.black);

            panel.add(scroll);
            filelist.addMouseListener(this);

        }
        catch (Exception exc) {
            System.out.println("Exception: " + exc.getMessage());
            error.setText("Exception:" + exc.getMessage());
            error.setBounds(300,125,600,50);
            panel.revalidate();
        }

        getContentPane().add(panel);
    }

    public void mouseClicked(MouseEvent click) {
        if (click.getClickCount() == 2) {
            String selectedItem = (String) filelist.getSelectedValue();
            txt.setText(selectedItem);
            panel.revalidate();
        }
    }

    public void mousePressed(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == up) {
            try {
                name = txt.getText();

                FileInputStream file = null;
                BufferedInputStream bis = null;

                boolean fileExists = true;
                path = dirName + name;

                try {
                    file = new FileInputStream(path);
                    bis = new BufferedInputStream(file);
                } catch (FileNotFoundException ex) {
                    fileExists = false;
                    System.out.println("FileNotFoundException:" + ex.getMessage());
                    error.setText("FileNotFoundException:" + ex.getMessage());
                    panel.revalidate();
                }

                if (fileExists) {
                    pw.println(name);

                    System.out.println("Upload begins");
                    error.setText("Upload begins");
                    panel.revalidate();

                    sendBytes(bis, outToServer);
                    System.out.println("Completed");
                    error.setText("Completed");
                    panel.revalidate();

                    boolean exists = false;
                    for(int i=0; i<number_of_files; i++) {
                        if(names[i].equals(name)) {
                            exists = true;
                            break;
                        }
                    }

                    if(!exists) {
                        names[number_of_files] = name;
                        number_of_files++;
                    }

                    String[] temp_names = new String[number_of_files];
                    for(int i=0; i<number_of_files; i++) {
                        temp_names[i] = names[i];
                    }

                    Arrays.sort(temp_names);
                    filelist.setListData(temp_names);

                    bis.close();
                    file.close();
                    outToServer.close();
                }
            }
            catch (Exception exc) {
                System.out.println("Exception: " + exc.getMessage());
                error.setText("Exception:" + exc.getMessage());
                panel.revalidate();
            }
        }
        else if (event.getSource() == down) {
            try {
                File directory = new File(dirName);

                if (!directory.exists()) {
                    directory.mkdir();
                }
                boolean complete = true;
                byte[] data = new byte[size];
                name = txt.getText();
                file = new String("*" + name + "*");
                pw.println(file);

                ObjectInputStream oin = new ObjectInputStream(inFromServer);
                String s = (String) oin.readObject();

                if(s.equals("Success")) {
                    File f = new File(directory, name);
                    FileOutputStream fileOut = new FileOutputStream(f);
                    DataOutputStream dataOut = new DataOutputStream(fileOut);

                    while (complete) {
                        c = inFromServer.read(data, 0, data.length);
                        if (c == -1) {
                            complete = false;
                            System.out.println("Completed");
                            error.setText("Completed");
                            panel.revalidate();

                        } else {
                            dataOut.write(data, 0, c);
                            dataOut.flush();
                        }
                    }
                    fileOut.close();
                }
                else {
                    System.out.println("Requested file not found on the server.");
                    error.setText("Requested file not found on the server.");
                    panel.revalidate();
                }
            }
            catch (Exception exc) {
                System.out.println("Exception: " + exc.getMessage());
                error.setText("Exception:" + exc.getMessage());
                panel.revalidate();
            }
        }
    }

    private static void sendBytes(BufferedInputStream in , OutputStream out) throws Exception {
        int size = 9022386;
        byte[] data = new byte[size];
        int bytes = 0;
        int c = in.read(data, 0, data.length);
        out.write(data, 0, c);
        out.flush();
    }

    public static void main(String args[]) {

        String srvr_dir,client_ip;
        int port_number = 5021;

        if(args.length==0) {

            System.out.println("Please enter the directory address: ");
            Scanner sc = new Scanner(System.in);
            srvr_dir = sc.nextLine();

            System.out.println("Please enter the IP address of server: ");
            client_ip = sc.nextLine();

            TCPClient tcp = new TCPClient(srvr_dir, client_ip, 5021);
            tcp.setSize(1000, 900);
            tcp.setVisible(true);
        }

        if(args.length >= 3){
            TCPClient tcp = new TCPClient(args[0], args[1], Integer.parseInt(args[2]));
            tcp.setSize(1000, 900);
            tcp.setVisible(true);
        }

        else if(args.length == 2){
            TCPClient tcp = new TCPClient(args[0], args[1], 5021);
            tcp.setSize(1000, 900);
            tcp.setVisible(true);
        }

        else if(args.length == 1){
            TCPClient tcp = new TCPClient(args[0], "localhost", 5021);
            tcp.setSize(1000, 900);
            tcp.setVisible(true);
        }

    }
}