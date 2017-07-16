/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.whg.websocket.server;

import org.springframework.context.ApplicationContext;

import com.whg.websocket.server.handler.WebSocketFrameHandler;
import com.whg.websocket.server.handler.WebSocketIndexPageHandler;
import com.whg.websocket.server.handler.WebSocketJsonEncoder;
import com.whg.websocket.server.handler.WebSocketProtobufEncoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/websocket";

    private final SslContext sslCtx;
    
    private final WebSocketProtobufEncoder wsProtobufEncoder;
    private final WebSocketJsonEncoder wsJsonEncoder;
    private final WebSocketIndexPageHandler wsIndexHandler;
    private final WebSocketFrameHandler wsFrameHandler;

    public WebSocketServerInitializer(SslContext sslCtx, ApplicationContext ac) {
        this.sslCtx = sslCtx;
        this.wsProtobufEncoder = new WebSocketProtobufEncoder();
        this.wsJsonEncoder = new WebSocketJsonEncoder();
        this.wsIndexHandler = new WebSocketIndexPageHandler(WEBSOCKET_PATH);
        this.wsFrameHandler = new WebSocketFrameHandler(ac);
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
        
        //必须把encoder的添加放在handler的前面，否则使用ctx.write的话就找不到encoder了，
        //只能使用channle.write从尾部一直找到头部，方可找到encoder
        pipeline.addLast("protobufEncoder", wsProtobufEncoder);
        pipeline.addLast("jsonEncoder", wsJsonEncoder);
        
        pipeline.addLast("index", wsIndexHandler);
        pipeline.addLast("handler", wsFrameHandler);
        
    }
}
