package webServer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.List;

public class Response {
	
	private String requestUrl;
	private BufferedWriter bw;
	//正文
	private StringBuilder content;
	//协议头（状态行与请求头 回车）信息
	private StringBuilder headInfo;
	private int len; //正文的字节数
	
	private final String BLANK = " ";
	private final String CRLF = "\r\n";
	
	private Response() {
		content = new StringBuilder();
		headInfo = new StringBuilder();
		len = 0;
	}
	
	public Response(Socket client, String requestUrl) {
		this();
		this.requestUrl = requestUrl;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		} 
		catch (IOException e) {
			e.printStackTrace();
			headInfo = null;
		}
	}
	
	//动态添加内容
	public Response print(String info) {
		content.append(info);
		len += info.getBytes().length;
		return this;
	}
	
	public Response println(String info) {
		content.append(info).append(CRLF);
		len += (info+CRLF).getBytes().length;
		return this;
	}
	
	//推送响应信息
	public void pushToBrowser(int code) throws IOException {
		if(headInfo == null) {
			code = 500;
		}
		createHeadInfo(code);
		bw.append(headInfo);
		bw.append(content);
		bw.flush();
	}
	
	public void pushToBrowser(int code, List<String> insertedRes) throws IOException {
		if(headInfo == null) {
			code = 500;
		}
		createHeadInfo(code);
		int size = headInfo.length();
		for(String item: insertedRes) {
			headInfo.insert(size-2, item+CRLF);
		}
		bw.append(headInfo);
		bw.append(content);
		bw.flush();
	}
	
	//构建头信息
	private void createHeadInfo(int code) {
		//1、响应行: HTTP/1.1 200 OK
		headInfo.append("HTTP/1.1").append(BLANK);
		headInfo.append(code).append(BLANK);
		switch(code) {
			case 200:
				headInfo.append("OK").append(CRLF);
				break;
			case 301:
				headInfo.append("Moved Permanently").append(CRLF);
				headInfo.append("Location:").append("http://www.baidu.com/").append(CRLF);
				break;
			case 404:
				headInfo.append("NOT FOUND").append(CRLF);
				break;
			case 405:
				headInfo.append("METHOD NOT ALLOWED").append(CRLF);
				break;
			case 500:
				headInfo.append("SERVER ERROR").append(CRLF);
				break;
		}
		//2、响应头(最后一行存在空行):
		headInfo.append("Date:").append(new Date()).append(CRLF);
		headInfo.append("Server:").append("zbc Server/0.0.1;charset=utf-8").append(CRLF);
		analyseMime();
		headInfo.append("Content-length:").append(len).append(CRLF);
		headInfo.append("Connection:keep-alive").append(CRLF);
		headInfo.append(CRLF);	
	}
	
	private void analyseMime() {
		headInfo.append("Content-Type:");
		if(requestUrl.endsWith(".html"))       headInfo.append("text/html");
		else if(requestUrl.endsWith(".css"))   headInfo.append("text/css");
		else if(requestUrl.endsWith(".js"))    headInfo.append("application/javascript");
		else if(requestUrl.endsWith(".png"))   headInfo.append("image/png");
		else if(requestUrl.endsWith(".gif"))   headInfo.append("image/gif");
		else if(requestUrl.endsWith(".jpg"))   headInfo.append("image/jpeg");
		else if(requestUrl.endsWith(".jpeg"))  headInfo.append("image/jpeg");
		else if(requestUrl.endsWith(".json"))  headInfo.append("application/json");
		else                                   headInfo.append("text/html");
		headInfo.append(CRLF);
	}

	public String getResponseInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append(headInfo).append(content);
		return sb.toString();
	}
}
