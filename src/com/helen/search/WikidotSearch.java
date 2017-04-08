package com.helen.search;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSun15HttpTransportFactory;

import com.helen.database.Configs;

public class WikidotSearch {

	private static final Logger logger = Logger.getLogger(WikidotSearch.class);

	private static XmlRpcClientConfigImpl config;
	private static XmlRpcClient client;

	static {
		config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(Configs.getSingleProperty(
					"wikidotServer").getValue()));
			config.setBasicUserName(Configs.getSingleProperty("appName")
					.getValue());
			config.setBasicPassword(Configs.getSingleProperty("wikidotapikey")
					.getValue());
			config.setEnabledForExceptions(true);
			config.setConnectionTimeout(10 * 1000);
			config.setReplyTimeout(30 * 1000);

			client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcSun15HttpTransportFactory(
					client));
			client.setConfig(config);

		} catch (Exception e) {
			logger.error("There was an exception",e);
		}
	}

	private static Object pushToAPI(String method, Object... params)
			throws XmlRpcException {
		return (Object) client.execute(method, params);
	}

	public static void getMethodList()  {
		try {
			Object[] result = (Object[]) pushToAPI("system.listMethods",
					(Object[]) null);

			String[] methodList = new String[result.length];
			for (int i = 0; i < result.length; i++) {
				methodList[i] = (String) result[i];
			}

			for (String str : methodList) {
				logger.info(str);
			}
			// return methodList;
		} catch (Exception e) {
			logger.error("There was an exception", e);
		}
	}
	
	public static void listPagetest(){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("site",Configs.getSingleProperty("site").getValue());
		
		try{
		
				Object[] result = (Object[]) pushToAPI("pages.select", params);
				
				// Convert result to a String[]
				String[] pageList = new String[result.length];
				for (int i=0; i<result.length; i++)
				{
					pageList[i] = (String) result[i];
				}
				int i = 0;
				for(String str: pageList){
					if(i++ > 100){
						break;
					}
					logger.info(str);
				}
		}catch(Exception e){
			logger.error("There was an exception",e);
		}
	}

}