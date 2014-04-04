/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package foo.test;

import java.lang.reflect.Method;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

final class FSWatchingThread extends Thread {

	private WatchService watchService;
	
	private FSWatcherResourceAdapter resourceAdapter;

	/**
	 * @param listeners 
	 * @param fsWatcherResourceAdapter
	 */
	FSWatchingThread(WatchService watchService, 
			FSWatcherResourceAdapter ra) {
		this.watchService = watchService;
		this.resourceAdapter = ra;
	}

	public void run() {
		while (true) {
			try {
				WatchKey watchKey = watchService.take();
				if (watchKey != null) {
					dispatchEvents(watchKey.pollEvents(), resourceAdapter.getListener(watchKey));
					watchKey.reset();
				}
			} catch (ClosedWatchServiceException e) {
				// Fine, we've been stopped
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void dispatchEvents(List<WatchEvent<?>> events, MessageEndpointFactory messageEndpointFactory) {
		for (WatchEvent<?> event: events) {
			Path path = (Path) event.context();

			try {
				MessageEndpoint endpoint = messageEndpointFactory.createEndpoint(null);
				Class<?> beanClass = resourceAdapter.getBeanClass(messageEndpointFactory);
				for (Method m: beanClass.getMethods()) {
					if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind()) 
							&& m.isAnnotationPresent(Create.class)
							&& path.toString().matches(m.getAnnotation(Create.class).value())) {
						invoke(endpoint, m, path);
					} else if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind()) 
							&& m.isAnnotationPresent(Delete.class)
							&& path.toString().matches(m.getAnnotation(Delete.class).value())) {
						invoke(endpoint, m, path);
					} else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind()) 
							&& m.isAnnotationPresent(Modify.class)
							&& path.toString().matches(m.getAnnotation(Modify.class).value())) {
						invoke(endpoint, m, path);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void invoke(final MessageEndpoint endpoint, final Method m, final Path path) throws WorkException {
		resourceAdapter.getBootstrapContext().getWorkManager().scheduleWork(new Work() {
			
			@Override
			public void run() {
				try {
					Method endpointMethod = endpoint.getClass().getMethod(m.getName(), m.getParameterTypes()); 
					endpoint.beforeDelivery(endpointMethod);
					
					endpointMethod.invoke(endpoint, path.toFile());
					
					endpoint.afterDelivery();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void release() {}
		});
	}
}