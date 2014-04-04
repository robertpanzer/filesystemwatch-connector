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

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

@Connector
public class FSWatcherResourceAdapter implements ResourceAdapter {

	FileSystem fileSystem;

	WatchService watchService;
	
	Map<WatchKey, MessageEndpointFactory> listeners = new ConcurrentHashMap<>();

	Map<MessageEndpointFactory, Class<?>> endpointFactoryToBeanClass = new ConcurrentHashMap<>();
	
	private BootstrapContext bootstrapContext;
	
	@Override
	public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec activationSpec) throws ResourceException {
		FSWatcherActivationSpec fsWatcherAS = (FSWatcherActivationSpec) activationSpec;
		
		try {
			WatchKey watchKey = fileSystem.getPath(fsWatcherAS.getDir()).register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			listeners.put(watchKey, endpointFactory);

			// On TomEE the endpoint class is available via activationSpec.getBeanClass() 
			// even though not JavaEE 7 compliant yet!
			endpointFactoryToBeanClass.put(
					endpointFactory, 
					fsWatcherAS.getBeanClass() != null ? fsWatcherAS.getBeanClass() : endpointFactory.getEndpointClass());
		} catch (IOException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec activationSpec) {
		for (WatchKey watchKey: listeners.keySet()) {
			if (listeners.get(watchKey) == endpointFactory) {
				listeners.remove(watchKey);
				break;
			}
		}
		endpointFactoryToBeanClass.remove(endpointFactory);
	}

	@Override
	public XAResource[] getXAResources(ActivationSpec[] arg0) throws ResourceException {
		return null;
	}

	@Override
	public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
		this.bootstrapContext = bootstrapContext;
		
		try {
			fileSystem = FileSystems.getDefault();
			watchService = fileSystem.newWatchService();
		} catch (IOException e) {
			throw new ResourceAdapterInternalException(e);
		}
		
		new FSWatchingThread(watchService, this).start();
	}

	@Override
	public void stop() {
		try {
			watchService.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public MessageEndpointFactory getListener(WatchKey watchKey) {
		return listeners.get(watchKey);
	}

	public BootstrapContext getBootstrapContext() {
		return bootstrapContext;
	}

	public Class<?> getBeanClass(MessageEndpointFactory endpointFactory) {
		return endpointFactoryToBeanClass.get(endpointFactory);
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
