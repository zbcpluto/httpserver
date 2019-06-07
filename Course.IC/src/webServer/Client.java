package webServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	private final static String BLANK = " ";
	private final static String CRLF = "\r\n";

	public static void main(String[] args) throws UnknownHostException, IOException {
		@SuppressWarnings("resource")
		Socket client = new Socket(InetAddress.getLocalHost(), 8888);
		String requestInfo = getRequestInfo();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		bw.append(requestInfo);
		bw.flush();
		
		System.out.print("响应报文：");
		BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		String line = "";
		while(line != null) {
			System.out.println(line);
			line = br.readLine();
		}
		
		bw.close();
		br.close();
	}

	private static String getRequestInfo() {
		StringBuilder sb = new StringBuilder();
		
		Scanner sc = new Scanner(System.in);
		System.out.print("请输入请求方式：");
		String method = sc.nextLine();
		System.out.print("请输入请求url：");
		String url = sc.nextLine();
		sc.close();
		
		//模拟火狐浏览器的请求头
		sb.append(method.toUpperCase()).append(BLANK).append("/").append(url).append(BLANK);
		sb.append("HTTP/1.1").append(CRLF);
		sb.append("HOST: localhost:8888").append(CRLF);
		sb.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8").append(CRLF);
		sb.append("Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2").append(CRLF);
		sb.append("Accept-Encoding: gzip, deflate").append(CRLF);
		sb.append("Connection: keep-alive").append(CRLF);
		
		return sb.toString();
	}
}
