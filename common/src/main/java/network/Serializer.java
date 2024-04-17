package network;

import java.io.*;

public class Serializer {
    public static byte[] prepareData(Serializable s){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(s);

            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object deserializeData(byte[] buffer){
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
            ObjectInputStream ois = new ObjectInputStream(bis);

            return ois.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
