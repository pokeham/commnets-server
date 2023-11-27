import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Server extends JFrame {
    //header
    //code
    //SRCipaddress
    //Destipaddress
    private String port;
    private String ip;
    private DatagramSocket socket;
    private JPanel display = new JPanel();
    private JTextArea displayArea = new JTextArea();
    public Server(){
        displayArea.setEditable(false);
        display.add(displayArea);
        setContentPane(display);
        setSize(300,300);
        setVisible(true);
        {
            try {
                socket = new DatagramSocket(1, InetAddress.getLocalHost());
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    public void receiveRequests(){
        while(true){
            try {
                byte[] data = new byte[256];
                 DatagramPacket request = new DatagramPacket(data, data.length);
                displayMessage("\nWaiting...");
                socket.receive(request);
                displayMessage("\nReceived!");
                displayMessage("\nIP"+ request.getAddress() +
                        "\nPort"+ request.getPort() +
                        "\nContaining:"+ new String(request.getData(),0,request.getLength()));
                sendResponse(request);
            }catch (IOException e){
                System.out.println("IoException");
            }
        }

    }
    public static int sumByte(byte[] array) {
        int result = 0;
        for (final byte v : array) {
            result += v;
        }
        return result;
    }
    public static int sum(String array) {
        int result = 0;
        for ( char c: array.toCharArray()) {
            result += c;
        }
        return result;
    }
    public boolean validate(DatagramPacket request){
        String requestCheck = new String(request.getData(),0,request.getLength());
        System.out.println(requestCheck);
        if(requestCheck.indexOf(">>>>") != -1){
            String check = requestCheck.substring(requestCheck.indexOf(">>>>")+4);
            System.out.println(check);
            int sum = sum(requestCheck.substring(0,requestCheck.indexOf(">>>>")+4));
            System.out.println(requestCheck.indexOf(">>>>"));
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
    static private String convertDecimalToFraction(double x){
        if (x < 0){
            return "-" + convertDecimalToFraction(-x);
        }
        double tolerance = 1.0E-6;
        double h1=1; double h2=0;
        double k1=0; double k2=1;
        double b = x;
        do {
            double a = Math.floor(b);
            double aux = h1; h1 = a*h1+h2; h2 = aux;
            aux = k1; k1 = a*k1+k2; k2 = aux;
            b = 1/(b-a);
        } while (Math.abs(x-h1/k1) > x*tolerance);

        return h1+"/"+k1;
    }
    public static boolean allCharactersSame(String s)
    {
        int n = s.length();
        for (int i = 1; i < n; i++)
            if (s.charAt(i) != s.charAt(0))
                return false;

        return true;
    }
    public void sendResponse(DatagramPacket request) throws IOException {
        if(validate(request)){
            String test = new String(request.getData(),0,request.getLength());
            char[] temp = test.toCharArray();

            System.out.println(temp);
            if(temp[0] == '0') {
                String code = "1";
                byte[] codeH = code.getBytes(StandardCharsets.UTF_8);
                String res = "Firm Handshake :)";
                byte[] buf = res.getBytes(StandardCharsets.UTF_8);
                String end = "----";
                byte[] endt = end.getBytes(StandardCharsets.UTF_8);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write(codeH);
                outputStream.write(buf);
                outputStream.write(endt);
                byte[] x = outputStream.toByteArray();
                int sum = sumByte(x);
                String xt = String.valueOf(sum);
                byte[] xtt = xt.getBytes(StandardCharsets.UTF_8);
                ByteArrayOutputStream whatever = new ByteArrayOutputStream( );
                whatever.write(x);
                whatever.write(xtt);
                byte[] result = whatever.toByteArray();
                DatagramPacket response = new DatagramPacket(result,result.length,request.getAddress(),request.getPort());
                socket.send(response);
            }else if(temp[0] == '1'){
                if(!(test.substring(test.indexOf("<<<<")+4,test.indexOf(">>>>")).matches("[0-9.]*")) || allCharactersSame(test.substring(test.indexOf("<<<<")+4,test.indexOf(">>>>")))){
                    String code = "0";
                    byte[] codeH = code.getBytes(StandardCharsets.UTF_8);
                    String res = "Invalid message payload :(";
                    byte[] buf = res.getBytes(StandardCharsets.UTF_8);
                    String end = "----";
                    byte[] endt = end.getBytes(StandardCharsets.UTF_8);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                    outputStream.write(codeH);
                    outputStream.write(buf);
                    outputStream.write(endt);
                    byte[] x = outputStream.toByteArray();
                    int sum = sumByte(x);
                    String xt = String.valueOf(sum);
                    byte[] xtt = xt.getBytes(StandardCharsets.UTF_8);
                    ByteArrayOutputStream whatever = new ByteArrayOutputStream( );
                    whatever.write(x);
                    whatever.write(xtt);
                    byte[] result = whatever.toByteArray();
                    DatagramPacket response = new DatagramPacket(result,result.length,request.getAddress(),request.getPort());
                    socket.send(response);
                    return;
                }
                String code = "1";
                byte[] codeH = code.getBytes(StandardCharsets.UTF_8);
                double dou = Double.parseDouble(test.substring(test.indexOf("<<<<")+4,test.indexOf(">>>>")));
                String ans = convertDecimalToFraction(dou);
                byte[] buf = ans.getBytes(StandardCharsets.UTF_8);
                String end = "----";
                byte[] endt = end.getBytes(StandardCharsets.UTF_8);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write(codeH);
                outputStream.write(buf);
                outputStream.write(endt);
                byte[] x = outputStream.toByteArray();
                int sum = sumByte(x);
                String xt = String.valueOf(sum);
                byte[] xtt = xt.getBytes(StandardCharsets.UTF_8);
                ByteArrayOutputStream whatever = new ByteArrayOutputStream( );
                whatever.write(x);
                whatever.write(xtt);
                byte[] result = whatever.toByteArray();
                DatagramPacket response = new DatagramPacket(result,result.length,request.getAddress(),request.getPort());
                socket.send(response);
                //status code 1
            }else {
                String code = "0";
                byte[] codeH = code.getBytes(StandardCharsets.UTF_8);
                String res = "Invalid Status :(";
                byte[] buf = res.getBytes(StandardCharsets.UTF_8);
                String end = "----";
                byte[] endt = end.getBytes(StandardCharsets.UTF_8);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write(codeH);
                outputStream.write(buf);
                outputStream.write(endt);
                byte[] x = outputStream.toByteArray();
                int sum = sumByte(x);
                String xt = String.valueOf(sum);
                byte[] xtt = xt.getBytes(StandardCharsets.UTF_8);
                ByteArrayOutputStream whatever = new ByteArrayOutputStream( );
                whatever.write(x);
                whatever.write(xtt);
                byte[] result = whatever.toByteArray();
                DatagramPacket response = new DatagramPacket(result,result.length,request.getAddress(),request.getPort());
                socket.send(response);
            }
        }else{
            String code = "0";
            byte[] codeH = code.getBytes(StandardCharsets.UTF_8);
            String res = "Packet Loss :(";
            byte[] buf = res.getBytes(StandardCharsets.UTF_8);
            String end = "----";
            byte[] endt = end.getBytes(StandardCharsets.UTF_8);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(codeH);
            outputStream.write(buf);
            outputStream.write(endt);
            byte[] x = outputStream.toByteArray();
            int sum = sumByte(x);
            String xt = String.valueOf(sum);
            byte[] xtt = xt.getBytes(StandardCharsets.UTF_8);
            ByteArrayOutputStream whatever = new ByteArrayOutputStream( );
            whatever.write(x);
            whatever.write(xtt);
            byte[] result = whatever.toByteArray();
            DatagramPacket response = new DatagramPacket(result,result.length,request.getAddress(),request.getPort());
            socket.send(response);
        }

    }
    private void displayMessage(final String message){
        SwingUtilities.invokeLater(
                () -> displayArea.append(message)
        );
        displayArea.setCaretPosition(displayArea.getText().length());
    }
}
