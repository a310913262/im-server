package com.im.server.msg;

import com.im.server.msg.handler.MsgHandler;
import com.im.server.msg.pojo.MyDataInfo1;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * description: EchoServer <br>
 * date: 2020/6/10 16:21 <br>
 * author: sunfei <br>
 * version: 1.0 <br>
 */
@Component
@Slf4j
public class EchoServer implements ApplicationRunner {
    /**
     * 事件循环组，就是死循环
     */
    //仅仅接受连接，转给workerGroup，自己不做处理
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    //真正处理
    EventLoopGroup workerGroup = new NioEventLoopGroup();


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("*************************************MSG NETTY START*************************************");

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new TestServerInitializer());

            //绑定一个端口并且同步，生成一个ChannelFuture对象
            ChannelFuture channelFuture = serverBootstrap.bind(8899).sync();
            //对关闭的监听
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            log.info("*************************************MSG NETTY STOP*************************************");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

class TestServerInitializer extends ChannelInitializer <SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new ProtobufDecoder(MyDataInfo1.MyMessage.getDefaultInstance()));
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast(new ProtobufEncoder());
        pipeline.addLast(new MsgHandler());
    }
}
