//1501 Assignment 3
//Ava Chong

import java.util.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.Math.*;

public class LZWmod {
    private static final int R = 256;          // number of input chars
    private static int W;                      // codeword width
    private static int L;                      // number of codewords = 2^W
    private static int max = 16;                

    //private static int reset = 1;

    public static void compress(int flag) throws FileNotFoundException{
        int skip = 0;
        PrintWriter pw = new PrintWriter("debug.txt"); 
        //try {
        
        StringBuilder stringIn = new StringBuilder();
        char C = BinaryStdIn.readChar();
        W = 9;
        L = (int)Math.pow(2, W);    //number of code words

        //reset dictionary flag
        BinaryStdOut.write(flag, W);

        //load the single chars into the dictionary
        TSTmod<Integer> dictionary = new TSTmod<Integer>();
   
        for (int i = 0; i < R; i++){
            stringIn.append((char) i);
            dictionary.put(stringIn, i);
            stringIn.deleteCharAt(0);
        }
        int code = R+1;  // R is codeword for EOF

        while(!BinaryStdIn.isEmpty()){
            stringIn.append(C);

            //search for the largest string in the dictionary
            while (dictionary.contains(stringIn) && !BinaryStdIn.isEmpty()){
                C = BinaryStdIn.readChar();   
                stringIn.append(C);
            } 

            //add the codeword to the dictionary if there is space
            if(code < L){
                dictionary.put(stringIn, code++); //increment codeword count
                pw.write("ADD String in: " + stringIn + ": " + (code-1) + "\n");
            } else if (W != max) {
                //else increase the codeword width and then add the value
                if(W < max){
                    W++;
                    L = (int)Math.pow(2, W);
                    dictionary.put(stringIn, code++);
                    pw.write("ADD String in: " + stringIn + ": " + (code-1) + "\n");
                } 
            } else if (code == L - 1 && W == max && flag == 1) {
                    //reset dictionary 
                    dictionary.put(stringIn, code++); //increment codeword count
                    stringIn.deleteCharAt(stringIn.length()-1);
                    BinaryStdOut.write(dictionary.get(stringIn), W);
                    stringIn.append(C);

                    StringBuilder blank2 = new StringBuilder();
                    TSTmod<Integer> dictionary2 = new TSTmod<Integer>();

                    for (int i = 0; i < R; i++){
                        dictionary2.put(blank2.append((char)i), i);
                        blank2.deleteCharAt(0);
                    }
                    dictionary = dictionary2; 
                    W = 9;
                    L = 512;
                    code = R + 1;
                    System.err.println("Reset ");
                    pw.write("Reset\n");
                    skip = 1;
                }
                if (!BinaryStdIn.isEmpty()){
                    stringIn.deleteCharAt(stringIn.length()-1); 
                }
                if (skip == 1){
                    skip = 0;
                } else {
                    BinaryStdOut.write(dictionary.get(stringIn), W); 
                    skip = 0;
                }
            
                stringIn.delete(0, stringIn.length());

        }
        //compress last char 
        BinaryStdOut.write(R, W); //write the end of file byte
        BinaryStdOut.close();
        //} catch(Exception e) {}
        //finally {pw.close();}
    } 

    public static void expand() throws FileNotFoundException{
        PrintWriter pw = new PrintWriter("debugExpand.txt"); 
        W = 9;
        L = (int)Math.pow(2, W);
        String[] dictionary = new String[L];
        int i;

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++) {
            dictionary[i] = "" + (char) i;
        }
        dictionary[i++] = "";                        // (unused) lookahead for EOF

        int flag = BinaryStdIn.readInt(W);
        System.err.println("flag: "+flag);

        int codeword = BinaryStdIn.readInt(W);
        String val = dictionary[codeword];
        System.err.println(codeword);
        System.err.println("val = " + val);

        while (val != null && true) {
            BinaryStdOut.write(val);

            if (i == L && W == max && flag == 1){
                //reset dictionary
                W = 9;
                L = (int)Math.pow(2, W);
                String[] dictionary2 = new String[L];

                for (i = 0; i < R; i++) {
                    dictionary2[i] = "" + (char) i;
                }
                dictionary2[i++] = "";
                dictionary = dictionary2;          
            }

            codeword = BinaryStdIn.readInt(W); //read next codeword
            pw.write( codeword +"\n");
            if (codeword == R){
                break; //end of file breaker
            }
            //look up codeword in symbol table
            String s = dictionary[codeword];

            //if codeword isnt in the dictionary yet, its the value plus the first char of value
            if (i == codeword){
                s = val + val.charAt(0);   // special case hack
            }
            //add the next thing to symbol table
            if (i < L - 1 && s != null) {
                //System.out.printf("val = " + val +"\ns = " + s + "\n");
                dictionary[i++] = val + s.charAt(0);
                pw.write("ADD:" + val + s.charAt(0) +": "+ (i-1) +"\n");
            }
            //if(i >= L - 1 && W != max)
            if(i == L - 1){
                //account for codeword increase
                if (W != max){
                    W++;
                    System.err.println("W: "+W);
                    L = (int)Math.pow(2, W);
                    //increase the array size 
                    dictionary = Arrays.copyOf(dictionary, L);
                }
            }
                /*
                else if (W == max && flag == 1){
                    //reset dictionary
                    String[] dictionary2 = new String[L];
                    // initialize symbol table with all 1-character strings
                    for (i = 0; i < R;  i++) {
                        dictionary2[i] = "" + (char) i;
                    }
                    dictionary2[i++] = "";                        // (unused) lookahead for EOF
                    dictionary = dictionary2;
                    W = 9;
                    L = 512;
                    System.err.println("Reset\n");
                    pw.write("Reset\n");
                }
                /*
                String[] temp = new String[L];
                System.arraycopy(dictionary, 0, temp, 0, (int)Math.pow(2, W - 1));
                dictionary = temp;
                */
                val = s;
            }
            BinaryStdOut.close();
        }
    public static void main(String[] args) throws java.io.FileNotFoundException{
        if      (args[0].equals("-")) compress(0);
        else if (args[0].equals("+")) expand();
        else if (args[0].equals("-r")) compress(1);
        else if (args[0].equals("-n")) compress(0);
        else throw new RuntimeException("Illegal command line argument");
    }

}



