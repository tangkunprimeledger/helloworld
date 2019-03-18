package com.higgs.trust.zkproof;

import java.io.*;

class SerializerUtil {

    static String serialize(Object o) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        byte[] buf = baos.toByteArray();
        oos.flush();
        return Base58.encode(buf);
    }

    static Object deserialize(String bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(Base58.decode(bytes));
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }


}

