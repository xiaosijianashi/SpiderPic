package cn.liyong.spiderRosi;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.liyong.spider.utils.BaseSpider;


public class SpiderRosi {
	// 主页
	public static String baseUrl = "http://www.mmxyz.net/";
	// 存储路径
	public static String basefilePath = "H://ROSI";
	// 拥塞队列,存贮需要爬取的页面信息
	public final static ArrayBlockingQueue<List<String>> arrayBlockQueue = new ArrayBlockingQueue<>(100);
	// 线程池,从队列中取出爬取页面
	private final static ExecutorService threadPool = Executors.newFixedThreadPool(15);
	
	private static BaseSpider baseSU=new BaseSpider();

	public static void main(String[] args) {

		// 2.采用线程池,创建多线程处理图片页面
		for (int i = 0; i < 14; i++) {
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while (true) {
						try {
							// take(线程安全)
							List<String> url_element = arrayBlockQueue.take();
							// 解析具体的产品信息
							getPicToLocal(url_element.get(0), url_element.get(1));

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
		}

		// 1.解析分页页面的信息,将带爬取的页面的url存入阻塞队列
		ParseListPage();
	}

	
	private static void ParseListPage() {
		//分页的URL
		String Url = "http://www.mmxyz.net/?action=ajax_post&pag=";
		//分页URL拼接
		for (int i = 27; i <= 81; i++) {
			// 解析每一个分页页面的信息
			searchList(Url + i);
			System.out.println(Url + i + "  parse over-----------");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void searchList(String nextUrl) {
		// 请求重复次数
		int count = 0;
		String html = null;
		try {
			while (html == null && count < 60) {
				count++;
				html = baseSU.getHtml(nextUrl,"utf-8");
			}

			if (html != null) {
				// 解析获取的文件
				Document document = Jsoup.parse(html);
				// 解析doucument
				Elements elements = document.select("div[class=post-thumbnail] a[class=inimg]");
				// 循环读取  
				for (Element e : elements) {// 读取网站所有图片  
					// 创建连接  
					String url1 = e.attr("href");
					//页面文字转码
					String filename = new String(e.attr("title").getBytes("ISO-8859-1"), "UTF-8");
					// 将(url1,filename)存入到拥塞队列中
					// put(线程安全)
					List<String> url_element = new ArrayList<>();
					url_element.add(url1);
					url_element.add(filename);
					arrayBlockQueue.put(url_element);
				}
			} else {
				// 将nextUrl加入队列???
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	//解析存有url图片地址的页面
	private static void getPicToLocal(String nextUrl, String filename) {
		// 请求超时次数
		int count = 0;
		String html = null;
		try {
			while (html == null && count < 60) {
				count++;
				html = baseSU.getHtml(nextUrl,"utf-8");
			}

			if(html!=null) {
				// 解析获取的文件
				Document document = Jsoup.parse(html);
				// 解析doucument
				Elements elements = document.select("#gallery-1 a");
				// 循环读取  
				for (Element e : elements) {// 读取网站所有图片  
					// 创建连接  
					String pic_src = e.attr("href");
					new File(basefilePath + "/" + filename).mkdirs();
					baseSU.loadPic(basefilePath + "/" + filename, pic_src);
					Thread.sleep(200);
				}
				System.out.println(filename + " 下载完成!!!");
			}
			
		} catch (Exception e) {
			System.out.println(nextUrl);
			e.printStackTrace();
		} 

	}

	

}
