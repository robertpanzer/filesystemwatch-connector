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

/**
 * This class is used as a CDI event to make the test case know about calls of the MDB.
 */
public class FileEvent {

	public static int CREATE = 1;
	public static int DELETE = 2;
	public static int MODIFY = 3;
	
	private File file;
	
	private int mode;
	
	public FileEvent(int mode, File file) {
		this.mode = mode;
		this.file = file;
	}

	public File getFile() {
		return file;
	}
	
	public int getMode() {
		return mode;
	}
}
