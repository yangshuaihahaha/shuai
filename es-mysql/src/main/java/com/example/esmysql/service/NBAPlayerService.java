package com.example.esmysql.service;

import com.example.esmysql.model.NBAPlayer;

import java.io.IOException;

public interface NBAPlayerService {
    boolean addPlayer(NBAPlayer player, String id) throws IOException;
}