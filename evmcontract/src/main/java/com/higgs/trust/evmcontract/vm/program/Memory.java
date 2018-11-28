/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package com.higgs.trust.evmcontract.vm.program;

import com.higgs.trust.evmcontract.util.ByteUtil;
import com.higgs.trust.evmcontract.vm.DataWord;
import com.higgs.trust.evmcontract.vm.program.listener.ProgramListener;
import com.higgs.trust.evmcontract.vm.program.listener.ProgramListenerAware;

import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.lang.String.format;

public class Memory implements ProgramListenerAware {

    private static final int CHUNK_SIZE = 1024;
    private static final int WORD_SIZE = 32;

    private List<byte[]> chunks = new LinkedList<>();
    private int softSize;
    private ProgramListener programListener;

    @Override
    public void setProgramListener(ProgramListener traceListener) {
        this.programListener = traceListener;
    }

    public byte[] read(int address, int size) {
        if (size <= 0) {
            return ByteUtil.EMPTY_BYTE_ARRAY;
        }

        extend(address, size);
        byte[] data = new byte[size];
        //块索引
        int chunkIndex = address / CHUNK_SIZE;
        //块偏移位
        int chunkOffset = address % CHUNK_SIZE;

        int toGrab = data.length;
        int start = 0;
        /**
         * toGrab为读取字节的长度，每次读取后将已读取长度减掉
         * chunkIndex每次读取 块索引+1
         * 读取块的偏移量为0
         */
        while (toGrab > 0) {

            int copied = grabMax(chunkIndex, chunkOffset, toGrab, data, start);

            // read next chunk from the start
            ++chunkIndex;
            chunkOffset = 0;

            // mark remind
            toGrab -= copied;
            //第二轮的拷贝的开始位置为开始拷贝的位置+已拷贝位置
            start += copied;
        }

        return data;
    }

    public void write(int address, byte[] data, int dataSize, boolean limited) {

        if (data.length < dataSize) {
            dataSize = data.length;
        }

        if (!limited) {
            extend(address, dataSize);
        }
        //表示属于
        int chunkIndex = address / CHUNK_SIZE;
        int chunkOffset = address % CHUNK_SIZE;

        int toCapture = 0;
        if (limited) {
            toCapture = (address + dataSize > softSize) ? softSize - address : dataSize;
        } else {
            toCapture = dataSize;
        }

        int start = 0;
        while (toCapture > 0) {
            int captured = captureMax(chunkIndex, chunkOffset, toCapture, data, start);

            // capture next chunk
            ++chunkIndex;
            chunkOffset = 0;

            // mark remind
            toCapture -= captured;
            start += captured;
        }

        if (programListener != null) {
            programListener.onMemoryWrite(address, data, dataSize);
        }
    }


    public void extendAndWrite(int address, int allocSize, byte[] data) {
        extend(address, allocSize);
        write(address, data, data.length, false);
    }

    public void extend(int address, int size) {

        if (size <= 0) {
            return;
        }

        final int newSize = address + size;

        int toAllocate = newSize - internalSize();
        //当前位置+读取长度>块已有长度 则进行扩容
        if (toAllocate > 0) {
            addChunks((int) ceil((double) toAllocate / CHUNK_SIZE));
        }
        //分配大小
        toAllocate = newSize - softSize;
        if (toAllocate > 0) {
            toAllocate = (int) ceil((double) toAllocate / WORD_SIZE) * WORD_SIZE;
            softSize += toAllocate;

            if (programListener != null) {
                programListener.onMemoryExtend(toAllocate);
            }
        }
    }

    //读取一个32个字节的word
    public DataWord readWord(int address) {
        return new DataWord(read(address, 32));
    }

    // just access expecting all data valid
    public byte readByte(int address) {

        int chunkIndex = address / CHUNK_SIZE;
        int chunkOffset = address % CHUNK_SIZE;

        byte[] chunk = chunks.get(chunkIndex);

        return chunk[chunkOffset];
    }

    @Override
    public String toString() {

        StringBuilder memoryData = new StringBuilder();
        StringBuilder firstLine = new StringBuilder();
        StringBuilder secondLine = new StringBuilder();

        for (int i = 0; i < softSize; ++i) {

            byte value = readByte(i);

            // Check if value is ASCII
            String character = ((byte) 0x20 <= value && value <= (byte) 0x7e) ? new String(new byte[]{value}) : "?";
            firstLine.append(character).append("");
            secondLine.append(ByteUtil.oneByteToHexString(value)).append(" ");

            if ((i + 1) % 8 == 0) {
                String tmp = format("%4s", Integer.toString(i - 7, 16)).replace(" ", "0");
                memoryData.append("").append(tmp).append(" ");
                memoryData.append(firstLine).append(" ");
                memoryData.append(secondLine);
                if (i + 1 < softSize) {
                    memoryData.append("\n");
                }
                firstLine.setLength(0);
                secondLine.setLength(0);
            }
        }

        return memoryData.toString();
    }

    public int size() {
        return softSize;
    }

    public int internalSize() {
        return chunks.size() * CHUNK_SIZE;
    }

    public List<byte[]> getChunks() {
        return new LinkedList<>(chunks);
    }

    private int captureMax(int chunkIndex, int chunkOffset, int size, byte[] src, int srcPos) {

        byte[] chunk = chunks.get(chunkIndex);
        int toCapture = min(size, chunk.length - chunkOffset);

        System.arraycopy(src, srcPos, chunk, chunkOffset, toCapture);
        return toCapture;
    }

    /**
     * @param chunkIndex
     * @param chunkOffset
     * @param size
     * @param dest
     * @param destPos
     * @return
     */
    private int grabMax(int chunkIndex, int chunkOffset, int size, byte[] dest, int destPos) {
        //chunkIndex 块索引
        byte[] chunk = chunks.get(chunkIndex);
        //拷贝的长度为=数组长度-数组拷贝的开始位置
        int toGrab = min(size, chunk.length - chunkOffset);
        /**
         * @param      src      原内存原字节数组.
         * @param      srcPos   原字节数组的开始位置
         * @param      dest     将内存原数组拷贝到新的字节数组
         * @param      destPos  目标数组的开始位置.
         * @param      length   拷贝长度.
         */
        System.arraycopy(chunk, chunkOffset, dest, destPos, toGrab);

        return toGrab;
    }

    /**
     * 给memory 扩容
     * 扩容按1024 byte为单位
     */
    private void addChunks(int num) {
        for (int i = 0; i < num; ++i) {
            chunks.add(new byte[CHUNK_SIZE]);
        }
    }
}
