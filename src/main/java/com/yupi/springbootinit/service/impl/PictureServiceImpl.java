package com.yupi.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.service.PictureService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图片服务实现类
 */
@Service
public class PictureServiceImpl implements PictureService {
    @Override
    public Page<Picture> searchPicture(String searchText, long pageNum, long pageSize) {
        long current=(pageNum-1)*pageSize;
        List<Picture> pictureList = new ArrayList<>();
        String url=String.format("https://cn.bing.com/images/search?q=%s&form=HDRSC2&cw=1177&ch=759&first=%s",searchText,current);
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
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
            if(pictureList.size()>=pageSize)break;
        }
        Page<Picture>picturePage=new Page<>();
        picturePage.setRecords(pictureList);
        return picturePage;
    }
}
