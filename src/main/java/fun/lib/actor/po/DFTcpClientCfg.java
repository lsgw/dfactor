package fun.lib.actor.po;

import fun.lib.actor.api.DFActorTcpDispatcher;
import fun.lib.actor.core.DFActorDefine;

public final class DFTcpClientCfg {
	public final String host;
	public final int port;
	//
	private volatile int soRecvBufLen = 4096;
	private volatile int soSendBufLen = 4096;
	private volatile boolean tcpNoDelay = true;
	private volatile boolean keepAlive = true;
	private volatile long _connTimeout = 5000;
	
	private volatile int tcpDecodeType = DFActorDefine.TCP_DECODE_RAW;
	private volatile int tcpMsgMaxLength = 4096;
	
	/**
	 * 
	 * @param host 目标主机地址
	 * @param port 目标端口
	 */
	public DFTcpClientCfg(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public long getConnTimeout(){
		return _connTimeout;
	}
	public DFTcpClientCfg setConnTimeout(long timeoutMilli){
		_connTimeout = timeoutMilli;
		return this;
	}
	
	//
	public int getSoRecvBufLen(){
		return soRecvBufLen;
	}
	public DFTcpClientCfg setSoRecvBufLen(int len){
		this.soRecvBufLen = len;
		return this;
	}
	public int getSoSendBufLen(){
		return soSendBufLen;
	}
	public DFTcpClientCfg setSoSendBufLen(int len){
		this.soSendBufLen = len;
		return this;
	}
	
	public boolean isTcpNoDelay(){
		return tcpNoDelay;
	}
	public DFTcpClientCfg setTcpNoDelay(boolean tcpNoDelay){
		this.tcpNoDelay = tcpNoDelay;
		return this;
	}
	public boolean isKeepAlive(){
		return keepAlive;
	}
	public DFTcpClientCfg setKeepAlive(boolean keepAlive){
		this.keepAlive = keepAlive;
		return this;
	}
	
	public int getTcpDecodeType(){
		return tcpDecodeType;
	}
	public DFTcpClientCfg setTcpDecodeType(int tcpDecodeType){
		if(tcpDecodeType == DFActorDefine.TCP_DECODE_LENGTH ||
				tcpDecodeType == DFActorDefine.TCP_DECODE_RAW){ //valid
			
		}else{ //invalid
			tcpDecodeType = DFActorDefine.TCP_DECODE_RAW;
		}
		this.tcpDecodeType = tcpDecodeType;
		return this;
	}
	public int getTcpMsgMaxLength(){
		return tcpMsgMaxLength;
	}
	public DFTcpClientCfg setTcpMsgMaxLength(int maxLength){
		this.tcpMsgMaxLength = maxLength;
		return this;
	}
	
}




