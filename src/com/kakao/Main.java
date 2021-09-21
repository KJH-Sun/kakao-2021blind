package com.kakao;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

    static int[] rentalOffice;
    static Truck[] truckPos;
    static int middle, N;
    static int[] moveDir;
    static Queue<Integer>[] orders;
    static Queue<Integer> lessRO = new ArrayDeque<>();


    public static void main(String[] args) {
//        JSONParser.getInstance().parseJsonFile();
        int problemId = 2;
        init(problemId);
        String response = start(problemId);
        if (response.equals("200")) {
            for (int i = 0; i < 720; i++) {
                checkLocations(); // 균일하지 않은 위치 찾기
                checkTrucks();
                JsonArray commands = makeCommand();
                simulate(commands);
            }
        }
        String score = score();
        System.out.println("시뮬레이션 결과 : " + score);

    }

    private static void init(int problemId) {
        if (problemId == 1) {
            N = 5;
            truckPos = new Truck[5];
            orders = new Queue[5];
            for (int i = 0; i < 5; i++) {
                orders[i] = new ArrayDeque<>();
            }
            middle = 2;
        } else {
            N = 60;
            truckPos = new Truck[10];
            orders = new Queue[10];
            for (int i = 0; i < 10; i++) {
                orders[i] = new ArrayDeque<>();
            }
            middle = 1;
        }
        rentalOffice = new int[N * N];
        moveDir = new int[]{1, -1, N, -N};
    }

    private static void checkTrucks() {
        JSONObject response = HttpUtil.getInstance()
            .callApi(Constants.GET_TRUCKS, new JsonObject(), "GET", false);
        if (response.has("responseCode")) {
            System.out.println("Get checkTruck 에러 발생 : " + response.getString("responseCode"));
        } else {
            JSONArray trucks = response.getJSONArray("trucks");
            for (Object jObject : trucks) {
                JSONObject truck = (JSONObject) jObject;
                int truck_id = (int) truck.get("id");
                int location_id = (int) truck.get("location_id");
                int loaded_bikes_count = (int) truck.get("loaded_bikes_count");
                truckPos[truck_id] = new Truck(truck_id, location_id, loaded_bikes_count);
            }
        }
    }

    private static JsonArray makeCommand() {
        while (!lessRO.isEmpty()) {
            int to = lessRO.poll();
            int from = findNearMaxRO(to);
            if (from == -1) {
                continue;
            }
            moveTruck(from, to);
        }
        return makeJsonCommand();
    }

    private static JsonArray makeJsonCommand() {
        Gson gson = new Gson();
        int idx = -1;
        JsonArray commands = new JsonArray();
        for (Queue<Integer> order : orders) {
            idx++;
            List<Integer> tmp = new ArrayList<>();
            if (order.isEmpty()) {
                continue;
            }
            while (!order.isEmpty()) {
                if (tmp.size() == 10) {
                    break;
                }
                tmp.add(order.poll());
            }
            Command command = new Command(idx, tmp.stream().mapToInt(i -> i).toArray());
            commands.add(gson.toJsonTree(command));
        }
        return commands;
    }

    private static void moveTruck(int from, int to) {
        Truck t = findNearTruck(from);
        if (t == null) {
            return;
        }
        orders[t.truck_id].addAll(move(t.location_id, from, to));

    }

    private static List<Integer> move(int location_id, int from, int to) {
        List<Integer> command = new ArrayList<>();
        go(location_id, from, command);
        command.add(5);
        go(from, to, command);
        command.add(6);
        return command;
    }

    private static void go(int from, int to, List<Integer> command) {
        int x = from % N - to % N;
        int y = from / N - to / N;
        while (x != 0) {
            if (x > 0) {
                command.add(3);
                x--;
            } else {
                command.add(1);
                x++;
            }
        }
        while (y != 0) {
            if (y > 0) {
                command.add(4);
                y--;
            } else {
                command.add(2);
                y++;
            }
        }
    }

    private static Truck findNearTruck(int from) {
        int min = Integer.MAX_VALUE;
        Truck nearTruck = null;
        for (Truck t : truckPos) {
            if (!orders[t.truck_id].isEmpty()) {
                continue;
            }
            int diff =
                Math.abs(t.location_id % N - from % N) + Math.abs(t.location_id / N - from / N);
            if (diff < min) {
                min = diff;
                nearTruck = t;
            }
        }
        return nearTruck;
    }


    private static int findNearMaxRO(int to) {
        Queue<Integer> que = new ArrayDeque<>();
        boolean[] check = new boolean[N * N];
        que.add(to);
        while (!que.isEmpty()) {
            int n = que.poll();
            check[n] = true;
            if (rentalOffice[n] >= rentalOffice[to] + 2) {
                return n;
            }
            for (int i = 0; i < 4; i++) {
                int nx = n + moveDir[i];
                if (0 <= nx && nx < N * N && !check[nx]) {
                    que.add(nx);
                }
            }
        }
        return -1;
    }

    private static void checkLocations() {
        JSONObject response = HttpUtil.getInstance()
            .callApi(Constants.GET_LOCATIONS, new JsonObject(), "GET", false);
        if (response.has("responseCode")) {
            System.out.println("Get Locations 에러 발생 : " + response.getString("responseCode"));
        } else {
            JSONArray locations = response.getJSONArray("locations");
            for (Object jObject : locations) {
                JSONObject location = (JSONObject) jObject;
                int id = (int) location.get("id");
                int bike_count = (int) location.get("located_bikes_count");
                rentalOffice[id] = bike_count;
                if (bike_count <= middle) {
                    lessRO.add(id);
                }
            }
        }
    }


    private static String score() {
        JSONObject response = HttpUtil.getInstance()
            .callApi(Constants.GET_SCORE, new JsonObject(), "GET", false);
        String score;
        if (response.has("responseCode")) {
            score = response.getString("responseCode");
        } else {
            score = Float.toString(response.getFloat("score"));
        }
        return score;
    }

    private static void simulate(JsonArray commands) {
        JsonObject params = new JsonObject();

        params.add("commands", commands);

        JSONObject response = HttpUtil.getInstance()
            .callApi(Constants.PUT_SIMULATE, params, "PUT", false);
        if (response.has("responseCode")) {
            String responseCode = response.getString("responseCode");
            System.out.println("시뮬레이션 실행 중 에러 발생 : " + responseCode);
        } else {
            System.out.println("현재 서버 상태 status : " + response.getString("status"));
            System.out.println("현재 서버 시각 : " + response.getInt("time"));
            System.out.println("현재 실패한 요청 수 : " + response.get("failed_requests_count"));
            System.out.println("현재 트럭 이동 거리 총합 : " + response.getString("distance"));
        }
    }

    private static String start(int problemId) {
        System.out.println(">>>> api.start()");
        String response = TokenManager.getInstance()
            .createToken(problemId);
        System.out.println("Token : " + TokenManager.getInstance().getToken());
        return response;
    }


}
