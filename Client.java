package client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.Socket;

public class Client {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public Client() throws IOException{
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        runClient();
    }


    private void runClient(){
        JFrame frame = new JFrame("NetworkStorage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,400);

        JList<File> jListClient;
        JList<File> jListServer;
        JLabel jClient = new JLabel("Client");
        JLabel jServer = new JLabel("Server");
        JTextArea ta = new JTextArea("Enter your message: ");
        JButton upload = new JButton("Upload to server");
        JButton download = new JButton("Download from server");
        JButton remove = new JButton("Remove");

        File myFolder = new File("Client/");
        File[] files = myFolder.listFiles();
        jListClient = new JList(files);

        File serverFolder = new File("Server/");
        File[] sfiles = serverFolder.listFiles();
        jListServer = new JList(sfiles);

        JPanel topPanel = new JPanel(new GridLayout(0,2));
        JPanel middlePanel = new JPanel(new GridLayout(0,2)); //new BorderLayout()
        JPanel lowPanel = new JPanel(new BorderLayout()); //new FlowLayout(FlowLayout.CENTER)
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.CENTER));

        topPanel.add(jClient);
        topPanel.add(jServer);

        middlePanel.add(jListClient);
        middlePanel.add(jListServer);


        lowPanel.add(ta, BorderLayout.NORTH);
        panelButton.add(upload);
        panelButton.add(download);
        panelButton.add(remove);
        lowPanel.add(panelButton, BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(middlePanel, BorderLayout.CENTER);
        frame.add(lowPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);



        upload.addActionListener(a ->{
            System.out.println(sendFile(ta.getText()));
        });

        download.addActionListener(a ->{
            receiveFile(ta.getText());
        });

        remove.addActionListener(a ->{
            removeFile(ta.getText());
        });

        ta.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ta.setText("");
            }
        });

        jListClient.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = jListClient.locationToIndex(e.getPoint());
                System.out.println(jListClient.getModel().getElementAt(idx));
                ta.setText("");
                ta.setText(String.valueOf(jListClient.getModel().getElementAt(idx)));
            }
        });
    }

    private void removeFile(String fileName) {
        try {
            out.writeUTF("remove");
            out.writeUTF(fileName);
            //File file = new File("Client" + File.separator + fileName);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void receiveFile(String fileName) {
        try {
            out.writeUTF("download");
            out.writeUTF(fileName);
            File file = new File("Client" + File.separator + fileName);
            if (!file.exists()){
                file.createNewFile();
            }
            long size = in.readLong();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[256];
            for (int i = 0; i < (size + 256) / 256; i++) {
                int read = in.read(buffer);
                fos.write(buffer,0,read);
            }
            fos.close();
            out.writeUTF("DONE");
        } catch (Exception e){
            //out.writeUTF("ERROR");
        }
    }




    private String sendFile(String fileName) {
        try {
            File file = new File("client" + File.separator + fileName);
            if (file.exists()){
                out.writeUTF("upload");
                out.writeUTF(fileName);
                long length = file.length();
                out.writeLong(length);
                FileInputStream fis = new FileInputStream(file);
                int read = 0;
                byte[] buffer = new byte[256];
                while ((read = fis.read(buffer)) != -1){
                    out.write(buffer, 0, read);
                }
                out.flush();

                String status = in.readUTF();
                return status;
            } else {
                return "File is not exists";
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return "Something error";

    }

    public static void main(String[] args) throws IOException {
        new Client();
    }
}

