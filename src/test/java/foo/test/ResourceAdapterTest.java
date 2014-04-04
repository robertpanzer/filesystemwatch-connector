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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import javax.enterprise.event.Observes;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ResourceAdapterTest {

	@Deployment
	public static EnterpriseArchive deploy() throws Exception {
		
		JavaArchive rarjar = ShrinkWrap.create(JavaArchive.class, "rar.jar")
				.addClasses(
						Create.class,
						Modify.class,
						Delete.class,
						FSWatcher.class,
						FSWatchingThread.class,
						FSWatcherResourceAdapter.class,
						FSWatcherActivationSpec.class);
		
		ResourceAdapterArchive rar = ShrinkWrap.create(ResourceAdapterArchive.class, "fswatcher.rar")
				.addAsLibrary(rarjar);
		
		JavaArchive ejbjar = ShrinkWrap.create(JavaArchive.class, "ejb.jar")
				.addClasses(
						FileEvent.class,
						FSWatcherMDB.class)
				.addAsManifestResource("jboss-ejb3.xml")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
		
		return ShrinkWrap.create(EnterpriseArchive.class, "testcase.ear")
				.addAsModules(
						rar,
						ejbjar);

	}

	private static CyclicBarrier barrier;
	
	private static File newFile;
	
	private static int mode;
	
	@Before
	public void init() {
		newFile = null;
		mode = 0;
		barrier = new CyclicBarrier(2);
	}
	
	@Test
	@InSequence(1)
	public void testTxtFile() throws Exception {
		
		File tempFile = new File(".", "testFile.txt");
		assertTrue("Could not create temp file", tempFile.createNewFile());
		
		barrier.await(5, TimeUnit.SECONDS);
		
		assertEquals(tempFile.getName(), newFile.getName());
		assertEquals(FileEvent.CREATE, mode);
	}

	@Test
	@InSequence(2)
	public void testPdfFile() throws Exception {

		File tempFile = new File(".", "test" + System.currentTimeMillis() + ".pdf");
		assertTrue("Could not create temp file", tempFile.createNewFile());
		tempFile.deleteOnExit();
		
		barrier.await(5, TimeUnit.SECONDS);
		
		assertEquals(tempFile.getName(), newFile.getName());
		assertEquals(FileEvent.CREATE, mode);
	}
	
	@Test
	@InSequence(3)
	public void testDeleteTxtFile() throws Exception {

		File tempFile = new File(".", "testFile.txt");
		assertTrue("Could not delete test file", tempFile.delete());
		
		barrier.await(5, TimeUnit.SECONDS);
		
		assertEquals(tempFile.getName(), newFile.getName());
		assertEquals(FileEvent.DELETE, mode);
	}
	
	public void notifyFileEvent(@Observes FileEvent fileEvent) {
		mode = fileEvent.getMode();
		newFile = fileEvent.getFile();
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
	}
	
}
