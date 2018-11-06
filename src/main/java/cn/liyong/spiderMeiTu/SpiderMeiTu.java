package cn.liyong.spiderMeiTu;

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

import cn.liyong.inter.ISpider;
//import cn.liyong.spider.utils.BaseSpider;
import cn.liyong.spider.utils.SpiderBySetteing;

public class SpiderMeiTu {
	// 主页
	public static String Url = "http://www.mmxyz.net/?action=ajax_post&pag=";
	// 存储路径
	public static String basefilePath = "F:/test";
	// 拥塞队列,存贮需要爬取的页面信息
	public final static ArrayBlockingQueue<List<String>> arrayBlockQueue = new ArrayBlockingQueue<>(100);
	// 线程池,从队列中取出爬取页面
	private final static ExecutorService threadPool = Executors.newFixedThreadPool(15);

	//加入重发机制
	private static ISpider spider = new SpiderBySetteing();
	//未加入重发机制
	//private static ISpider spider = new BaseSpider();
	
	public static void main(String[] args) {
		
		// 1.创建存储路径
		File f=new File(basefilePath);
		//部署到linux中需要設置setWritable
		f.setWritable(true,false);
		if(!f.exists()) {
			f.mkdirs();
		}

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
							// 从拥塞队列取出页面url
							getPicToLocal(url_element.get(0), url_element.get(1));

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
		}

		// 3.解析分页页面的信息,将带爬取的页面的url存入阻塞队列
		ParseListPage();
	}

	/**
	 * 拼接分页页面
	 */
	private static void ParseListPage() {
		// 分页URL拼接,待爬取的网站有81个分页
		for (int i = 1; i <= 81; i++) {
			// 解析每一个分页页面的信息
			searchList(Url + i);
			System.out.println(Url + i + "  parse over-----------");
			try {
				//解析一个页面睡1秒
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * 解析每一个分页页面的信息
	 * @param nextUrl：分页Url
	 */
	private static void searchList(String nextUrl) {
		String html = null;
		try {

			html = spider.getHtml(nextUrl, "utf-8");

			if (html != null) {
				// 解析获取的文件
				Document document = Jsoup.parse(html);
				// 解析doucument
				Elements elements = document.select("div[class=post-thumbnail] a[class=inimg]");
				// 循环读取  
				for (Element e : elements) {// 读取网站所有图片  
					// 创建连接  
					String url1 = e.attr("href");
					// 页面文字转码
					String filename = new String(e.attr("title").getBytes("UTF-8"), "UTF-8");
					// 将(url1,filename)装入List,存入到拥塞队列中
					// put(线程安全)
					List<String> url_element = new ArrayList<>();
					url_element.add(url1);
					url_element.add(filename);
					arrayBlockQueue.put(url_element);
				}
			} else {
				// 解析失败将nextUrl加入队列？？？
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析存有图片Url信息的页面
	 * @param nextUrl：存有图片Url信息的页面地址
	 * @param filename：图片文件夹名称
	 */
	private static void getPicToLocal(String nextUrl, String filename) {
		String html = null;
		try {

			html = spider.getHtml(nextUrl, "utf-8");

			if (html != null) {
				// 解析获取的文件
				Document document = Jsoup.parse(html);
				// 解析doucument
				Elements elements = document.select("#gallery-1 a");
				// 循环读取  
				for (Element e : elements) {// 读取网站所有图片  
					String pic_src = e.attr("href");
					new File(basefilePath + "/" + filename).mkdirs();
					//下载图片Url
					spider.loadPic(basefilePath + "/" + filename, pic_src);	
				}
				System.out.println(filename + " 下载完成!!!");
			}

		} catch (Exception e) {
			System.out.println(nextUrl);
			e.printStackTrace();
		}

	}

}
