package com.im.server.msg.handler;

import com.im.server.msg.pojo.MyDataInfo1;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * description: MsgHandler <br>
 * date: 2020/6/11 17:56 <br>
 * author: sunfei <br>
 * version: 1.0 <br>
 */
@Slf4j
public class MsgHandler extends SimpleChannelInboundHandler <MyDataInfo1.MyMessage> {
//    绑定channelId与userId
    static Map channelmap = new HashMap();

    static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MyDataInfo1.MyMessage myMessage) throws Exception {
        Channel channel = channelHandlerContext.channel();
        MyDataInfo1.MyMessage.DataType dataType = myMessage.getDataType();

        String channelId = channelHandlerContext.channel().id().asShortText();


        if (dataType == MyDataInfo1.MyMessage.DataType.oneType) {
            log.info("****************************************进入点对点会话****************************************");
//            为one to one会话
            MyDataInfo1.OneSession oneSession = myMessage.getOneSession();

            channelmap.put(oneSession.getSourceId(),channelHandlerContext);
            Integer targetId = oneSession.getTargetId();

            ChannelHandlerContext c = (ChannelHandlerContext) channelmap.get(targetId);
            if(c==null){

//                为空，离线状态，存储
                String h = targetId+"<-"+oneSession.getSourceId();
                System.out.println(h+"========================="+oneSession.getMsg());
            }else {
                MyDataInfo1.OneSession build = MyDataInfo1.OneSession.newBuilder()
                        .setMsg(myMessage.getOneSession().getMsg())
                        .setSourceId(myMessage.getOneSession().getSourceId())
                        .setMsgStatus(1)
                        .setTargetId(myMessage.getOneSession().getTargetId())
                        .setName(myMessage.getOneSession().getName())
                        .setTimestamp(Long.toString((new Date()).getTime())).build();
                MyDataInfo1.MyMessage.newBuilder().setDataType(MyDataInfo1.MyMessage.DataType.oneType).setOneSession(build);
                c.channel().writeAndFlush(myMessage);
            }
        } else if (dataType == MyDataInfo1.MyMessage.DataType.groupTypeout) {
//            为group会话
            MyDataInfo1.GroupSession groupSession = myMessage.getGroupSession();
            channelmap.put(groupSession.getSourceId(),channelId);
        } else {
            log.error("************************************没有指定消息类型************************************");
        }


    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String s = ctx.channel().id().asShortText();
        log.warn(s+":激活后读取离线信息*******************************");
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        log.info("************************************chanel注册加入chanelgroup************************************");
        channels.add(ctx.channel());
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error(cause.getMessage());
    }


}

