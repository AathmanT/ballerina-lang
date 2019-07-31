/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.runtime.threadpool;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.omg.PortableServer.ThreadPolicyOperations;
import org.wso2.msf4j.util.RuntimeAnnotations;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.*;

/**
 * {@code ThreadPoolFactory} holds the thread pools in Ballerina engine.
 *
 * @since 0.8.0
 */
public class ThreadPoolFactory implements ThreadPoolFactoryMBean{

    private static final int DEFAULT_THREAD_POOL_SIZE = 100;
    private static final int MAX_THREAD_POOL_SIZE = 2147483647;
    private static final int MIN_THREAD_POOL_SIZE = 1;

    private static final String WORKER_THREAD_POOL_SIZE_PROP = "b7a.runtime.scheduler.threadpoolsize";
    private static final String CORE_POOL_SIZE_PROP = "b7a.runtime.scheduler.corepoolsize";
    private static final String MAX_POOL_SIZE_PROP = "b7a.runtime.scheduler.maxpoolsize";
    private static final String QUEUE_CAPACITY_PROP = "b7a.runtime.scheduler.queuecapacity";
    private static final String QUEUE_TYPE_PROP = "b7a.runtime.scheduler.queuetype";
    private static final String KEEP_ALIVE_TIME_PROP = "b7a.runtime.scheduler.keepalivetime";

    private static ThreadPoolFactory instance;

    private ThreadPoolExecutor workerExecutor;

    private ThreadPoolFactory() {
        //int poolSize = this.extractThreadPoolSize();
        //this.workerExecutor = Executors.newFixedThreadPool(poolSize,
        //        new BLangThreadFactory(new ThreadGroup("worker"), "worker-thread-pool"));

        int corePoolSize = this.extractThreadPoolSize(CORE_POOL_SIZE_PROP);
        int maxPoolSize = this.extractThreadPoolSize(MAX_POOL_SIZE_PROP);
        int queueCapacity = this.extractThreadPoolSize(QUEUE_CAPACITY_PROP);
        int keepAliveTime = this.extractThreadPoolSize(KEEP_ALIVE_TIME_PROP);
        String type = ConfigRegistry.getInstance().getAsString(QUEUE_TYPE_PROP);
        if(type.equals("linked")) {
            this.workerExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(queueCapacity),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }
        else if(type.equals("array")){
            this.workerExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(queueCapacity),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }
        else if(type.equals("default-linked")){
            this.workerExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }
    };
    
    private int extractThreadPoolSize(String property) {
        int poolSize = DEFAULT_THREAD_POOL_SIZE;
        String workerThreadPoolSizeProp = ConfigRegistry.getInstance().getAsString(property);
        if (workerThreadPoolSizeProp != null) {
            try {
                poolSize = Integer.parseInt(workerThreadPoolSizeProp);
                if (poolSize < MIN_THREAD_POOL_SIZE || poolSize > MAX_THREAD_POOL_SIZE) {
                    throw new BallerinaException(WORKER_THREAD_POOL_SIZE_PROP + " must be between "
                            + MIN_THREAD_POOL_SIZE + " and " + MAX_THREAD_POOL_SIZE + " (inclusive)");
                }
            } catch (NumberFormatException ignore) { 
                throw new BallerinaException("invalid value for '" + WORKER_THREAD_POOL_SIZE_PROP 
                        + "': " + workerThreadPoolSizeProp);
            }
        }
        return poolSize;
    }

    public static ThreadPoolFactory getInstance() {
        if (instance == null) {
            synchronized (ThreadPolicyOperations.class) {
                if (instance == null) {
                    instance = new ThreadPoolFactory();

                    MBeanServer server = ManagementFactory.getPlatformMBeanServer();

                    ObjectName objectName = null;
                    try {
                        objectName = new ObjectName("com.ballerina.autotuning:type=basic,name=dynamicTuning");
                        server.registerMBean(instance,objectName);

                        LocateRegistry.createRegistry(1234);
                        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:1234/jmxrmi");
                        JMXConnectorServer svr = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
                        svr.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }
        }
        return instance;
    }

    public ExecutorService getWorkerExecutor() {
        return workerExecutor;
    }

    @Override
    public void setCoreThreadPoolSize(int size) {
        this.workerExecutor.setCorePoolSize(size);
    }

    @Override
    public void setMaxThreadPoolSize(int size) {
        this.workerExecutor.setMaximumPoolSize(size);
    }

    @Override
    public void setKeepAliveTime(long time) {
        this.workerExecutor.setKeepAliveTime(time,TimeUnit.SECONDS);
    }

    @Override
    public int getCoreThreadPoolSize() {
        return this.workerExecutor.getCorePoolSize();
    }

    @Override
    public int getMaxThreadPoolSize() {
        return this.workerExecutor.getMaximumPoolSize();
    }

    @Override
    public long getKeepAliveTime() {
        return this.workerExecutor.getKeepAliveTime(TimeUnit.SECONDS);
    }
}
