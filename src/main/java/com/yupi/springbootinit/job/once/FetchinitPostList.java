package com.yupi.springbootinit.job.once;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.esdao.PostEsDao;
import com.yupi.springbootinit.model.dto.post.PostEsDTO;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获取初始列表帖子
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
// 取消注释后，每次运行SpringBoot都会执行一次run方法
@Component
@Slf4j
public class FetchinitPostList implements CommandLineRunner {

    @Resource
    private PostService postService;

    @Override
    public void run(String... args) {
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
        if(b){
            log.info("获取帖子成功"+postList.size());
        }else{
            log.error("获取帖子失败");
        }
    }
}
