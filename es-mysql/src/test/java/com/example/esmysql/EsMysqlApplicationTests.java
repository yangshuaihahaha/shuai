package com.example.esmysql;

import com.alibaba.fastjson.JSONObject;
import com.example.esmysql.dao.NBAPlayerDao;
import com.example.esmysql.model.NBAPlayer;
import com.example.esmysql.service.NBAPlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class EsMysqlApplicationTests {

    @Autowired
    private NBAPlayerService nbaPlayerService;

    @Autowired(required = false)
    private NBAPlayerDao nbaPlayerDao;

    @Test
    public void slectAll() {
        System.out.println(JSONObject.toJSON(nbaPlayerDao.selectAll()));
    }

    @Test
    public void addPlayer() throws IOException {
        NBAPlayer nbaPlayer = new NBAPlayer();
        nbaPlayer.setId(999);
        nbaPlayer.setDisplayName("陈彦斌");
        nbaPlayerService.addPlayer(nbaPlayer, "999");
    }
}
