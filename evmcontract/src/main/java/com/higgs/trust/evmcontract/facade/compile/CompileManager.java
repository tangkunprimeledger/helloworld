package com.higgs.trust.evmcontract.facade.compile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.higgs.trust.evmcontract.crypto.HashUtil;
import com.higgs.trust.evmcontract.db.ByteArrayWrapper;
import com.higgs.trust.evmcontract.solidity.compiler.CompilationResult;
import com.higgs.trust.evmcontract.solidity.compiler.SolidityCompiler;
import com.higgs.trust.evmcontract.solidity.compiler.SolidityCompiler.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Chen Jiawei
 * @date 2018-11-29
 */
public class CompileManager {
    private static Cache<String, Entity> cacheWithFilepath = CacheBuilder.newBuilder().maximumSize(5000).build();
    private static Cache<ByteArrayWrapper, Entity> cacheWithFingerprint = CacheBuilder.newBuilder().maximumSize(5000).build();

    private CompileManager() {
    }

    private static class Entity {
        private ByteArrayWrapper fingerprint;
        private byte[] bytes;
        private CompilationResult compilationResult;

        Entity(ByteArrayWrapper fingerprint, byte[] bytes, CompilationResult compilationResult) {
            this.fingerprint = fingerprint;
            this.bytes = bytes;
            this.compilationResult = compilationResult;
        }
    }

    /**
     * Gets the compilation result by the file path.
     *
     * @param filePath the file path
     * @return the compilation result
     */
    public static CompilationResult getCompilationResultByFile(String filePath) throws IOException {
        Entity entity = cacheWithFilepath.getIfPresent(filePath);
        if (entity != null) {
            return entity.compilationResult;
        }

        entity = getEntityByBytes(read(new File(filePath)), cacheWithFingerprint);
        if (entity != null) {
            cacheWithFingerprint.put(entity.fingerprint, entity);
            cacheWithFilepath.put(filePath, entity);

            return entity.compilationResult;
        }

        return null;
    }

    /**
     * Gets the compilation result by the bytes of source content.
     *
     * @param bytes the bytes of source content
     * @return the compilation result
     */
    public static CompilationResult getCompilationResultByBytes(byte[] bytes) throws IOException {
        Entity entity = getEntityByBytes(bytes, cacheWithFingerprint);
        if (entity != null) {
            cacheWithFingerprint.put(entity.fingerprint, entity);

            return entity.compilationResult;
        }

        return null;
    }

    private static Entity getEntityByBytes(
            final byte[] bytes, final Cache<ByteArrayWrapper, Entity> cacheWithFingerprint) throws IOException {
        byte[] sha3 = sha3(bytes);
        ByteArrayWrapper fingerprint = new ByteArrayWrapper(sha3);

        Entity entity = cacheWithFingerprint.getIfPresent(fingerprint);
        if (entity != null) {
            if (!Arrays.equals(bytes, entity.bytes)) {
                throw new IOException(" Fingerprint collision happens");
            }

            return entity;
        }

        SolidityCompiler.Result compileResult =
                SolidityCompiler.compile(bytes, true, Options.ABI, Options.BIN, Options.INTERFACE, Options.METADATA);
        if (compileResult.isFailed()) {
            throw new IOException(compileResult.errors);
        }

        CompilationResult compilationResult = CompilationResult.parse(compileResult.output);
        return new Entity(fingerprint, bytes, compilationResult);
    }

    private static byte[] sha3(byte[] bytes) {
        return HashUtil.sha3(bytes);
    }

    private static byte[] read(File file) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);

            int available = inputStream.available();
            byte[] buffer = new byte[available];
            int read = inputStream.read(buffer);
            if (read != available) {
                throw new IOException("Reading is not complete");
            }

            return buffer;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
