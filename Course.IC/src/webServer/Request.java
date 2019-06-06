package webServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该类封装了请求协议，将请求参数封装为Map
 * @author zbc
 *
 */
public class Request {

	//协议信息
	private String requestInfo;
	//请求方式
	private String method; 
	//请求url
	private String url; 
	//请求参数
	private String queryStr;
	//存储参数
	private Map<String, List<String>> parameterMap;
	//换行符
	private final String CRLF = "\r\n";
	
	public Request(Socket client) throws IOException {
		InputStream is = client.getInputStream();
		parameterMap = new HashMap<String, List<String>>();
		byte[] datas = new byte[1024*1024*1024];
		int len = is.read(datas);
		requestInfo = new String(datas, 0, len);
		System.out.println(requestInfo);
		//分解字符串
		parseRequestInfo();
	}
	
	//分解字符串
	private void parseRequestInfo() {
		//1、获取请求方式: 开头到第一个/
		int startIdx = requestInfo.indexOf("/");
		method = requestInfo.substring(0, startIdx).toLowerCase().trim();
		//2、获取请求url: 第一个/ 到 HTTP/（可能包含请求参数，?前面的为真正的url，后面的为请求参数）
		//2.1、获取 HTTP/的位置
		int endIdx = requestInfo.indexOf("HTTP/");
		//2.2、截取字符串
		url = requestInfo.substring(startIdx+1, endIdx).trim();		
		//2.3、获取?的位置，若存在则继续分割
		int queryIdx = url.indexOf("?");	
		if(queryIdx >= 0) {
			String[] urlArray = this.url.split("\\?");
			url = urlArray[0];
			queryStr = urlArray[1];
		}
		
		//3、获取请求参数:如果Get已经获取,如果是post可能在请求体中
		if(method.equals("post")) {
			String qStr = requestInfo.substring(requestInfo.lastIndexOf(CRLF)).trim();
			if(queryStr == null) {
				queryStr = qStr;
			}
			else { 
				queryStr += "&"+qStr;
			}
		}
		queryStr = queryStr==null ? "" : queryStr;
		System.out.println(method+"-->"+url+"-->"+queryStr);
		//转成Map fav=1&fav=2&uname=shsxt&age=18&others=
		convertMap();
	}
	
	//将请求参数封装为Map
	private void convertMap() {
		//1、分割字符串 &
		String[] keyValues = queryStr.split("&");
		for(String queryStr: keyValues) {
			//2、再次分割字符串  =
			String[] kv = queryStr.split("=");
			kv = Arrays.copyOf(kv, 2);  //保证kv的长度为2
			//获取key和value
			String key = kv[0];
			String value = kv[1]==null ? null : decode(kv[1], "utf-8");
			//存储到map中
			if(!parameterMap.containsKey(key)) {
				parameterMap.put(key, new ArrayList<String>());
			}
			parameterMap.get(key).add(value);			
		}
	}
	
	/**
	 * 处理中文
	 * @return
	 */
	private String decode(String value, String enc) {
		try {
			return java.net.URLDecoder.decode(value, enc);
		} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 通过name获取对应的多个值
	 * @param key
	 * @return
	 */
	public String[] getParameterValues(String key) {
		List<String> values = this.parameterMap.get(key);
		if(values == null || values.size() == 0) {
			return null;
		}
		return values.toArray(new String[0]);
	}
	
	/**
	 * 通过name获取对应的一个值
	 * @param key
	 * @return
	 */
	public String getParameter(String key) {
		String[] values = getParameterValues(key);
		return values==null ? null : values[0];
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getQueryStr() {
		return queryStr;
	}
	
}
