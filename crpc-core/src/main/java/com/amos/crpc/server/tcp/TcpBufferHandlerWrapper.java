package com.amos.crpc.server.tcp;

import com.amos.crpc.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * 装饰者模式（使用recordParser）对原有的buffer处理能力进行增强
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {
    private final RecordParser recordParser;

    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        recordParser = initRecordParser(bufferHandler);
    }

    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        // 构造parser
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);
        parser.setOutput(new Handler<Buffer>() {
            // 初始化
            int size = -1;
            // 一次完整的读取（头 + 体）
            Buffer resultbuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if (-1 == size) {
                    // 读取消息体长度
                    size = buffer.getInt(13);
                    parser.fixedSizeMode(size);
                    // 写入头信息到结果
                    resultbuffer.appendBuffer(buffer);
                }else{
                    // 读取消息体
                    resultbuffer.appendBuffer(buffer);
                    // 读取完毕，已拼接为完整Buffer，执行处理
                    bufferHandler.handle(resultbuffer);  // 处理, 交给上层
                    // 重置
                    size = -1;
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    resultbuffer = Buffer.buffer();
                }
            }
        });
        return parser;
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }
}
