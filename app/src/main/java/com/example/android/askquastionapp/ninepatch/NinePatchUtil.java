package com.example.android.askquastionapp.ninepatch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NinePatchUtil {

    public static ByteBuffer getByteBufferFixed(int left, int top, int right, int bottom) {
        int NO_COLOR = 0x00000001;
        //Docs check the NinePatchChunkFile
        ByteBuffer buffer = ByteBuffer.allocate(84).order(ByteOrder.nativeOrder());
        //was translated
        buffer.put((byte) 0x01);
        //divx size
        buffer.put((byte) 0x02);
        //divy size
        buffer.put((byte) 0x02);
        //color size
        buffer.put((byte) 0x09);

        //skip
        buffer.putInt(0);
        buffer.putInt(0);

        //padding
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);

        //skip 4 bytes
        buffer.putInt(0);

        buffer.putInt(left);
        buffer.putInt(right);
        buffer.putInt(top);
        buffer.putInt(bottom);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        return buffer;
    }

    /**
     * byteBuffer 转 byte数组
     *
     * @param buffer
     * @return
     */
    public static byte[] bytebuffer2ByteArray(ByteBuffer buffer) {
        //重置 limit 和postion 值
        buffer.flip();
        //获取buffer中有效大小
        int len = buffer.limit() - buffer.position();
        byte[] bytes = new byte[len];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = buffer.get();
        }
        return bytes;
    }
}
