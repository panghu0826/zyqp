package com.buding.common.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class Netty4CodecOld extends ByteToMessageCodec<byte[]> {
//    private static final int BASE_LENTH = 4+8+4;
    private static final int BASE_LENTH = 4+4;
    private Logger logger = LogManager.getLogger(getClass());

	private int HEAD_LENGTH = 4;

	public Netty4CodecOld() {

	}

	public Netty4CodecOld(int packLen) {
		HEAD_LENGTH = packLen;
	}

    /**
     * 解码
     *  长度       | 编号       |   数据     |
     *  int(4)    | long(8)    |   byte[](4)
     * @param ctx
     * @param in
     * @param out
     */
	public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
//		logger.info("netmsg reach");
        if (in.readableBytes() < HEAD_LENGTH) {  //这个HEAD_LENGTH是我们用于表示头长度的字节数。  由于上面我们传的是一个int类型的值，所以这里HEAD_LENGTH的值为4.
            return;
        }
        in.markReaderIndex();                  //我们标记一下当前的readIndex的位置
        int dataLength = in.readInt();       // 读取传送过来的消息的长度。ByteBuf 的readInt()方法会让他的readIndex增加4
//        logger.info("packet len : " + dataLength);
        if (dataLength < 0) { // 我们读到的消息体长度为0，这是不应该出现的情况，这里出现这情况，关闭连接。
            ctx.close();
        }

        if (in.readableBytes() < dataLength - 4) { //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
            in.resetReaderIndex();
            return;
        }

        byte[] body = new byte[dataLength-4];  //  嗯，这时候，我们读到的长度，满足我们的要求了，把传送过来的数据，取出来吧~~
        try {
            in.readBytes(body);
            byte[] realBody =  DesUtil.decrypt(body);
            out.add(realBody);
        } catch (Exception e) {
            e.printStackTrace();
            if(ctx.channel().isOpen()) {
                ctx.channel().close();
            }
            return;
        }



//        while(true){
//            if(in.readableBytes() >= BASE_LENTH){
//                //第一个可读数据包的起始位置
////                int beginIndex;
////
////                while(true) {
////                    //包头开始游标点
////                    beginIndex = in.readerIndex();
////                    //标记初始读游标位置
//                    in.markReaderIndex();
////                    if (in.readInt() == ConstantValue.HEADER_FLAG) {
////                        break;
////                    }
////                    //未读到包头标识略过一个字节
////                    in.resetReaderIndex();
////                    in.readByte();
//
//                    //不满足
//                    if(in.readableBytes() < BASE_LENTH){
//                        return ;
//                    }
////                }
//                //读取模块号命令号
////                short module = in.readShort();
////                short cmd = in.readShort();
////
////                int stateCode = in.readInt();
//
//                //读取数据长度
//                int lenth = in.readInt();
//                if(lenth < 0 ){
//                    ctx.channel().close();
//                }
//
//                //读取数据编号
////                long bianhao = in.readLong();
////                if(bianhao<0 ){
////                    ctx.channel().close();
////                }
//
//                //读取
//
//                //数据包还没到齐
//                if(in.readableBytes() < lenth-4){
////                    in.readerIndex(beginIndex);
//                    in.resetReaderIndex();
//                    return ;
//                }
//
//                //读数据部分
//                byte[] data = new byte[lenth];
//                in.readBytes(data);
//
//                //解析出消息对象，继续往下面的handler传递
//                out.add(data);
//            }else{
//                break;
//            }
//        }
//        //数据不完整，等待完整的数据包
//        return ;

    }

    /**
     * 编码
     * @param arg0
     * @param body
     * @param out
     * @throws Exception
     */
	@Override
	protected void encode(ChannelHandlerContext arg0, byte[] body, ByteBuf out)
			throws Exception {
        byte[] encryptBody =  DesUtil.encrypt(body);
        int dataLength = encryptBody.length;  //读取消息的长度
        out.writeInt(dataLength+4);  //先将消息长度写入，也就是消息头
        out.writeBytes(encryptBody);
	}

}
