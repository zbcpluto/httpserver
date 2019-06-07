package webServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
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
			printReg(301);
		}
		else if(requestUrl.equals("regis.html")) {
			insertedRes.add("Location:http://p.nju.edu.cn/");
			printReg(302);
		}
		else if(requestUrl.equals("register.html")) {
			printRegister();
		}
		else if(requestUrl.equals("aa.txt")) {
			printTxt();
		}
		else if(requestUrl.equals("bb.jpg")) {
			printJpg();
		}
		else {
			printError(404);
		}	
		System.out.println(response.getResponseInfo());
		release();
	}
	
	//测试状态码200和304
	private void printIndex() {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("index.html");
			byte[] datas = new byte[1024*1024];
			int len = is.read(datas);
			response.print((new String(datas, 0, len)));
			Date date = request.getLastModifiedTime();
			if(date != null && new Date().getTime() - date.getTime() < 60000){
				System.out.println("缓存有效");
				response.pushToBrowser(304);
			}
			else{
				response.pushToBrowser(200);
			}

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
	
	private void printReg(int code) {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(code+"Redirect.html");
			byte[] datas = new byte[1024*1024];
			int len = is.read(datas);
			response.print((new String(datas, 0, len)));
			response.pushToBrowser(code, insertedRes);
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
	
	private void printTxt() {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("aa.txt");
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
	
	private void printJpg() {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bb.jpg");
			byte[] datas = new byte[1024*1024*10];
			int len = is.read(datas);
			Encoder encoder = Base64.getEncoder();
			response.print("<img src=\"data:image/jpg;base64,");
			response.print(encoder.encodeToString(datas));
			response.print("\"/>");
			response.pushToBrowser(200);
			is.close();
		}
		catch (IOException e) {
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
