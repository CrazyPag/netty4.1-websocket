package com.whg.websocket.server.framework.thread.pool;

public interface ThreadPoolSelector {

	int selectPool();

	void setPoolsState(PoolState[] poolStates);

}
