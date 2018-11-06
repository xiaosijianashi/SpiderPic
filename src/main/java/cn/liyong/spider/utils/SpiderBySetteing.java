package cn.liyong.spider.utils;

import cn.liyong.inter.ISpider;

/**
 * BaseSpider的子类，增加了重发和图片重传的机制，
 * 默认页面请求重发次数，default_request_recycling_count
 * 默认的图片重下次数，default_load_recycling_count
 * 
 */
@SuppressWarnings("all")
public class SpiderBySetteing extends BaseSpider implements ISpider{

	// 默认的请求url次数
	private int default_request_recycling_count = 60;
	// 默认的图片重下次数
	private int default_load_recycling_count = 5;
	
	// 向页面发起请求,返回html
	@Override
	public String getHtml(String nextUrl, String encode) {
		String html=null;
		int count=0;
		//请求重发
		while (html == null && count < default_request_recycling_count) {
			count++;
			html = super.getHtml(nextUrl, encode);
		}
		return html;
	}
	
	//解析存有url图片地址的页面
	@Override
	public String loadPic(String filePath, String pic_src) {
		int load_count = 0;
		String pic_path=null;
		//图片请求重发
		while(pic_path==null&&load_count<default_load_recycling_count) {
			load_count++;
			pic_path=super.loadPic(filePath, pic_src);
		}
		return pic_path;
	}

}
