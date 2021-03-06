package com.whg.websocket.server.framework.request;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.whg.websocket.server.framework.Player;
import com.whg.websocket.server.framework.method.MethodInvoker;

public abstract class DefaultRequest implements Request{

	/** 缓存基础对象类型的String构造器 */
	protected static final Map<Class<?>, Constructor<?>> constructorMap = new HashMap<Class<?>, Constructor<?>>();
	
	public String s;
	public String m;
	public String[] args;
	
	@Override
	public String service() {
		return s;
	}
	@Override
	public String method() {
		return m;
	}
	@Override
	public String serviceMethod() {
		return MethodInvoker.name(s, m);
	}
	
	@Override
	public int argsCount() {
		return args.length;
	}
	
	@Override
	public Object[] methodArgs(Player player, Class<?>[] argTypes) throws Exception{
		Object[] methodArgs = new Object[argTypes.length];
		methodArgs[0] = player;
		
		for(int i=0;i<argTypes.length-1;i++){
			Class<?> clazz = argTypes[i+1];
			if(clazz == String.class){
				methodArgs[i+1] = args[i];
			}else{
				Constructor<?> constructor = constructorMap.get(clazz);
				if(constructor == null){
					constructor = clazz.getConstructor(String.class);
					constructorMap.put(clazz, constructor);
				}
				methodArgs[i+1] = constructor.newInstance(args[i]);
			}
			
		}
		
		return methodArgs;
	}
	
}
