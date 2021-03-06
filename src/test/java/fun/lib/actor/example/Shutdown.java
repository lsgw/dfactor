package fun.lib.actor.example;

import fun.lib.actor.core.DFActor;
import fun.lib.actor.core.DFActorDefine;
import fun.lib.actor.core.DFActorManager;
import fun.lib.actor.core.DFActorManagerConfig;
/**
 * 关闭整个dfactor示例
 * @author lostsky
 *
 */
public final class Shutdown {

	public static void main(String[] args) {
		final DFActorManager mgr = DFActorManager.get();
		//启动配置参数
		DFActorManagerConfig cfg = new DFActorManagerConfig()
				.setLogicWorkerThreadNum(2);  //设置逻辑线程数量
		//启动入口actor，开始消息循环		
		mgr.start(cfg, "EntryActor", EntryActor.class);
	}

	/**
	 * 入口actor
	 * @author lostsky
	 *
	 */
	private static class EntryActor extends DFActor{
		public EntryActor(Integer id, String name, Integer consumeType, Boolean isBlockActor) {
			super(id, name, consumeType, isBlockActor);
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onStart(Object param) {
			//使用自带日志打印
			log.info("EntryActor start, curThread="+Thread.currentThread().getName());
			//启动定时器
			int delay = DFActor.transTimeRealToTimer(1000); //1秒(1000毫秒)后触发定时器
			sys.timeout(delay, 1);
		}
		
		@Override
		public void onTimeout(int requestId) {
			log.info("onTimeout, requestId="+requestId+", curThread="+Thread.currentThread().getName());
			//关闭整个dfactor
			DFActorManager.get().shutdown();
		}
		
		@Override
		public int onMessage(int srcId, int requestId, int subject, int cmd, Object payload) {
			// TODO Auto-generated method stub
			return 0;
		}

		
		
	}
}
