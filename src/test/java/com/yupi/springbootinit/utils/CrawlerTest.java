package com.yupi.springbootinit.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.service.PostService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class CrawlerTest {//数据抓取测试
    @Resource
    private PostService postService;
    @Test
    void testFetchPicture() throws IOException {//第二种方法：当网络请求中的数据看不懂的时候，通过前端页面的渲染找到元素的位置获取
        int current=1;
        List<Picture> pictureList = new ArrayList<>();
        String url="https://cn.bing.com/images/search?q=哈尔的移动城堡&form=HDRSC2&cw=1177&ch=759&first="+current;
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".iuscp.isv");
        for (Element element : elements) {
            String m = element.select(".iusc").get(0).attr("m");
            Map<String,Object> map= JSONUtil.toBean(m,Map.class);
            String murl =(String) map.get("murl");
//            System.out.println(murl);
            String title = element.select(".inflnk").get(0).attr("aria-label");
//            System.out.println(title);
            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictureList.add(picture);
        }
        System.out.println(pictureList);
//        Elements newsHeadlines = doc.select("#mp-itn b a");
//        for (Element headline : newsHeadlines) {
//        }
    }
    @Test
    void testFetchPassage(){//第一种方法：通过网络请求查询预览以及网址获得数据
        //获取数据
        String json="{\"pageSize\":12,\"sortOrder\":\"descend\",\"sortField\":\"_score\",\"tags\":[],\"searchText\":\"学习路线\",\"current\":1,\"reviewStatus\":1,\"hiddenContent\":true,\"type\":\"all\"}";
        String url="https://api.codefather.cn/api/search/";
        String result= HttpRequest
                .post(url)
                .body(json)
                .execute()
                .body();
//        System.out.println(result);
        //json转对象
        Map<String,Object> map= JSONUtil.toBean(result,Map.class);
        JSONObject data = (JSONObject) map.get("data");
        JSONObject searchPage =(JSONObject) data.get("searchPage");
        JSONArray records = (JSONArray) searchPage.get("records");
        ArrayList<Post> postList = new ArrayList<>();
        for (Object record : records) {
            JSONObject tempRecord = (JSONObject) record;//临时数据，在内部进行强转
            Post post = new Post();
            post.setTitle(tempRecord.getStr("title"));
            post.setContent(tempRecord.getStr("content"));
            JSONArray tags = (JSONArray) tempRecord.get("tags");
            List<String> tagsList = tags.toList(String.class);
            post.setTags(JSONUtil.toJsonStr(tagsList));
            post.setUserId(1L);
            postList.add(post);
        }
        System.out.println(postList);
        //插入数据库
        boolean b = postService.saveBatch(postList);
        Assertions.assertTrue(b);
    }
}
