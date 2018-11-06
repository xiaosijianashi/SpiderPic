package cn.liyong.spiderMeiTu.distribute;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import cn.liyong.inter.ISpider;
import cn.liyong.spider.utils.SpiderBySetteing;
import redis.clients.jedis.Jedis;

public class Spider_Slave {

	// 存储路径
	public static String basefilePath = "H://ROSI";
	// 10个容量的线程池
	private final static ExecutorService threadPool = Executors.newFixedThreadPool(10);

	// 加入重发机制
	private static ISpider baseSU = new SpiderBySetteing();
	// 未加入重发机制
	// private static ISpider baseSU = new BaseSpider();

	public static void main(String[] args) {

		// 1.创建存储路径
		File f = new File(basefilePath);
		// 部署到linux中需要設置setWritable
		f.setWritable(true, false);
		if (!f.exists()) {
			f.mkdirs();
		}

		// 2.采用线程池,创建多线程处理图片页面
		for (int i = 0; i < 10; i++) {
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Jedis jedis = new Jedis("192.168.77.130", 6379);
					jedis.auth("admin");
					while (true) {
						try {
							// take(线程安全)
							// List<String> url_element = arrayBlockQueue.take();
							// take(线程安全)
							String json_list = jedis.rpop("liyong:spider:pic");
							if (json_list != null) {
								Gson gson = new Gson();
								List<String> url_element = gson.fromJson(json_list, List.class);
								// 解析具体的产品信息
								getPicToLocal(url_element.get(0), url_element.get(1));

							}

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
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

			html = baseSU.getHtml(nextUrl, "utf-8");

			if (html != null) {
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
				}
				System.out.println(filename + " 下载完成!!!");
			}

		} catch (Exception e) {
			System.out.println(nextUrl);
			e.printStackTrace();
		}

	}

}
