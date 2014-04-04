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

import java.io.File;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "dir", propertyValue = ".") })
public class FSWatcherMDB implements FSWatcher {

	@Inject
	private Event<FileEvent> fileEvent;

	@Create(".*\\.txt")
	public void onNewTextFile(File f) {
		fileEvent.fire(new FileEvent(FileEvent.CREATE, f));
	}

	@Create(".*\\.pdf")
	public void onNewPdfFile(File f) {
		fileEvent.fire(new FileEvent(FileEvent.CREATE, f));
	}

	@Delete(".*\\.txt")
	public void onDeleteTextFile(File f) {
		fileEvent.fire(new FileEvent(FileEvent.DELETE, f));
	}
}
