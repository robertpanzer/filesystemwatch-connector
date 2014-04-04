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

import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

@Activation(messageListeners = FSWatcher.class)
public class FSWatcherActivationSpec implements ActivationSpec {

	private ResourceAdapter resourceAdapter;

	private String dir;
	
	Class<?> beanClass;
	
	@Override
	public ResourceAdapter getResourceAdapter() {
		return resourceAdapter;
	}

	@Override
	public void setResourceAdapter(ResourceAdapter resourceAdapter)
			throws ResourceException {
		this.resourceAdapter = resourceAdapter;
	}

	@Override
	public void validate() throws InvalidPropertyException {
		
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}


}