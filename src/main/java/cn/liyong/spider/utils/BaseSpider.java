package cn.liyong.spider.utils;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import cn.liyong.inter.ISpider;

/**
 * ISpider的实现类，没有做重发和重传的机制
 */
//@SuppressWarnings("all")
public class BaseSpider implements ISpider {

	//默认stream请求超时 时间
	private int default_connect_timeout = 10000;
	//默认stream读取超时 时间
	private int default_stream_read_timeout = 10000;
	//默认stream 缓存
	private int default_buffer = 10240;
	//默认睡眠 时间 
	private int default_sleepTime = 1000;
	
	// 向页面发起请求,返回html
	@Override
	public String getHtml(String nextUrl, String encode) {
		CloseableHttpClient httpClient;
		HttpGet httpGet;
		CloseableHttpResponse response;
		String html = null;
		try {
			// 1.httpclient
			httpClient = HttpClients.createDefault();
			// 2.get请求头拼接
			httpGet = new HttpGet(nextUrl);
			httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) "
					+ "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
			httpGet.setHeader("Accept", "*/*");
			// 3.接收响应
			response = httpClient.execute(httpGet);
			if (200 == response.getStatusLine().getStatusCode()) {
				// 4.获得首页的信息，String类型
				html = EntityUtils.toString(response.getEntity(), Charset.forName(encode));
			}
			// 离线爬取，需要设置UTF-8
			// String html = EntityUtils.toString(response.getEntity(), "UTF-8");
			return html;
		} catch (Exception e) {
			// 暂时注释掉
			// System.out.println("getHtml 异常爬取地址：" + nextUrl+",异常状态码:"+code);
			// e.printStackTrace();
			try {
				Thread.sleep(default_sleepTime);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return null;
		}
	}

	// 下载图片存于本地
	@Override
	public String loadPic(String filePath, String pic_src) {

		String pic_path=null;
		OutputStream outputStream = null;
		InputStream inputStream = null;
		BufferedInputStream bis = null;
		try {
			// 1.封装图片url
			URL imgUrl = new URL(pic_src);
			String pic_name = pic_src.substring(pic_src.lastIndexOf("/") + 1, pic_src.length());
			// 2.创建URLConnection
			HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
			// 3.设置请求头
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) "
					+ "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");

			conn.setRequestProperty("Accept",
					"image/jpg, image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
							+ "application/x-shockwave-flash, application/xaml+xml, "
							+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
							+ "application/x-ms-application, application/vnd.ms-excel, "
							+ "application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setConnectTimeout(default_connect_timeout);
			conn.setReadTimeout(default_stream_read_timeout);

			// 4.获取输入流
			inputStream = conn.getInputStream();
			// 5.将输入流信息放入缓冲流提升读写速度  
			bis = new BufferedInputStream(inputStream);
			// 6.读取字节娄  
			byte[] buf = new byte[default_buffer];
			// 7.生成文件  
			String filedir = filePath + "/" + pic_name;
			outputStream = new FileOutputStream(filedir);
			int size = 0;
			// 8.边读边写  
			while ((size = bis.read(buf)) != -1) {
				outputStream.write(buf, 0, size);
			}
			// 9.刷新文件流  
			outputStream.flush();
			pic_path=filedir;
			return pic_path;
		} catch (Exception ex) {
			System.out.println(pic_src + " 下载发生异常!!!!");
			ex.printStackTrace();
			return pic_path;

		} finally {
			releaseStream(outputStream, bis, inputStream);
		}
	}

	/**
	 * 关闭流
	 * @param os
	 * @param bis
	 * @param is
	 */
	public void releaseStream(OutputStream os, BufferedInputStream bis, InputStream is) {
		try {
			if (os != null) {
				os.close();
			}
			if (os != null) {
				bis.close();
			}
			if (is != null) {
				is.close();
			}
		} catch (Exception ex) {

		}

	}
	
}
