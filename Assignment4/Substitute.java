import java.util.*;

public class Substitute implements SymCipher {

    private byte[] keys;
    private byte[] decodedKeys;
    
    public Substitute() {

        //create random 256 byte array and shuffle to randomize
        ArrayList<Byte> randomBytes = new ArrayList<Byte>();
        keys = new byte[256];
        decodedKeys = new byte[256];

        for (int i = 0; i < 256; i++){
            randomBytes.add((byte) i);
        }

        Collections.shuffle(randomBytes);

        //create key arrays
        for (int i = 0; i < 256; i ++){
            keys[i] = randomBytes.get(i);
            decodedKeys[keys[i] & 0xFF] = (byte) i;
        }
    }
    public Substitute(byte [] bytes){
        if (bytes.length != 256){
            throw new IllegalArgumentException("input byte stream != 256");
        }

        //set key array to byte array
        this.keys = bytes.clone(); 
        decodedKeys = new byte[256];

        //decode bytes
        for(int i = 0; i < 256; i ++){
            decodedKeys[bytes[i] & 0xFF] = (byte) i;
        }
    }

    public byte [] getKey(){
        return keys; 

    }
    
    public byte [] encode(String input){

        //get string in byte form and make coded byte array
        byte[] byteStream = input.getBytes();
        byte[] codedStream = new byte[input.length()];

        //encoded key set
        for (int i = 0; i < byteStream.length; i ++){
            codedStream[i] = (keys[byteStream[i] & 0xFF]);
        }

        return codedStream; 
    }
    public String decode(byte [] bytes){

        //byte array
        byte[] decodedStream = new byte[bytes.length];

        //decode bytes
        for (int i = 0; i < bytes.length; i++){
            decodedStream[i] = (decodedKeys[bytes[i] & 0xFF]);
        }

        String decodedMsg = new String(decodedStream);
        return decodedMsg;

    }

}  