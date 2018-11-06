package cn.liyong.spiderMeiTu.distribute;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import cn.liyong.inter.ISpider;
import cn.liyong.spider.utils.SpiderBySetteing;
import redis.clients.jedis.Jedis;


/**
 * master:解析分页页面的信息,将带爬取的页面的url存入Redis的list集合
 * @author liyong
 *
 */
public class Spider_Master {

	private static Jedis jedis = null;
	
	// 分页的URL
	private	static String Url = "http://www.mmxyz.net/?action=ajax_post&pag=";

	private static ISpider baseSU = new SpiderBySetteing();
	

	static {
		jedis = new Jedis("192.168.77.130", 6379);
		jedis.auth("admin");
	}

	public static void main(String[] args) {
		// 1.解析分页页面的信息,将带爬取的页面的url存入阻塞队列
		ParseListPage();
	}

	/**
	 * 拼接分页页面
	 */
	private static void ParseListPage() {
		// 分页URL拼接
		for (int i = 1; i <= 81; i++) {
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

	/**
	 * 解析每一个分页页面的信息，将带爬取的页面的url存入Redis的list集合
	 * @param nextUrl：分页Url
	 */
	private static void searchList(String nextUrl) {
		String html = null;
		try {

			html = baseSU.getHtml(nextUrl, "utf-8");

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
					// 将(url1,filename)存入到拥塞队列中
					List<String> url_element = new ArrayList<>();
					url_element.add(url1);
					url_element.add(filename);
					Gson gjson = new Gson();
					String json_list = gjson.toJson(url_element);
					// 将将(url1,filename)以json格式存入Redis
					jedis.lpush("liyong:spider:pic", json_list);
				}
			} else {
				// 将nextUrl加入队列???
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
