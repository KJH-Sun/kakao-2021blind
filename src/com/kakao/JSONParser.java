package com.kakao;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class JSONParser {

    private static JSONParser instance = null;

    private JSONParser() {
    }

    public static JSONParser getInstance() {
        return instance == null ? new JSONParser() : instance;
    }

    public JsonObject parseJsonFile() {
        int[] requestCnt = new int[3600];
        int[] responseCnt = new int[3600];
        int maxRequestIdx = 0;
        int maxRequest = Integer.MIN_VALUE;
        int maxResponseIdx = 0;
        int maxResponse = Integer.MIN_VALUE;
        JsonObject obj = null;
        try {
            Reader reader = new FileReader("./res/problem2_day-3.json");
            Gson gson = new Gson();
            obj = gson.fromJson(reader, JsonObject.class);
            for (int i = 0; i < 720; i++) {
                String num = String.valueOf(i);
                JsonArray requestList = (JsonArray) obj.get(num);
                for (Object request : requestList) {
                    JsonArray rent = (JsonArray) request;
                    requestCnt[rent.get(0).getAsInt()]++;
                    responseCnt[rent.get(1).getAsInt()]++;
                }
            }

            for (int i = 0; i < 3600; i++) {
                if (requestCnt[i] > maxRequest) {
                    maxRequest = requestCnt[i];
                    maxRequestIdx = i;
                }
                if (responseCnt[i] > maxResponse) {
                    maxResponse = requestCnt[i];
                    maxResponseIdx = i;
                }
            }
            System.out
                .println("maxRequestIdx : " + maxRequestIdx + "maxResponseIdx : " + maxResponseIdx);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

}
