package com.example.esmysql.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.esmysql.model.NBAPlayer;
import com.example.esmysql.service.NBAPlayerService;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class NBAPlayerServiceImpl implements NBAPlayerService {
    @Resource
    private RestHighLevelClient client;

    /**
     * 添加
     * @param player 实体类
     * @param id 编号
     * @return
     * @throws IOException
     */
    @Override
    public boolean addPlayer(NBAPlayer player, String id) throws IOException {
        IndexRequest request=new IndexRequest("nba_latest").id(id).source(beanToMap(player));
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(JSONObject.toJSON(response));
        return false;
    }

    /**
     * 对象转map
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> Map<String,Object> beanToMap(T bean){
        Map<String,Object> map=new HashMap<>();
        if (bean!=null){
            BeanMap beanMap= BeanMap.create(bean);
            for(Object key:beanMap.keySet()){
                if (beanMap.get(key)!=null){
                    map.put(key+"",beanMap.get(key));
                }
            }
        }
        return map;
    }
}