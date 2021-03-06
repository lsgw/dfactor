package fun.lib.actor.core;

import com.alibaba.fastjson.JSONObject;

import fun.lib.actor.api.DFTcpChannel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public final class DFTcpChannelWrapper implements DFTcpChannel{

	private static int s_sessionIdCount = 1;
	//
	private final String _remoteHost;
	private final int _remotePort;
	private final Channel _channel;
	private final int _tcpDecodeType;
	private volatile boolean _isClosed = false;
	private volatile int _statusActorId = 0;
	private volatile int _msgActorId = 0;
	private volatile int _sessionId = 0;
	private long _openTime = 0;
	//
	protected DFTcpChannelWrapper(String remoteHost, int remotePort, final Channel channel, 
			final int decodeType){
		this._remoteHost = remoteHost;
		this._remotePort = remotePort;
		this._channel = channel;
		this._tcpDecodeType = decodeType;
		//
		synchronized (DFTcpChannelWrapper.class) {
			this._sessionId = s_sessionIdCount;
			if(++s_sessionIdCount >= Integer.MAX_VALUE){
				s_sessionIdCount = 1;
			}
		}
		
	}
	
	protected void onClose(){
		_isClosed = true;
	}
	
	protected int getStatusActor(){
		return _statusActorId;
	}
	protected int getMsgActor(){
		return _msgActorId;
	}

	@Override
	public String getRemoteHost() {
		return _remoteHost;
	}
	@Override
	public int getRemotePort() {
		return _remotePort;
	}

	@Override
	public int write(Object msg) {
		if(_isClosed){
			return 1;
		}
		if(_tcpDecodeType == DFActorDefine.TCP_DECODE_WEBSOCKET){ //web socket frame
			if(msg instanceof String){
				_channel.writeAndFlush(new TextWebSocketFrame((String) msg));
			}else if(msg instanceof ByteBuf){
				_channel.writeAndFlush(new BinaryWebSocketFrame((ByteBuf) msg));
			}else{  //按json消息处理
				final String str = JSONObject.toJSONString(msg);
				_channel.writeAndFlush(new TextWebSocketFrame(str));
			}
		}else{
			_channel.writeAndFlush(msg);
		}
		return 0;
	}

	@Override
	public boolean isClosed() {
		return _isClosed;
	}

	@Override
	public void setStatusActor(int actorId) {
		_statusActorId = actorId;
	}

	@Override
	public void setMessageActor(int actorId) {
		_msgActorId = actorId;
	}

	@Override
	public int getChannelId() {
		// TODO Auto-generated method stub
		return _sessionId;
	}

	@Override
	public void close() {
		if(!_isClosed){
			_channel.close();
		}
	}

	@Override
	public long getOpenTime() {
		// TODO Auto-generated method stub
		return _openTime;
	}
	protected void setOpenTime(long tm){
		_openTime = tm;
	}
}
