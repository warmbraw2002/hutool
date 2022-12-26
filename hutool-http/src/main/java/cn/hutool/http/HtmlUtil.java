package cn.hutool.http;

import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HTMLFilter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * HTML工具类
 *
 * <p>
 * 比如我们在使用爬虫爬取HTML页面后，需要对返回页面的HTML内容做一定处理，<br>
 * 比如去掉指定标签（例如广告栏等）、去除JS、去掉样式等等，这些操作都可以使用此工具类完成。
 *
 * @author xiaoleilu
 *
 */
public class HtmlUtil {

  public static final String NBSP = StrUtil.HTML_NBSP;
  public static final String AMP = StrUtil.HTML_AMP;
  public static final String QUOTE = StrUtil.HTML_QUOTE;
  public static final String APOS = StrUtil.HTML_APOS;
  public static final String LT = StrUtil.HTML_LT;
  public static final String GT = StrUtil.HTML_GT;

  public static final String RE_HTML_MARK = "(<[^<]*?>)|(<[\\s]*?/[^<]*?>)|(<[^<]*?/[\\s]*?>)";
  public static final String RE_SCRIPT = "<[\\s]*?script[^>]*?>.*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";

  /**
   * 在HTML中注释的内容 正则
   */
  public static final String COMMENT_REGEX = "(?s)<!--.+?-->";

  /**
   * 在HTML中无效的字符 正则
   */
  public static final String INVALID_REGEX = "[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]";

  /**
   * HTML格式化输出默认缩进量
   */
  public static final int INDENT_DEFAULT = 2;

  private static final char[][] TEXT = new char[64][];

  static {
    for (int i = 0; i < 64; i++) {
      TEXT[i] = new char[] { (char) i };
    }

    // special HTML characters
    TEXT['\''] = "&#039;".toCharArray(); // 单引号 ('&apos;' doesn't work - it is not by the w3 specs)
    TEXT['"'] = QUOTE.toCharArray(); // 单引号
    TEXT['&'] = AMP.toCharArray(); // &符
    TEXT['<'] = LT.toCharArray(); // 小于号
    TEXT['>'] = GT.toCharArray(); // 大于号
  }

  /**
   * 转义文本中的HTML字符为安全的字符，以下字符被转义：
   * <ul>
   * <li>' 替换为 &amp;#039; (&amp;apos; doesn't work in HTML4)</li>
   * <li>" 替换为 &amp;quot;</li>
   * <li>&amp; 替换为 &amp;amp;</li>
   * <li>&lt; 替换为 &amp;lt;</li>
   * <li>&gt; 替换为 &amp;gt;</li>
   * </ul>
   *
   * @param text 被转义的文本
   * @return 转义后的文本
   */
  public static String escape(String text) {
    return encode(text);
  }

  /**
   * 还原被转义的HTML特殊字符
   *
   * @param htmlStr 包含转义符的HTML内容
   * @return 转换后的字符串
   */
  public static String unescape(String htmlStr) {
    if (StrUtil.isBlank(htmlStr)) {
      return htmlStr;
    }

    return EscapeUtil.unescapeHtml4(htmlStr);
  }

  // ---------------------------------------------------------------- encode text

  /**
   * 清除所有HTML标签，但是不删除标签内的内容
   *
   * @param content 文本
   * @return 清除标签后的文本
   */
  public static String cleanHtmlTag(String content) {
    return content.replaceAll(RE_HTML_MARK, "");
  }

  /**
   * 清除指定HTML标签和被标签包围的内容<br>
   * 不区分大小写
   *
   * @param content 文本
   * @param tagNames 要清除的标签
   * @return 去除标签后的文本
   */
  public static String removeHtmlTag(String content, String... tagNames) {
    return removeHtmlTag(content, true, tagNames);
  }

  /**
   * 读取html文件转化为String
   * @param fileName 文件名
   * @return 读取后的字符串
   */
  public static String readHtml(String fileName){

    String str="";

    File file=new File(fileName);

    try {

      FileInputStream in=new FileInputStream(file);

      // size  为字串的长度 ，这里一次性读完

      int size=in.available();

      byte[] buffer=new byte[size];

      in.read(buffer);

      in.close();

      str=new String(buffer, StandardCharsets.UTF_8);

    } catch (IOException e) {

      e.printStackTrace();

    }
    return str;
  }

