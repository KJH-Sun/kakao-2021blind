package com.kakao;


public class Command {

    int truck_id;
    int[] command;

    public Command(int truck_id, int[] command) {
        this.truck_id = truck_id;
        this.command = command;
    }
}
