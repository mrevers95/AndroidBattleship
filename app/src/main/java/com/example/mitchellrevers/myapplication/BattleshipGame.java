package com.example.mitchellrevers.myapplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BattleshipGame {

    public static final int BOARD_WIDTH = 5;
    public static final int BOARD_HEIGHT = 5;
    public static final int EXPECTED_TOTAL = 5;

    private String sendingUser;
    private String receivingUser;
    private String dateCreated;
    private int gameCode = (int) (Math.random() * 1000000);
    private List<Integer> sendingBoardSetup;
    private List<Integer> receivingBoardSetup;
    private List<Integer> sendingBoardClicks;
    private List<Integer> receivingBoardClicks;
    private boolean currentTurn = true;

    public BattleshipGame() {
    }

    public BattleshipGame(String sendingUser) {
        this.sendingUser = sendingUser;
        this.receivingUser = null;
        this.sendingBoardSetup = generateBoardSetup();
        this.receivingBoardSetup = generateBoardSetup();
        this.sendingBoardClicks = generateClickedLocations();
        this.receivingBoardClicks = generateClickedLocations();
        this.dateCreated = generateDateCreated();
    }

    public String getSendingUser() {
        return sendingUser;
    }

    public String getReceivingUser() {
        return receivingUser;
    }

    public int getGameCode() {
        return gameCode;
    }

    public List<Integer> getSendingBoardSetup() {
        return sendingBoardSetup;
    }

    public List<Integer> getReceivingBoardSetup() {
        return receivingBoardSetup;
    }

    public List<Integer> getSendingBoardClicks() {
        return sendingBoardClicks;
    }

    public List<Integer> getReceivingBoardClicks() {
        return receivingBoardClicks;
    }

    public boolean getCurrentTurn() {
        return currentTurn;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setSendingUser(String sendingUser) {
        this.sendingUser = sendingUser;
    }

    public void setReceivingUser(String receivingUser) {
        this.receivingUser = receivingUser;
    }

    public void setGameCode(int gameCode) {
        this.gameCode = gameCode;
    }

    public void setSendingBoardSetup(List<Integer> sendingBoardSetup) {
        this.sendingBoardSetup = sendingBoardSetup;
    }

    public void setReceivingBoardSetup(List<Integer> receivingBoardSetup) {
        this.receivingBoardSetup = receivingBoardSetup;
    }

    public void setSendingBoardClicks(List<Integer> sendingBoardClicks) {
        this.sendingBoardClicks = sendingBoardClicks;
    }

    public void setReceivingBoardClicks(List<Integer> receivingBoardClicks) {
        this.receivingBoardClicks = receivingBoardClicks;
    }

    public void setCurrentTurn(boolean currentTurn) {
        this.currentTurn = currentTurn;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    private List<Integer> generateBoardSetup() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < BOARD_WIDTH * BOARD_HEIGHT; i++) {
            if (i < EXPECTED_TOTAL) {
                list.add(1);
            }
            else {
                list.add(0);
            }
        }
        Collections.shuffle(list);
        return list;
    }

    private List<Integer> generateClickedLocations() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < BattleshipGame.BOARD_WIDTH * BattleshipGame.BOARD_HEIGHT; i++) {
            list.add(0);
        }
        return list;
    }

    private String generateDateCreated() {
        Date now = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mma");
        return dateFormatter.format(now).toLowerCase();
    }
}
