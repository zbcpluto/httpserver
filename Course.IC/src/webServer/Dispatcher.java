package webServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 分发器：采用多线程处理每一个客户端
 * @author zbc
 *
 */
public class Dispatcher implements Runnable {
	
	private Socket client;
	private Request request;
	private Response response;
	private List<String> insertedRes;
	
	public Dispatcher(Socket client) {
		this.client = client;
		try {
			//获取请求协议与获取响应协议
			request = new Request(client);
			response = new Response(client, request.getUrl());
			insertedRes = new ArrayList<String>();
		} catch (IOException e) {
			e.printStackTrace();
			this.release();
		}
	}

	@Override
	public void run() {
		String requestUrl = request.getUrl();
		if(requestUrl == null || requestUrl.equals("")) {
			printIndex();
		}
		else if(requestUrl.equals("login.html")) {
			if(request.getMethod().equals("get")) {
				printLogin();
			}
			else {
				printError(405);
			}
		}
		else if(requestUrl.equals("reg.html")) {
			StringBuilder value = new StringBuilder("Location:register.html");
			String queryStr = request.getQueryStr();
			if(queryStr.length() > 0) {
				value.append("?").append(queryStr);
			}
			insertedRes.add(value.toString());
			printReg();
		}
		else if(requestUrl.equals("register.html")) {
			printRegister();
		}
		else {
			printError(404);
		}	
		System.out.println(response.getResponseInfo());
		release();
	}
	
	private void printIndex() {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("index.html");
			byte[] datas = new byte[1024*1024];
			int len = is.read(datas);
			response.print((new String(datas, 0, len)));
			response.pushToBrowser(200);
			is.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			printServerError();
		}
		
	}
	
	private void printLogin() {
		try {
			response.print("<html>"); 
			response.print("<head>"); 
			response.print("<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">" ); 
			response.print("<title>");
			response.print("登陆");
			response.print("</title>"); 
			response.print("</head>");
			response.print("<body>");
			response.print("欢迎回来:" + request.getParameter("uname"));
			response.print("</body>");
			response.print("</html>");
			response.pushToBrowser(200);
		} catch (IOException e) {
			e.printStackTrace();
			printServerError();
		}
	}
	
	private void printReg() {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("301Redirect.html");
			byte[] datas = new byte[1024*1024];
			int len = is.read(datas);
			response.print((new String(datas, 0, len)));
			response.pushToBrowser(301, insertedRes);
			is.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			printServerError();
		}
		
	}
	
	private void printRegister() {
		try {
			response.print("<html>"); 
			response.print("<head>"); 
			response.print("<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">" ); 
			response.print("<title>");
			response.print("注册");
			response.print("</title>"); 
			response.print("</head>");
			response.print("<body>");
			response.print("欢迎注册zbc服务器:" + request.getParameter("uname"));
			response.print("</body>");
			response.print("</html>");
			response.pushToBrowser(200);
		} catch (IOException e) {
			e.printStackTrace();
			printServerError();
		}
	}
	
	private void printError(int code) {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(code+"error.html");
			byte[] datas = new byte[1024*1024];
			int len = is.read(datas);
			response.print((new String(datas, 0, len)));
			response.pushToBrowser(code);
			is.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			printServerError();
		}
		
	}
	
	private void printServerError() {
		try {
			response.print("<html>"); 
			response.print("<head>"); 
			response.print("<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">" ); 
			response.print("<title>");
			response.print("服务器出错");
			response.print("</title>"); 
			response.print("</head>");
			response.print("<body>");
			response.print("500 SERVER ERROR");
			response.print("</body>");
			response.print("</html>");
			response.pushToBrowser(500);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	//释放资源
	private void release() {
		try {
			client.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
