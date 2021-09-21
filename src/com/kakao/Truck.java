package com.kakao;

public class Truck {

    int truck_id;
    int location_id;
    int loaded_bikes_count;

    public Truck(int truck_id, int location_id, int loaded_bikes_count) {
        this.truck_id = truck_id;
        this.location_id = location_id;
        this.loaded_bikes_count = loaded_bikes_count;
    }
}
