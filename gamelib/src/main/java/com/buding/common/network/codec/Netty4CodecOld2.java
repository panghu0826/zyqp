package com.buding.common.network.codec;

import com.buding.common.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Netty4CodecOld2 extends ByteToMessageCodec<byte[]> {
//    private static final int BASE_LENTH = 4+8+4;
    private static final int BASE_LENTH = 4+4;
    private Logger logger = LogManager.getLogger(getClass());

	private int HEAD_LENGTH = 4;

	private int KEY_LENGTH = 8;

	public Netty4CodecOld2() {

	}

	public Netty4CodecOld2(int packLen) {
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
//		logger.info("netmsg reach");
        if (in.readableBytes() < HEAD_LENGTH + KEY_LENGTH) {  //这个HEAD_LENGTH是我们用于表示头长度的字节数。  由于上面我们传的是一个int类型的值，所以这里HEAD_LENGTH的值为4.
            return;
        }
        in.markReaderIndex();                  //我们标记一下当前的readIndex的位置
        int dataLength = in.readInt();       // 读取传送过来的消息的长度。ByteBuf 的readInt()方法会让他的readIndex增加4
//        logger.info("packet len : " + dataLength);
        if (dataLength < 0) { // 我们读到的消息体长度为0，这是不应该出现的情况，这里出现这情况，关闭连接。
//            logger.info("22222222222222");
            ctx.close();
        }

        if (in.readableBytes() < dataLength - HEAD_LENGTH) { //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
            in.resetReaderIndex();
            return;
        }

        long key = in.readLong();
        if (key < 0) { // 我们读到的key长度为0，这是不应该出现的情况，这里出现这情况，关闭连接。
//            logger.info("33333333333333");
            ctx.close();
        }

        if (in.readableBytes() < dataLength - HEAD_LENGTH - KEY_LENGTH) { //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
            in.resetReaderIndex();
            return;
        }

        byte[] body = new byte[dataLength - HEAD_LENGTH - KEY_LENGTH];  //  嗯，这时候，我们读到的长度，满足我们的要求了，把传送过来的数据，取出来吧~~
        try {
            in.readBytes(body);
//            logger.info("key"+key);
//            logger.info("secretKeyMap"+SecretKeyManager.secretKeyMap);
//            Secretkey model = SecretKeyManager.secretKeyMap.get(key);
            byte[] realBody =  DesUtil.decrypt(body, "88888888");
            out.add(realBody);
        } catch (Exception e) {
            if(ctx.channel().isOpen()) {
//                logger.info("44444444444444");
                ctx.channel().close();
            }
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
	protected void encode(ChannelHandlerContext arg0, byte[] body, ByteBuf out) throws Exception {
        byte[] encryptBody =  DesUtil.encrypt(body,"88888888");
        int dataLength = encryptBody.length;  //读取消息的长度
        out.writeInt(dataLength + HEAD_LENGTH + KEY_LENGTH);  //先将消息长度写入，也就是消息头
        out.writeLong(9L);  //再将key写入
        out.writeBytes(encryptBody);
	}
}
