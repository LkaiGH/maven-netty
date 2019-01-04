package com.open.coinnews.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

@Component
public class WSServer {


    private static class SingletionWSServer{
        static final WSServer instance = new WSServer();
    }

    public static WSServer getInstance(){

        return SingletionWSServer.instance;
    }

    private EventLoopGroup mainGroup;

    private EventLoopGroup subGroup;

    private  ServerBootstrap server;

    private  ChannelFuture future;

    public WSServer(){
         mainGroup = new NioEventLoopGroup();
         subGroup = new NioEventLoopGroup();
        server = new ServerBootstrap();
        server.group(mainGroup,subGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WSServerInitialzer());
        System.out.println("初始化-------------------------------------------");
    }

    public void start(){

        try {
            this.future= server.bind(8081);

            System.err.println("netty server 启动完成-------");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
