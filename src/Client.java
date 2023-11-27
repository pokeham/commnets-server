import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
//header
//code
//SRCipaddress
//Destipaddress
//SRCsocket
//DESTsocket
//message
//checksum

//handshaking
//client sends to server destination address and src address
//server then sends a test packet to the destination
//destinion client responds with ackowledgement
//server sends acknowdlgement to src client

public class Client extends JFrame {

    private JPanel clientPanel;
    private JPanel messagePanel;
    private JTextArea messageField;
    private JButton sendButton;
    private JScrollPane scroll;
    private JPanel chatPanel;
    private JTextArea chatArea;
    private JTextArea welcomeArea;
    private String systemIP;
    private String localIP;
    private DatagramSocket socket;
    public Client(){
        clientPanel = new JPanel();
        try {
            localIP = InetAddress.getLocalHost().getHostAddress().trim();
        } catch (UnknownHostException e) {
            System.out.println("Error Fetching Local IP!");
            e.printStackTrace();
        }
        try
        {
            URL url_name = new URL("http://checkip.amazonaws.com");

            BufferedReader sc =
                    new BufferedReader(new InputStreamReader(url_name.openStream()));

            systemIP = sc.readLine().trim();
        }
        catch (Exception e)
        {
            System.out.println("Error Fetching System IP!");
            e.printStackTrace();
        }
        welcomeArea = new JTextArea("Welcome!\n");
        welcomeArea.append("Local IP : " + localIP +"\n");
        welcomeArea.append("System IP : " + systemIP +"\n");
        clientPanel.add(welcomeArea, BorderLayout.NORTH);
        //fix formatting
        chatPanel = new JPanel();
        chatPanel.setBorder(new TitledBorder(new EtchedBorder()));
        chatArea = new JTextArea(30,40);
        chatArea.setEditable(false);
        chatArea.setWrapStyleWord(true);
        chatArea.setLineWrap(true);
        scroll = new JScrollPane(chatArea,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        chatPanel.add(scroll);
        clientPanel.add(chatPanel);
        messagePanel = new JPanel();
        messageField = new JTextArea(3,30);
        messageField.setWrapStyleWord(true);
        messageField.setLineWrap(true);
        messageField.setEditable(true);
        messageField.setText("");
        messagePanel.add(messageField,BorderLayout.WEST);
        sendButton = new JButton("Send!");
        sendButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        sendMessage("1");//send message code
                    }
                }
        );
        messagePanel.add(sendButton,BorderLayout.EAST);
        clientPanel.add(messagePanel,BorderLayout.NORTH);
        setContentPane(clientPanel);
        setSize(500,700);
        setResizable(false);
        setVisible(true);
        try{
            socket = new DatagramSocket(0,InetAddress.getLocalHost());
        }catch (SocketException socketException){
            socketException.printStackTrace();
            System.out.println("Socket Error");
            System.exit(1);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        sendMessage("0");//handshaking code
        while (true){
            try{
                byte[] data = new byte[256];
                DatagramPacket recievePacket = new DatagramPacket(data,data.length);
                socket.receive(recievePacket);
                System.out.println( new String(recievePacket.getData(),0, recievePacket.getLength()));
                String packet = new String(recievePacket.getData(),0, recievePacket.getLength());
                String payload = packet.substring(1,packet.indexOf("----"));
                if(packet.charAt(0)==('1')){
                    if(!checksum(recievePacket)){
                        displayMessage("PACKET LOSS!");
                    }else{
                        displayMessage("SUCCESS------>");
                        displayMessage(payload+"\n");
                        break;
                    }
                }else{
                    if(!checksum(recievePacket)){
                        displayMessage("PACKET LOSS!");
                    }
                    displayMessage("ERROR---------->");
                    displayMessage(payload+"\n");
                    sendMessage("0");
                }
                if(!checksum(recievePacket)){
                    displayMessage("PACKET LOSS!+\n");

                }
            }catch (IOException e){
                displayMessage(e + "\n");
                e.printStackTrace();
            }
        }
        messageField.setText("");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    public static int sum(byte[] array) {
        int result = 0;
        for (final byte v : array) {
            result += v;
        }
        return result;
    }
    public static int sumString(String array) {
        int result = 0;
        for ( char c: array.toCharArray()) {
            result += c;
        }
        return result;
    }
    public void sendMessage(String code){
        String msg = messageField.getText();
        System.out.println(msg);
        if (msg.contains("<<<<") || msg.contains(">>>>")) {
            messageField.setText("INVALID MESSAGE");
        }else{
            try {
                byte[] packet = createMSG(code,msg);
                send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            messageField.setText("");
        }


    }
    public byte[] createMSG(String code,String msg) throws IOException {
        String end = ">>>>";
        String s = "<<<<";
        String destP = "00001";
        String srcP = "00000";
        byte[] srcPort = srcP.getBytes(StandardCharsets.UTF_8);
        byte[] destPort = destP.getBytes(StandardCharsets.UTF_8);
        byte[] start = s.getBytes(StandardCharsets.UTF_8);
        byte[] endH = end.getBytes(StandardCharsets.UTF_8);
        byte[] status = code.getBytes(StandardCharsets.UTF_8);
        byte[] message = msg.trim().getBytes(StandardCharsets.UTF_8);
        byte[] sysIp = systemIP.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(status);
        outputStream.write(sysIp);
        outputStream.write(srcPort);
        outputStream.write(destPort);
        outputStream.write(start);
        outputStream.write(message);
        outputStream.write(endH);
        byte[] result = outputStream.toByteArray();
        Integer sum = sum(result);
        System.out.println(sum);
        String temp = String.valueOf(sum);
        System.out.println(temp);
        ByteArrayOutputStream whatever = new ByteArrayOutputStream( );
        whatever.write(result);
        whatever.write(temp.getBytes(StandardCharsets.UTF_8));
        byte[] resultFinal = whatever.toByteArray();
        System.out.println(resultFinal);
        System.out.println(new String(resultFinal,StandardCharsets.UTF_8));
        return resultFinal;
    }
    public void send(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data,data.length,InetAddress.getLocalHost(),1);
        socket.send(packet);
    }
    public boolean checksum(DatagramPacket packet){
        String requestCheck = new String(packet.getData(),0,packet.getLength());
        System.out.println(requestCheck);
        if(requestCheck.indexOf("----") != -1){
            String check = requestCheck.substring(requestCheck.indexOf("----")+4);
            System.out.println(check);
            int sum = sumString(requestCheck.substring(0,requestCheck.indexOf("----")+4));
            System.out.println(requestCheck.indexOf("----"));
            System.out.println(sum);
            int intVersion = Integer.parseInt(check);
            System.out.println(intVersion);
            if(sum == intVersion){
                return true;
            }
            return false;
        }else{
            System.out.println("WhoopS!");
        }
        return false;
    }
    public void waitForPackets(){
        while (true){
            try{
                byte[] data = new byte[256];
                DatagramPacket recievePacket = new DatagramPacket(data,data.length);
                socket.receive(recievePacket);
                System.out.println( new String(recievePacket.getData(),0, recievePacket.getLength()));
                String packet = new String(recievePacket.getData(),0, recievePacket.getLength());
                String payload = packet.substring(1,packet.indexOf("----"));
                if(packet.charAt(0)==('1')){
                    displayMessage("SUCCESS------>");
                }else{
                    displayMessage("ERROR---------->");
                }
                displayMessage(payload+"\n");
                if(!checksum(recievePacket)){
                    displayMessage("PACKET LOSS!");

                }
            }catch (IOException e){
                displayMessage(e + "\n");
                e.printStackTrace();
            }
        }
    }
    private void displayMessage(final String message){
        SwingUtilities.invokeLater(
                () -> chatArea.append(message)
        );
        chatArea.setCaretPosition(chatArea.getText().length());
    }


}
