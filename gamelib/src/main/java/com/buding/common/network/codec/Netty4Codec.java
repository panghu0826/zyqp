package com.buding.common.network.codec;

import com.buding.common.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Netty4Codec extends ByteToMessageCodec<Message> {
    private Logger logger = LogManager.getLogger(getClass());

	private int HEAD_LENGTH = 4;

    public Netty4Codec() {

    }

	public Netty4Codec(int packLen) {
		HEAD_LENGTH = packLen;
	}


    /**
     * 解码
     *  长度       | secretKey       |   数据     |
     *  int(4)    | long(8)    |   byte[](4)
     * @param ctx
     * @param in
     * @param out
     */
	public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < HEAD_LENGTH) return;

        //标记一下当前的readIndex的位置
        in.markReaderIndex();

        //读取传送过来的消息的长度。ByteBuf 的readInt()方法会让他的readIndex增加4
        int dataLength = in.readInt();

        //消息体长度为0,关闭连接。
        if (dataLength < 0) ctx.close();

        //读到的消息体长度小于传送过来的消息长度，则resetReaderIndex.配合markReaderIndex使用。把readIndex重置到mark的地方
        if (in.readableBytes() < dataLength - HEAD_LENGTH) {
            in.resetReaderIndex();
            return;
        }

        byte[] body = new byte[dataLength - HEAD_LENGTH];
        try {
            in.readBytes(body);
            Message message = new Message(body);
            out.add(message);
        } catch (Exception e) {
            e.printStackTrace();
            if(ctx.channel().isOpen()) ctx.channel().close();
        }
    }

    /**
     * 编码
     *  长度       | secretKey       |   数据     |
     *  int(4)    | long(8)    |   byte[](4)
     * @param arg0
     * @param body
     * @param out
     * @throws Exception
     */
	@Override
	protected void encode(ChannelHandlerContext arg0, Message body, ByteBuf out) throws Exception {
        int dataLength = body.getData().length;  //读取消息的长度
        out.writeInt(dataLength + HEAD_LENGTH);  //先将消息长度写入，也就是消息头
        out.writeBytes(body.getData());
	}
}