  /**
   * 将字符串写入HTML文件中
   *
   * @param str 写入的字符串
   * @param filename 需要写入的文件名
   */
  public static void writeHtml(String str,String filename){
    try {
      File file = new File(filename);

      PrintStream ps = new PrintStream(new FileOutputStream(file));

      ps.append(str);// 在已有的基础上添加字符串

      ps.close();
    } catch (FileNotFoundException e) {

      e.printStackTrace();
    }
  }

  /**
   * 将目录下的html文件转换为jsp文件
   *
   * @param file 需要转换的目录
   */
  public static void change2jsp(File file) throws IOException {
    File[] files = file.listFiles();
    for (File a : files) {
      if (a.isDirectory()) {
        change2jsp(a);
      }
      html2jsp(a);
    }
  }

  /**
   * 将html文件转换成jsp文件，并转换成utf-8字符集
   *
   * @param file 需要转换的文件
   */
  public static void html2jsp(File file) throws IOException{
    String name = file.getName();
    //获取文件名，文件名以html结尾的进入if分支
    if (name.endsWith(".html")) {
      //在相同的目录下创建一个文件名相同的jsp文件
      File tempFile = new File(file.getAbsolutePath().replace(".html", ".jsp"));
      //copy文件  将html文件内容copy到jsp中
      InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
      FileOutputStream outFile = new FileOutputStream(tempFile);
      OutputStreamWriter ow = new OutputStreamWriter(outFile, "UTF-8");
      //添加utf-8字符集
      String s = "<%@page pageEncoding=\"UTF-8\" contentType=\"text/html; charset=UTF-8\" %>\r\n";
      ow.write(s, 0, s.length());
      //copy内容
      char[] buffer = new char[1024];
      int i = 0;
      while ((i = isr.read(buffer)) != -1) {
        ow.write(buffer, 0, i);
      }
      //关闭流
      ow.close();
      isr.close();
      outFile.close();
      // 复制完成删除htnl文件
      file.delete();
    }
  }

