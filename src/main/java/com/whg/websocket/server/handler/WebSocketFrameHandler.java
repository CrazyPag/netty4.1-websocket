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
package com.whg.websocket.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import com.whg.protobuf.TestProtobuf.ServiceMethodProto;
import com.whg.websocket.server.framework.Dispatcher;
import com.whg.websocket.server.framework.GlobalContext;
import com.whg.websocket.server.framework.Player;
import com.whg.websocket.server.framework.request.JsonRequest;
import com.whg.websocket.server.framework.request.Request;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

@Sharable
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);
    
    private final Dispatcher dispatcher;
    private final GlobalContext globalContext;
    
    public WebSocketFrameHandler(ApplicationContext ac) {
		dispatcher = new Dispatcher(ac);
		globalContext = (GlobalContext)ac.getBean("globalContext");
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		globalContext.addConnect(new Player(ctx));
	}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled

//    	if(!(frame instanceof TextWebSocketFrame || frame instanceof BinaryWebSocketFrame)){
//    		 String message = "unsupported frame type: " + frame.getClass().getName();
//             throw new UnsupportedOperationException(message);
//    	}
    	
    	if(frame instanceof TextWebSocketFrame){
    		handleText(ctx, (TextWebSocketFrame)frame);
    	}else if(frame instanceof BinaryWebSocketFrame){
    		handleBinary(ctx, (BinaryWebSocketFrame)frame);
    	}else{
    		String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
    	}
    }
    
    private void handleText(ChannelHandlerContext ctx, TextWebSocketFrame frame){
    	ByteBuf buf = frame.content();
    	System.out.println(buf.array().length); //16M的array字节数组大小！？
    	
    	// Send the uppercase string back.
        String request = frame.text();
        logger.info("{} received {}", ctx.channel(), request);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(request));
        
        handle(ctx, request);
    }
    
    private void handle(ChannelHandlerContext ctx, String request){
        Player player =  getPlayer(ctx);
        
        //Request wsRequest = JSONUtil.fromJSON(request, JsonRequest.class);
        Request wsRequest = JSON.parseObject(request, JsonRequest.class);
        dispatcher.dispatch(player, wsRequest);
    }
    
    private void handleBinary(ChannelHandlerContext ctx, BinaryWebSocketFrame frame){
    	ByteBuf buf = frame.content();
    	System.out.println(buf.array().length); //16M的array字节数组大小！？
    	
    	byte[] data = new byte[buf.readableBytes()];
    	buf.readBytes(data);
    	System.out.println(data.length);
//    	String text = buf.toString(CharsetUtil.UTF_8);
    	System.out.println(new String(data, CharsetUtil.UTF_8));
    	try {
//			TestProto proto = TestProto.parseFrom(data);
//			System.out.println(proto.getId()+", "+proto.getName());
//			
//			Player player =  getPlayer(ctx);
//			player.write(proto);
    		
    		ServiceMethodProto proto = ServiceMethodProto.parseFrom(data);
    		System.out.println(proto.getS()+", "+proto.getM()+", "+proto.getArgsList());
    		
//    		JsonProto proto = JsonProto.parseFrom(data);
//    		System.out.println(proto.getData());
    		
    		Player player =  getPlayer(ctx);
    		dispatcher.dispatch(player, proto);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
    }
    
    private Player getPlayer(ChannelHandlerContext ctx){
    	Player player =  ctx.channel().attr(Player.key).get();
        if(player == null){
        	//throw new BusinessException("not exist player!");
        	throw new RuntimeException("not exist player!");
        }
        return player;
    }
    
}
