package com.higgs.trust.network.utils;

import com.caucho.hessian.io.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author duhongming
 * @date 2018/9/5
 */
public final class Hessian {

    static {
        SerializerFactory serializerFactory = SerializerFactory.createDefault();
        ExtSerializerFactory extFactory = new ExtSerializerFactory();
        serializerFactory.addFactory(extFactory);
        extFactory.addSerializer(BigDecimal.class, new StringValueSerializer());
        extFactory.addDeserializer(BigDecimal.class, new BigDecimalDeserializer());
    }

    public static byte[] serialize(Object obj) {
        try {
            Deflation envelope = new Deflation();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Hessian2Output out = new Hessian2Output(bos);
            // 压缩
//            out = envelope.wrap(out);

            out.startMessage();
            out.writeObject(obj);
            out.writeListEnd();
            out.completeMessage();
            out.close();
            byte[] data = bos.toByteArray();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T parse(byte[] data) {
        Deflation envelope = new Deflation();
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Hessian2Input in = new Hessian2Input(bin);

        try {
//            in = envelope.unwrap(in);
            in.startMessage();
            Object obj = in.readObject();
            in.completeMessage();
            return (T)obj;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                bin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Object clone(Object obj) {
        byte[] data = serialize(obj);
        return parse(data);
    }
}
