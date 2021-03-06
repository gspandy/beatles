/**
 * 
 */
package com.taobao.top.analysis.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.analysis.util.AnalyzerUtil;
import com.taobao.top.analysis.util.ReportUtil;

/**
 * 配置抽象类
 * 参数采取动态获取，每次获取都会可能产生消耗，注意使用场景
 * @author fangweng
 *
 */
public abstract class AbstractConfig implements IConfig{
	
	private static final Log logger = LogFactory.getLog(AbstractConfig.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1038082086935557144L;
	
	/**
	 * 用于存储动态属性的map
	 */
	Map<String,String> properties = new HashMap<String,String>();
	
	/**
	 * 是否使用zookeeper作为集群的配置中心，默认关闭，使用本地文件
	 */
	public static final String USE_ZOOKEEPER = "useZookeeper";
	
	/**
	 * 扫描文件的频率
	 */
	public static final String SCANFILETIME = "scanTime";
	
	/**
	 * 如果使用本地文件配置，配置文件中将根据needScan字段判断是否定期扫描变更，文件最后修改时间
	 * 扫描线程暂定写在AbstractConfig中，后续若zookeeper集成进来，再考虑实现变更接口
	 */
	private long fileLastModifyTime;
	
	/**
	 * 配置文件名称
	 */
	private String configFile;
	
	public boolean isUseZookeeper()
	{
		if(this.properties.containsKey(USE_ZOOKEEPER))
			return Boolean.valueOf(this.properties.get(USE_ZOOKEEPER));
		else
			return false;
	}
	
	public void setUseZookeeper(String useZookeeper)
	{
		this.properties.put(USE_ZOOKEEPER,useZookeeper);
	}
	
	public int getScanFileTime() {
	    return Double.valueOf(
            this.properties.get(SCANFILETIME) == null ? "60"
                    : this.properties.get(SCANFILETIME)).intValue();
	}
	
	public void setScanFileTime(int scanFileTime) {
	    this.properties.put(SCANFILETIME, String.valueOf(scanFileTime));
	}
	
	public static String getSystemName(){
        String app="";
        if (System.getProperty("masterName") != null) {
            app=System.getProperty("masterName");
        } else {
            String processName = ManagementFactory.getRuntimeMXBean().getName();
            Long id = Long.parseLong(processName.split("@")[0]);
            app=""+id;
        }
        return app;
    }
	
	/**
	 * 从配置中根据名称获得属性内容
	 * @param propName
	 */
	@Override
	public String get(String propName)
	{
		return this.properties.get(propName);
	}
	
	@Override
	public void addAllToConfig(Map<String,String> props)
	{
		this.properties.putAll(props);
	}
	
	/**
	 * 从外部配置文件载入
	 * @param file
	 */
	@Override
	public void load(String file)
	{
		InputStream in = null;
		
		try
		{
			in = ReportUtil.getInputStreamFromFile(file);
			
			Properties prop = new Properties();
			prop.load(in);
			
			Iterator<Object> keys = prop.keySet().iterator();
			
			while(keys.hasNext())
			{
				String key = (String)keys.next();
				
				properties.put(key, prop.getProperty(key));
			}
			
			configFile = file;
			
			fileLastModifyTime = (new File(file)).lastModified();
			
		}
		catch(Exception ex)
		{
			logger.error(ex,ex);
		}
		finally
		{
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					logger.error(e,e);
				}
		}
		
	}
	
	public String toString()
	{
		StringBuilder st = new StringBuilder();
		
		Iterator<String> keys = properties.keySet().iterator();
		
		while(keys.hasNext())
		{
			String key = keys.next();
			
			st.append(key).append("=").append(properties.get(key)).append(" ,");
		}
		
		return st.toString();
	}
	
	public boolean equals(Object obj) {
	    if(!(obj instanceof AbstractConfig))
	        return false;
	    if(this == obj)
	        return true;
	    AbstractConfig config = AbstractConfig.class.cast(obj);
	    return AnalyzerUtil.covertNullToEmpty(this.toString()).equals(AnalyzerUtil.covertNullToEmpty(config.toString()));
	}
	
	/* (non-Javadoc)
     * @see com.taobao.top.analysis.config.IConfig#isNeedReload()
     */
    @Override
    public boolean isNeedReload() {
        File prop = new File(this.configFile);
        if (prop.isFile() && prop.lastModified() != this.fileLastModifyTime) {
            this.fileLastModifyTime = prop.lastModified();
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.taobao.top.analysis.config.IConfig#reload()
     */
    @Override
    public void reload() {
        this.load(configFile);
        logger.error("trying to reload config from " + configFile + ", please check that it's ok");
    }
}
