import java.util.*;

public class Add128 implements SymCipher {
    
    private byte[] keys; 
    
    public Add128() {
        //128 byte additive key
        keys = new byte[128];
        Random R = new Random();
        //place into byte array
        R.nextBytes(keys);
   }

    public Add128(byte[] bytes) {  

        //byte array used as key
		if(bytes.length != 128){ 
            throw new IllegalArgumentException("input byte stream != 128");
        }
        keys = new byte[128]; 
        this.keys = bytes.clone(); 
	}
   
    public byte[] getKey() {
        return keys.clone();
    }

    public byte[] encode(String input) {
        //convert string to bytes
        byte[] byteStream = input.getBytes(); 

       for(int i = 0; i < byteStream.length; i++){  
            //add the byte of the key and use mod to cycle through key index
            byteStream[i] =  (byte) (byteStream[i] + keys[i % keys.length]);
       }
       return byteStream.clone();
    }

   
    public String decode(byte[] bytes) {

        byte[] byteStream = bytes.clone(); 

        for(int i = 0; i < byteStream.length; i++){
            //remove the key 
            byteStream[i] = (byte) (byteStream[i] - keys[i % keys.length]);
        }
        return new String(byteStream);
    }
}