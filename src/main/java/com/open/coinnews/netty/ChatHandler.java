package com.open.coinnews.netty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.open.coinnews.SpringUtil;
import com.open.coinnews.app.service.UserService;
import com.open.coinnews.basic.tools.MsgActionEnum;
import com.open.coinnews.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @Description: 处理消息的handler
 * TextWebSocketFrame： 在netty中，是用于为websocket专门处理文本的对象，frame是消息的载体
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

	// 用于记录和管理所有客户端的channle
	private static ChannelGroup users =
			new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) 
			throws Exception {
		// 获取客户端传输过来的消息
		String content = msg.text();

		Channel currentChannel = ctx.channel();
		//获取客户端发来的消息
		DataContent dataContent = JsonUtils.jsonToPojo(content,DataContent.class);
		Integer action = dataContent.getAction();

		//判断消息类型，根据不同的类型来处理不同的业务

		if (action == MsgActionEnum.CONNECT.type){

			//当websocket 第一次open的时候，初始化channel,把channel和userid关联起来
			String senderId = dataContent.getChatMsg().getSenderId();
			UserChannelRel.put(senderId,currentChannel);

			for (Channel c:users){
				System.out.println(c.id().asLongText());
			}

			UserChannelRel.output();

		}else if (action == MsgActionEnum.CHAT.type){

			//聊天类型的消息，把聊天记录保存到数据库，标记消息的签收状态 未签收
			ChatMsg msg1 = dataContent.getChatMsg();

			DataContent dataContentMsg = new DataContent();
			dataContentMsg.setChatMsg(msg1);

			String msgText = msg1.getMsg();
			String receiverId = msg1.getReceiverId();
			String senderId = msg1.getSenderId();

			UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
			userService.savenMsg(msg1);

			Channel receiverChannel = UserChannelRel.get(receiverId);
			if(receiverChannel == null){
				//用户离线，推送消息（）
			}else{
				Channel findChannel = users.find(receiverChannel.id());
				if(findChannel !=null){
					receiverChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContentMsg)));
				}else {
					//用户离线推送
				}
			}

		}else if(action == MsgActionEnum.SIGNED.type){

			//签收消息类型，针对具体的消息进行签收，修改数据库中对应消息的签收状态 已签收
			UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
			String msgIdStr = dataContent.getExtand();
			String msgIds [] = msgIdStr.split(",");

			List<String> msgIdList = new ArrayList<>();
			for (String id :msgIds){

				if(StringUtils.isNotBlank(id)){

					msgIdList.add(id);
				}
				if(msgIdList !=null && !msgIdList.isEmpty()&&msgIdList.size()>0){
					userService.udpateMsgSigned(msgIdList);
				}
			}
		}else if(action == MsgActionEnum.KEEPALIVE.type){

			//心跳类型，
		}

		/*users.writeAndFlush(
				new TextWebSocketFrame(
						"[服务器在]" + LocalDateTime.now() 
						+ "接受到消息, 消息为：" + content));*/
		
	}

	/**
	 * 当客户端连接服务端之后（打开连接）
	 * 获取客户端的channle，并且放到ChannelGroup中去进行管理
	 */
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		users.add(ctx.channel());
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// 当触发handlerRemoved，ChannelGroup会自动移除对应客户端的channel
		users.remove(ctx.channel());
		/*System.out.println("客户端断开，channle对应的长id为："
							+ ctx.channel().id().asLongText());
		System.out.println("客户端断开，channle对应的短id为：" 
							+ ctx.channel().id().asShortText());*/
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		//发生异常之后关闭连接
		ctx.channel().close();
		users.remove(ctx.channel());
	}
}
