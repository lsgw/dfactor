package fun.lib.actor.core;

public final class DFActorDefine {

	public static final int CONSUME_AUTO = 0;
	public static final int CONSUME_SINGLE = 1;
//	public static final int CONSUME_QUARTER = 2;
	public static final int CONSUME_HALF = 3;
	public static final int CONSUME_ALL = 4;
		
	//
	public static final int SUBJECT_TIMER = 1;
	public static final int SUBJECT_SCHEDULE = 2;
	public static final int SUBJECT_NET = 3;
	public static final int SUBJECT_USER = 4;
	
	//
	protected static final int NET_TCP_LISTEN_RESULT = 1;
	protected static final int NET_TCP_LISTEN_CLOSED = 2;
	protected static final int NET_TCP_CONNECT_RESULT = 3;
	protected static final int NET_TCP_CONNECT_OPEN = 4;
	public static final int NET_TCP_CONNECT_CLOSE = 5;
	public static final int NET_TCP_MESSAGE = 6;
	public static final int NET_TCP_MESSAGE_TEXT = 7;
	
	protected static final int NET_UDP_LISTEN_RESULT = 8;
	protected static final int NET_UDP_LISTEN_CLOSED = 9;
	protected static final int NET_UDP_MESSAGE = 10;
	
	public static final int NET_KCP_MESSAGE = 11;
	public static final int NET_KCP_ACTIVE = 12;
	public static final int NET_KCP_INACTIVE = 13;
	
	//
	public static final int MSG_AUTO_RELEASE = 1;
	public static final int MSG_MANUAL_RELEASE = 2;
	
	//
	public static final int TCP_DECODE_LENGTH = 1;
	public static final int TCP_DECODE_RAW = 2;
	public static final int TCP_DECODE_WEBSOCKET = 3;
	public static final int TCP_DECODE_HTTP = 4;
	
	//
	protected static final String ACTOR_NAME_LOG = "SYSTEM_LOG_lostsky";
	
	protected static final int ACTOR_ID_LOG = 1;
	//
	public static final int ACTOR_ID_APP_BEGIN = 1000;
	
}
