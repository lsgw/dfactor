package fun.lib.actor.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.concurrent.locks.StampedLock;

import com.funtag.util.concurrent.DFSpinLock;

import fun.lib.actor.api.DFTcpChannel;
import fun.lib.actor.api.DFUdpChannel;
import fun.lib.actor.define.DFActorErrorCode;
import fun.lib.actor.po.DFActorEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.ReferenceCountUtil;

public final class DFActorWrapper {

	
	private final LinkedList<DFActorMessage>[] _arrQueue = new LinkedList[2];
	private int[] _arrQueueSize = new int[2];
	private LinkedList<DFActorMessage> _queueWrite = null;
	private int _queueWriteIdx = 0;
	private int _queueReadIdx = 1;
	
	private final ReentrantReadWriteLock _lockQueue = new ReentrantReadWriteLock();
//	private final ReadLock _lockQueueRead = _lockQueue.readLock();
	private final WriteLock _lockQueueWrite = _lockQueue.writeLock();
//	private final DFSpinLock _lockQueue = new DFSpinLock();
//	private final StampedLock _lockQueue = new StampedLock();
	
	private final DFActor _actor;
	private final int _actorConsumeType;
	private boolean _bInGlobalQueue = false;
	private volatile boolean _bRemoved = false;
	private final int _actorId;
	private final boolean _isLogicActor;
	private final DFActorManager _actorMgr;
	private final String _consumeLock;
	
	
	protected DFActorWrapper(final DFActor actor) {
		this._actor = actor;
		_actorId = actor.getId();
		_actorConsumeType = actor.getConsumeType();
		_isLogicActor = !actor.isBlockActor;
		_consumeLock = _actorId + "_" + actor.name;
		//
		for(int i=0; i<2; ++i){
			_arrQueue[i] = new LinkedList<>();
			_arrQueueSize[i] = 0;
		}
		_queueWriteIdx = 0;
		_queueReadIdx = 1;
		_queueWrite = _arrQueue[_queueWriteIdx];
		//
		_actorMgr = DFActorManager.get();
	}
	protected int pushMsg(int srcId, int sessionId, 
			int subject, int cmd, Object payload, Object context, final boolean addTail){
		if(_bRemoved){
			return 1;
		}
		final DFActorMessage msg = _actorMgr.newActorMessage(srcId, _actorId, sessionId, subject, cmd, payload, context);
				//new DFActorMessage(srcId, _actorId, sessionId, subject, cmd, payload);
		//
		_lockQueueWrite.lock();
		try{
			if(addTail){
				_queueWrite.offer(msg);
			}else{
				_queueWrite.offerFirst(msg);
			}
			++_arrQueueSize[_queueWriteIdx];
			if(!_bInGlobalQueue){  //add to global queue
				_bInGlobalQueue = true;
				return 0;
			}
		}finally{
			_lockQueueWrite.unlock();
		}
		return 2;
	}
	protected int consumeMsg(int consumeType){
		if(_actorConsumeType != DFActorDefine.CONSUME_AUTO){
			consumeType = _actorConsumeType;
		}
		//
		int queueMsgLeft = 0;
		final LinkedList<DFActorMessage> queueRead;
		_lockQueueWrite.lock();
		try{
			final int curReadQueueSize = _arrQueueSize[_queueReadIdx];
			if(curReadQueueSize > 0){  //cur readQueue not empty, continue reading
				queueMsgLeft = curReadQueueSize;
				queueRead = _arrQueue[_queueReadIdx];
			}else{  //cur readQueue empty, swap queue
				queueMsgLeft = _arrQueueSize[_queueWriteIdx];
				queueRead = _arrQueue[_queueWriteIdx];
				//swap write queue
				_queueWriteIdx = _queueReadIdx;
				_queueReadIdx = (_queueReadIdx + 1)%2;
				_queueWrite = _arrQueue[_queueWriteIdx];
			}
		}finally{
			_lockQueueWrite.unlock();
		}
		//
		int targetNum = 1;  //default proc num 
		if(queueMsgLeft > 1){
			if(consumeType == DFActorDefine.CONSUME_ALL){
				targetNum = queueMsgLeft;
			}else if(consumeType == DFActorDefine.CONSUME_HALF){
				targetNum = Math.max(1, queueMsgLeft/2);
			}
		}
		//consume
		synchronized (_consumeLock) {
			final Iterator<DFActorMessage> it = queueRead.iterator();
			while(it.hasNext()){
				if(_bRemoved){
					break;
				}
				final DFActorMessage msg = it.next();
				it.remove();
				--queueMsgLeft;
				try{
					final int subject = msg.subject;
					if(subject == DFActorDefine.SUBJECT_SCHEDULE){
						_actor.onSchedule(msg.cmd);
					}else if(subject == DFActorDefine.SUBJECT_TIMER){
						_actor.onTimeout(msg.cmd);
					}else if(msg.subject == DFActorDefine.SUBJECT_NET){
						if(msg.cmd == DFActorDefine.NET_UDP_MESSAGE){ //udp msg
							final int ret = _actor.onUdpServerRecvMsg(msg.sessionId, (DFUdpChannel) msg.context, (DatagramPacket) msg.payload);
							if(ret == DFActorDefine.MSG_AUTO_RELEASE){ //auto release
								ReferenceCountUtil.release(msg.payload);
							}
						}else if(msg.cmd == DFActorDefine.NET_TCP_MESSAGE){ //tcp binary msg
							final int ret = _actor.onTcpRecvMsg(msg.srcId, (DFTcpChannel) msg.context, (ByteBuf) msg.payload);
							if(ret == DFActorDefine.MSG_AUTO_RELEASE){ //auto release
								ReferenceCountUtil.release(msg.payload);
							}
						}else if(msg.cmd == DFActorDefine.NET_TCP_MESSAGE_TEXT){ //tcp text msg(ws text)
							_actor.onTcpRecvMsg(msg.srcId, (DFTcpChannel)msg.context, (String) msg.payload);
						}else if(msg.cmd == DFActorDefine.NET_TCP_CONNECT_OPEN){
							_actor.onTcpConnOpen(msg.sessionId, (DFTcpChannel) msg.payload);
						}else if(msg.cmd == DFActorDefine.NET_TCP_CONNECT_CLOSE){
							_actor.onTcpConnClose(msg.sessionId, (DFTcpChannel) msg.payload);
						}else if(msg.cmd == DFActorDefine.NET_TCP_LISTEN_RESULT){
							final DFActorEvent event = (DFActorEvent) msg.payload;
							final boolean isSucc = event.getWhat()==DFActorErrorCode.SUCC?true:false;
							_actor.onTcpServerListenResult(msg.sessionId, isSucc, event.getMsg());
						}else if(msg.cmd == DFActorDefine.NET_TCP_CONNECT_RESULT){
							final DFActorEvent event = (DFActorEvent) msg.payload;
							final boolean isSucc = event.getWhat()==DFActorErrorCode.SUCC?true:false;
							_actor.onTcpClientConnResult(msg.sessionId, isSucc, event.getMsg());
						}else if(msg.cmd == DFActorDefine.NET_UDP_LISTEN_RESULT){
							final DFActorEvent event = (DFActorEvent) msg.payload;
							final boolean isSucc = event.getWhat()==DFActorErrorCode.SUCC?true:false;
							_actor.onUdpServerListenResult(msg.sessionId, isSucc, event.getMsg(), 
									(DFUdpChannel) event.getExtObj1());
						}
					}else{
						_actor.onMessage(msg.srcId, msg.sessionId, msg.subject, msg.cmd, msg.payload);
					}
				}catch(Throwable e){  //catch logic exception
					e.printStackTrace();
				}finally{
					if(--targetNum < 1){  //match target num
						break;
					}
					//
					_actorMgr.recycleActorMessage(msg);
				}
			}
		}
		_arrQueueSize[_queueReadIdx] = queueMsgLeft; //readQueue left num
		//
		if(_bRemoved){  //释放占用io内存的消息
			_release();
			return 1;
		}
		//check
		_lockQueueWrite.lock();
		try{
			if(queueMsgLeft > 0 || _arrQueueSize[_queueWriteIdx] > 0){ //still has msg in either queue, back to global queue
				_bInGlobalQueue = true;
				return 0;
			}else{	//both queue empty, mark removed from global queue
				_bInGlobalQueue = false;
			}
		}finally{
			_lockQueueWrite.unlock();
		}
		return 2;
	}
	protected void markRemoved(){
		_bRemoved = true;
	}
	protected boolean isRemoved(){
		return _bRemoved;
	}
	
	protected int getActorId(){
		return _actor.id;
	}
	protected String getActorName(){
		return _actor.name;
	}
	protected boolean isLogicActor(){
		return _isLogicActor;
	}
	
	private void _release(){
		_lockQueueWrite.lock();
		try{
			for(int i=0; i<2; ++i){
				final LinkedList<DFActorMessage> q = _arrQueue[i];
				final Iterator<DFActorMessage> itMsg = q.iterator();
				while(itMsg.hasNext()){
					final DFActorMessage m = itMsg.next();
					if(m.subject==DFActorDefine.SUBJECT_NET
							&&m.cmd==DFActorDefine.NET_TCP_MESSAGE){ //tcp binary msg
						ReferenceCountUtil.release(m.payload);
						m.payload = null;
					}
				}
				q.clear();
			}
		}finally{
			_lockQueueWrite.unlock();
		}
	}
}
