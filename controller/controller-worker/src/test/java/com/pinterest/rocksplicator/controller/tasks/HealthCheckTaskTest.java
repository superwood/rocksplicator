/*
 *  Copyright 2017 Pinterest, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.pinterest.rocksplicator.controller.tasks;

import com.pinterest.rocksplicator.controller.FIFOTaskQueue;

import org.apache.thrift.TException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Ang Xu (angxu@pinterest.com)
 */
public class HealthCheckTaskTest extends TaskBaseTest {

  @Test
  public void testSuccessful() throws Exception {
    HealthCheckTask t = new HealthCheckTask(
        new HealthCheckTask.Param().setNumReplicas(3)
    );
    injector.injectMembers(t);

    FIFOTaskQueue taskQueue = new FIFOTaskQueue(10);
    Context ctx = new Context(123, CLUSTER, taskQueue, null);
    t.process(ctx);

    Assert.assertEquals(taskQueue.getResult(123), "Cluster devtest is healthy");
  }

  @Test
  public void testFail() throws Exception {
    doThrow(new TException()).when(client).ping();

    HealthCheckTask t = new HealthCheckTask(
        new HealthCheckTask.Param().setNumReplicas(3)
    );
    injector.injectMembers(t);

    FIFOTaskQueue taskQueue = new FIFOTaskQueue(10);
    Context ctx = new Context(123, CLUSTER, taskQueue, null);
    t.process(ctx);

    Assert.assertTrue(taskQueue.getResult(123).startsWith("Unable to ping hosts"));
    Assert.assertTrue(taskQueue.getResult(123).contains("127.0.0.1:8090"));
    Assert.assertTrue(taskQueue.getResult(123).contains("127.0.0.1:8091"));
    Assert.assertTrue(taskQueue.getResult(123).contains("127.0.0.1:8092"));
  }

}
