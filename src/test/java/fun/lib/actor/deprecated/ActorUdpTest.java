package fun.lib.actor.deprecated;

import java.nio.charset.Charset;
import java.util.ArrayList;

import fun.lib.actor.api.DFUdpChannel;
import fun.lib.actor.api.DFUdpDispatcher;
import fun.lib.actor.core.DFActor;
import fun.lib.actor.core.DFActorDefine;
import fun.lib.actor.core.DFActorManager;
import fun.lib.actor.core.DFActorManagerConfig;
import fun.lib.actor.define.DFActorErrorCode;
import fun.lib.actor.po.DFActorEvent;
import fun.lib.actor.po.DFUdpServerCfg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.socket.DatagramPacket;

public final class ActorUdpTest extends DFActor implements DFUdpDispatcher{

	protected ActorUdpTest(Integer id, String name, Integer consumeType, Boolean isIoActor) {
		super(id, name, consumeType, isIoActor);
		// TODO Auto-generated constructor stub
	}

	private DFUdpChannel channel = null;
	@Override
	public int onMessage(int srcId, int requestId, int subject, int cmd, Object payload) {
		
		return DFActorDefine.MSG_AUTO_RELEASE;
	}

	private final int listenPort = 13500;
	@Override
	public void onStart(Object param) {
		log.debug("onStart");	
		
		_initKcpListen();
		
		sys.timeout(100, 1);
	}
	
	private void _initKcpListen(){
		final DFUdpServerCfg cfg = new DFUdpServerCfg(listenPort, 1, false);
		net.doUdpListen(cfg, this, 1);
	}
	
	@Override
	public void onTimeout(int requestId) {
		
//		doUdpListenClose(listenPort);
	}

	@Override
	public int queryMsgActorId(DatagramPacket pack) {
		return id;
	}
	
	
	public static void main(String[] args) {
		//
		final DFActorManager mgr = DFActorManager.get();
		DFActorManagerConfig cfg = new DFActorManagerConfig()
				.setLogicWorkerThreadNum(4)
				.setBlockWorkerThreadNum(0);
		mgr.start(cfg, "actor_udp_test", ActorUdpTest.class, ""+1000, 0, DFActorDefine.CONSUME_AUTO);
		
	}
}