  /**
   * 获取网页的链接
   * @param filename 文件名
   * @return 链接集合
   */
  public static List<String> getLinks(String filename){

    File input = new File(filename);

    Document doc = null;
    try {
      doc = Jsoup.parse(input,"UTF-8","网址/");
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<String>links = new ArrayList<>();
    Elements href = doc.select("a");
    for (Element element : href) {
      links.add(element.attr("href"));
    }
    Elements iframe = doc.select("iframe");
    for (Element element : iframe) {
      links.add(element.attr("src"));
    }
    Elements link = doc.select("link");
    for (Element element : link) {
      links.add(element.attr("href"));
    }
    Elements script = doc.select("script");
    for (Element element : script) {
      links.add(element.attr("src"));
    }
    return links;
  }

  /**
   * 获取外部链接
   * @param strings 网页的全部链接
   * @return 符合条件的链接集合
   */
  public static List<String> filterOutLinks(List<String> strings) {
    List<String> outLinks = new ArrayList<>();
    for (String link : strings) {
      if (link.startsWith("http")||link.startsWith("https")){
        outLinks.add(link);
      }
    }
    return outLinks;
  }

  /**
   * 获取符合条件的链接
   * @param links 网页的全部链接
   * @param isOutLinks  true：是获取外链接 ；false:是获取内链接
   * @param patterns 链接的过滤条件，不传表示不用过滤
   * @return 符合条件的链接集合
   */
  public static List<String> filterLinks(List<String>links, boolean isOutLinks, String... patterns){
    List<String> result = new ArrayList<>();
    if(isOutLinks){
      result = filterOutLinks(links);
    }else {
      for (String link : links) {
        if (!link.startsWith("http")&&!link.startsWith("https")){
          result.add(link);
        }
      }
    }
    if(patterns!=null&&patterns.length>0){
      Iterator<String> iterator = result.iterator();
      while (iterator.hasNext()){
        for (String reg : patterns) {
          //只要是不符合传入正则表达式规则的都移除
          if(!isPatternlink(iterator.next(),reg)){
            iterator.remove();
          }
        }
      }
    }
    return result;
  }

  /**
   * 通过正则表达式获取匹配的Link
   * @param link 目标链接
   * @param reg  正则表达式
   * @return true 匹配
   */
  public static boolean isPatternlink(String link,String reg){
    Pattern pattern = Pattern.compile(reg);
    Matcher matcher = pattern.matcher(link);
    return matcher.matches();
  }

  /**
   * 清除指定HTML标签，不包括内容<br>
   * 不区分大小写
   *
   * @param content 文本
   * @param tagNames 要清除的标签
   * @return 去除标签后的文本
   */
  public static String unwrapHtmlTag(String content, String... tagNames) {
    return removeHtmlTag(content, false, tagNames);
  }

  /**
   * 清除指定HTML标签<br>
   * 不区分大小写
   *
   * @param content 文本
   * @param withTagContent 是否去掉被包含在标签中的内容
   * @param tagNames 要清除的标签
   * @return 去除标签后的文本
   */
  public static String removeHtmlTag(String content, boolean withTagContent, String... tagNames) {
    String regex;
    for (String tagName : tagNames) {
      if (StrUtil.isBlank(tagName)) {
        continue;
      }
      tagName = tagName.trim();
      // (?i)表示其后面的表达式忽略大小写
      if (withTagContent) {
        // 标签及其包含内容
        regex = StrUtil.format("(?i)<{}(\\s+[^>]*?)?/?>(.*?</{}>)?", tagName, tagName);
      } else {
        // 标签不包含内容
        regex = StrUtil.format("(?i)<{}(\\s+[^>]*?)?/?>|</?{}>", tagName, tagName);
      }

      content = ReUtil.delAll(regex, content); // 非自闭标签小写
    }
    return content;
  }

  /**
   * 去除HTML标签中的属性，如果多个标签有相同属性，都去除
   *
   * @param content 文本
   * @param attrs 属性名（不区分大小写）
   * @return 处理后的文本
   */
  public static String removeHtmlAttr(String content, String... attrs) {
    String regex;
    for (String attr : attrs) {
      // (?i)     表示忽略大小写
      // \s*      属性名前后的空白符去除
      // [^>]+?   属性值，至少有一个非>的字符，>表示标签结束
      // \s+(?=>) 表示属性值后跟空格加>，即末尾的属性，此时去掉空格
      // (?=\s|>) 表示属性值后跟空格（属性后还有别的属性）或者跟>（最后一个属性）
      regex = StrUtil.format("(?i)(\\s*{}\\s*=[^>]+?\\s+(?=>))|(\\s*{}\\s*=[^>]+?(?=\\s|>))", attr, attr);
      content = content.replaceAll(regex, StrUtil.EMPTY);
    }
    return content;
  }

  /**
   * 去除指定标签的所有属性
   *
   * @param content 内容
   * @param tagNames 指定标签
   * @return 处理后的文本
   */
  public static String removeAllHtmlAttr(String content, String... tagNames) {
    String regex;
    for (String tagName : tagNames) {
      regex = StrUtil.format("(?i)<{}[^>]*?>", tagName);
      content = content.replaceAll(regex, StrUtil.format("<{}>", tagName));
    }
    return content;
  }

  /**
   * 去除HTML文本中的注释内容
   *
   * @param htmlContent XML文本
   * @return 当传入为null时返回null
   * @since 5.4.5
   */
  public static String cleanComment(String htmlContent) {
    if (htmlContent == null) {
      return null;
    }
    return htmlContent.replaceAll(COMMENT_REGEX, StrUtil.EMPTY);
  }


  /**
   * Encoder
   *
   * @param text 被编码的文本
   * @return 编码后的字符
   */
  private static String encode(String text) {
    int len;
    if ((text == null) || ((len = text.length()) == 0)) {
      return StrUtil.EMPTY;
    }
    StringBuilder buffer = new StringBuilder(len + (len >> 2));
    char c;
    for (int i = 0; i < len; i++) {
      c = text.charAt(i);
      if (c < 64) {
        buffer.append(TEXT[c]);
      } else {
        buffer.append(c);
      }
    }
    return buffer.toString();
  }

  /**
   * 过滤HTML文本，防止XSS攻击
   *
   * @param htmlContent HTML内容
   * @return 过滤后的内容
   */
  public static String filter(String htmlContent) {
    return new HTMLFilter().filter(htmlContent);
  }
}
