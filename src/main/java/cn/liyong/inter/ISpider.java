package cn.liyong.inter;

/**
 * 定义了爬虫最基本的两个方法
 * @author liyong
 *
 */
public interface ISpider {
	/**
	 * 请求页面
	 * @param nextUrl: 请求页面的url
	 * @param encode: 解析的编码格式
	 * @return response页面的字符串
	 */
	public  String getHtml(String nextUrl, String encode);
	/**
	 * 图片下载
	 * @param filePath 图片的存贮路径
	 * @param pic_src 图片的网络url
	 * @return
	 */
	public String loadPic(String filePath, String pic_src);
}
