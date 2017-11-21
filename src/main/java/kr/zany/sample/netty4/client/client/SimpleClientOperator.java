package kr.zany.sample.netty4.client.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import kr.zany.sample.netty4.client.common.data.ArgumentVo;
import kr.zany.sample.netty4.client.common.data.SocketThreadInfoVo;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.concurrent.Future;

/**
 * <p><b>Class Description</b></p>
 * <p>Copyright ⓒ 2016 kt corp. All rights reserved.</p>
 *
 * @author Lee Sang-Hyun (zanylove@gmail.com)
 * @since 2016-01-04 18:32
 */
@Data
@Slf4j
@Named
public class SimpleClientOperator {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Inject Beans
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private final ArgumentVo argumentVo;

    private final Provider<SimpleClientHandler> clientHandlerProvider;



    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Member Variables
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.NONE)
    private SocketThreadInfoVo socketThreadInfoVo;


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constructor
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Inject
    public SimpleClientOperator(
            ArgumentVo argumentVo,
            Provider<SimpleClientHandler> clientHandlerProvider
    ) {
        this.argumentVo = argumentVo;
        this.clientHandlerProvider = clientHandlerProvider;
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Public Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Async
    public Future<SocketThreadInfoVo> createClient(final SocketThreadInfoVo socketThreadInfoVo) {

        EventLoopGroup worker = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        try {

            // bootstrap.channel(OioSocketChannel.class);

            bootstrap.group(worker);
            bootstrap.channel(NioSocketChannel.class);

            if (argumentVo.getConnectionTimeoutMillis() > 0) {
                bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, argumentVo.getConnectionTimeoutMillis());
            }

            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {

                    SimpleClientHandler clientHandler = clientHandlerProvider.get();
                    clientHandler.setSocketThreadInfoVo(socketThreadInfoVo);

                    ch.pipeline().addLast(clientHandler);
                }
            });

            ChannelFuture future = bootstrap.connect(argumentVo.getHost(), argumentVo.getPort()).sync();
            future.channel().closeFuture().sync();

        } catch (Exception e) {
            log.error(String.format("Client creation error : %s", e.toString()));
        } finally {
            worker.shutdownGracefully();
            log.debug(String.format("Shutdown client - %s %s", socketThreadInfoVo.getThreadId(), socketThreadInfoVo.getLoopId()));
        }

        return new AsyncResult<>(socketThreadInfoVo);
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Protected, Private Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Override Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

}
