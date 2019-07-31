package org.ballerinalang.runtime.threadpool;



public interface ThreadPoolFactoryMBean {

    public void setCoreThreadPoolSize(int size);

    public void setMaxThreadPoolSize(int size);

    public void setKeepAliveTime(long time);

    public int getCoreThreadPoolSize();

    public int getMaxThreadPoolSize();

    public long getKeepAliveTime();
}