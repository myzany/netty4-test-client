package kr.zany.sample.netty4.client.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import kr.zany.sample.netty4.client.common.data.ArgumentVo;
import kr.zany.sample.netty4.client.common.data.SocketThreadInfoVo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * <p><b>Class Description</b></p>
 * <p>Copyright ⓒ 2016 kt corp. All rights reserved.</p>
 *
 * @author Lee Sang-Hyun (zanylove@gmail.com)
 * @since 2016-01-04 18:46
 */
@Slf4j
@Named
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SimpleClientHandler extends ChannelInboundHandlerAdapter {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Inject Beans
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private final ArgumentVo argumentVo;

    private final ThreadPoolTaskExecutor asyncExecutor;


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Member Variables
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.NONE)
    private SocketThreadInfoVo socketThreadInfoVo;

    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");



    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constructor
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Inject
    public SimpleClientHandler(
            ArgumentVo argumentVo,
            ThreadPoolTaskExecutor asyncExecutor
    ) {
        this.argumentVo = argumentVo;
        this.asyncExecutor = asyncExecutor;
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Public Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Protected, Private Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private void tryDisconnect(ChannelHandlerContext ctx) throws InterruptedException {
        if (argumentVo.isDisconnectAfterComplete()) {
            if (argumentVo.getDisconnectDelayMillis() > 0) {
                Thread.sleep(argumentVo.getDisconnectDelayMillis());
            }

            ctx.disconnect();
            ctx.channel().close();
            ctx.close();

            log.info(">>> Request socket close.");
        }
    }

    private void sendMessage(ChannelHandlerContext ctx, byte[] message) throws InterruptedException {
        if (argumentVo.getSendDelayMillis() > 0) {
            Thread.sleep(argumentVo.getSendDelayMillis());
        }

        final ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(message);

        final ChannelFuture channelFuture = ctx.writeAndFlush(buf);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                assert channelFuture == future;
                socketThreadInfoVo.setSentTime(System.currentTimeMillis() - socketThreadInfoVo.getStartTimestamp());
            }
        });
    }

    private String currentChannelStatus(ChannelHandlerContext ctx) {
        return String.format("registered:%s active:%s open:%s writable:%s", ctx.channel().isRegistered(), ctx.channel().isActive(), ctx.channel().isOpen(), ctx.channel().isWritable());
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Override Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * CONNECT     : channelRegistered -> channelActive ->
     * COMMUNICATE : [channelRead -> channelReadComplete] ->
     * DISCONNECT  : channelInactive -> channelUnregistered
     */

    /** active, inactive */

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        log.info(String.format(">>> [Inbound] channelActive - %s", currentChannelStatus(ctx)));
        socketThreadInfoVo.setActive(ctx.channel().isActive());
        sendMessage(ctx, argumentVo.getPayloadContents().get(socketThreadInfoVo.getMessageIndex()).getBytes());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info(String.format(">>> [Inbound] channelInactive - %s", currentChannelStatus(ctx)));
        socketThreadInfoVo.setActive(ctx.channel().isActive());
    }

    /** channel read */

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info(String.format(">>> [Inbound] channelRead - %s", currentChannelStatus(ctx)));
        ByteBuf in = (ByteBuf) msg;
        try {
            StringBuilder builder = new StringBuilder();
            while (in.isReadable()) {
                builder.append((char)in.readByte());
            }
            log.info(builder.toString());
        } finally {
            in.release();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info(String.format(">>> [Inbound] channelReadComplete - %s", currentChannelStatus(ctx)));
        socketThreadInfoVo.setReceivedTime(System.currentTimeMillis() - socketThreadInfoVo.getStartTimestamp());
        tryDisconnect(ctx);
    }

    /** channel register */

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info(String.format(">>> [Inbound] channelRegistered - %s", currentChannelStatus(ctx)));
        socketThreadInfoVo.setConnectedTime(System.currentTimeMillis() - socketThreadInfoVo.getStartTimestamp());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info(String.format(">>> [Inbound] channelUnregistered - %s", currentChannelStatus(ctx)));

        long currentTime = System.currentTimeMillis();

        socketThreadInfoVo.setEndTime(formatter.print(currentTime));
        socketThreadInfoVo.setEndTimestamp(currentTime);

        socketThreadInfoVo.setThreadPoolActive(asyncExecutor.getThreadPoolExecutor().getActiveCount());
        socketThreadInfoVo.setDisconnectedTime(System.currentTimeMillis() - socketThreadInfoVo.getStartTimestamp());

        System.out.println(String.format("%5s, %5s, %05d, [%3d/%3d] %s ~ %s, %s, %s, %s, %s [%d, %d, %d]",
                socketThreadInfoVo.getThreadId(),
                socketThreadInfoVo.getLoopId(),
                socketThreadInfoVo.getMessageIndex(),
                socketThreadInfoVo.getThreadPoolActive(),
                asyncExecutor.getThreadPoolExecutor().getCorePoolSize(),
                socketThreadInfoVo.getStartTime(),
                socketThreadInfoVo.getEndTime(),
                socketThreadInfoVo.getConnectedTime(),
                socketThreadInfoVo.getSentTime(),
                socketThreadInfoVo.getReceivedTime(),
                socketThreadInfoVo.getDisconnectedTime(),
                asyncExecutor.getThreadPoolExecutor().getTaskCount(),
                asyncExecutor.getThreadPoolExecutor().getMaximumPoolSize(),
                asyncExecutor.getThreadPoolExecutor().getQueue().remainingCapacity()
        ));
    }

    /** exception */

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        tryDisconnect(ctx);
    }
}
