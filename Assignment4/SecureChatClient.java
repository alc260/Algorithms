import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.math.BigInteger;

public class SecureChatClient extends JFrame implements Runnable, ActionListener{

    //set port value
    public static final int PORT = 8765;

    ObjectInputStream myReader;
    ObjectOutputStream myWriter; 

    JTextArea outputArea;
    JLabel prompt;
    JTextField inputField;

    String myName, serverName;
    Socket connection;

    BigInteger E, N, key;

    byte[] eName;
    BigInteger eKey;
    SymCipher cipher;
    String cipherType;

    public SecureChatClient(){

        try{
            //retrieve user input for name and server values
            myName = JOptionPane.showInputDialog(this, "Enter your user name: ");
            serverName = JOptionPane.showInputDialog(this, "Enter the server name: ");
            InetAddress addr = InetAddress.getByName(serverName);
            connection = new Socket(addr, PORT);   // Connect to server with new socket

            //objects created for writing and reading 
            myWriter = new ObjectOutputStream(connection.getOutputStream());
            myWriter.flush(); 
            myReader = new ObjectInputStream(connection.getInputStream()); 

            //read public key, mod value, and preferred symmetric cipher using ObjectInputStream 
            E = (BigInteger) myReader.readObject(); 
            N = (BigInteger) myReader.readObject();
            cipherType = (String) myReader.readObject();  
            System.out.println("E: " + E + "\nN: " + N);
            System.out.println("Encryption type: " + cipherType);

            //create cipher
            if (cipherType.equals("Sub")){
                cipher = new Substitute();
            } else if (cipherType.equals("Add")){
                cipher = new Add128();
            }

            //convert key to big integer and encrypt using E and N
            key = new BigInteger(1, cipher.getKey()); 
            eKey = key.modPow(E, N); 
            System.out.println("Symmetric Key: " + key);

            //write encryption to server
            myWriter.writeObject(eKey);
            myWriter.flush(); 

            //encrypt name and write to server
            eName = cipher.encode(myName);
            myWriter.writeObject(eName); 
            myWriter.flush();

            //gui interface starts here
            this.setTitle(myName);

            Box b = Box.createHorizontalBox();
            outputArea = new JTextArea(8, 30);
            outputArea.setEditable(false);
            b.add(new JScrollPane(outputArea));

            outputArea.append("Welcome to the Chat Group, " + myName + "\n");
            inputField = new JTextField("");
            inputField.addActionListener(this);

            prompt = new JLabel("Type your messages below:");
            Container c = getContentPane();

            c.add(b, BorderLayout.NORTH);
            c.add(prompt, BorderLayout.CENTER);
            c.add(inputField, BorderLayout.SOUTH);

            Thread outputThread = new Thread(this);  
            outputThread.start();                    

            addWindowListener(
                    new WindowAdapter(){
                        public void windowClosing(WindowEvent e){
                            try{
                                myWriter.writeObject(cipher.encode("CLIENT CLOSING"));
                                myWriter.flush();
                            } catch (IOException io){
                                System.out.println("Problem closing client!");
                            }
                            System.exit(0);
                        }
                    }
            );
            setSize(500, 200);
            setVisible(true);
        } catch (Exception e){
            System.out.println("Problem starting client!");
        }
    }
    public void run(){
        while (true){
            try{
                //read encrypted message, decode and write to output area
                byte[] eMsg = (byte[]) myReader.readObject();
                String msg = cipher.decode(eMsg);
                outputArea.append(msg + "\n");

                //decryption data 
                byte[] bytes = msg.getBytes(); 
                System.out.println("Recieved array of bytes: " + Arrays.toString(eMsg));
                System.out.println("Decrypted array of bytes: " + Arrays.toString(bytes));
                System.out.println("Corresponding string: " + msg);

            } catch (Exception e){
                System.out.println(e + ", closing client!");
                break;
            }
        }
        System.exit(0);
    }

    public void actionPerformed(ActionEvent e){

        String currMsg = e.getActionCommand();    
        inputField.setText("");

        try{
            //add name + encode message
            currMsg = myName + ": " + currMsg;
            byte[] eMsg = cipher.encode(currMsg);
            myWriter.writeObject(eMsg);
            //avoid deadlock 
            myWriter.flush();
            byte[] bytes = currMsg.getBytes(); 

            //encryption data
            System.out.println("Original String Message: " + currMsg);
            System.out.println("Array of bytes: " + Arrays.toString(bytes));
            System.out.println("Encrypted array of bytes: " + Arrays.toString(eMsg));

        } catch (IOException io){
            System.err.println("Error: Failed to send message to server!");
        }
    }

    public static void main(String[] args){
        //create the new client and set the defualt close operation
        SecureChatClient JR = new SecureChatClient();
        JR.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    }
}
