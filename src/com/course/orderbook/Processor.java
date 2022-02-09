package com.course.orderbook;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

class Processor {
    private static final String UNEXPECTED_VALUE = "Unexpected value: ";

    private final TreeMap<Integer, Integer> bidTreeMap = new TreeMap<>();
    private final TreeMap<Integer, Integer> askTreeMap = new TreeMap<>();

    public TreeMap<Integer, Integer> getBidTreeMap() {
        return bidTreeMap;
    }

    public TreeMap<Integer, Integer> getAskTreeMap() {
        return askTreeMap;
    }

    void process(Stream<String> source, Consumer<String> drain) {
        source.flatMap(this::processLine).forEach(drain);
    }

    Stream<String> processLine(String s) {
        String res = null;
        if (s.length() > 0) {
            String[] values = s.split(",");//getting values of price, quantity, type of order
            switch (values[0]) {
                case "q" -> res = processQuery(values);
                case "u" -> processUpdate(values);//getting values of price, quantity, type of order
                case "o" -> processOrder(values);
                default -> throw new IllegalStateException(UNEXPECTED_VALUE + values[0]);
            }
        }
        return Stream.ofNullable(res);
    }

    public String processQuery(String[] values) {
        String output = null;
        int price;
        int quantity;
        Map.Entry<Integer, Integer> entry;
        if (values.length == 2) {
            switch (values[1]) {
                case "best_bid" -> entry = bidTreeMap.lastEntry();
                case "best_ask" -> entry = askTreeMap.firstEntry();
                default -> throw new IllegalStateException(UNEXPECTED_VALUE + values[1]);
            }
            if (Objects.nonNull(entry)) {
                price = entry.getKey();
                quantity = entry.getValue();

                output = "%d,%d".formatted(price, quantity);
            }
        } else {
            price = Integer.parseInt(values[2]);
            if (bidTreeMap.containsKey(price)) {
                output = bidTreeMap.get(price).toString();
            } else if (askTreeMap.containsKey(price)) {
                output = (askTreeMap.get(price).toString());
            } else {
                output = "0";
            }
        }
        return output;
    }

    public void processUpdate(String[] values) {
        int price = Integer.parseInt(values[1]);
        int quantity = Integer.parseInt(values[2]);
        if ("bid".equals(values[3])) {
            if (quantity == 0) {
                bidTreeMap.remove(price);
            } else {
                bidTreeMap.put(price, quantity);
            }
        } else if ("ask".equals(values[3])) {
            if (quantity == 0) {
                askTreeMap.remove(price);
            } else {
                askTreeMap.put(price, quantity);
            }
        } else {
            throw new IllegalStateException(UNEXPECTED_VALUE + values[1]);
        }
    }

    public void processOrder(String[] values) {
        int amount = Integer.parseInt(values[2]);
        if ("buy".equals(values[1])) {
            buy(amount);
        } else if ("sell".equals(values[1])) {
            sell(amount);
        } else {
            throw new IllegalStateException(UNEXPECTED_VALUE + values[1]);
        }
    }

    void buy(int amount) {
        while (!askTreeMap.isEmpty() && amount > 0) {
            amount -= askTreeMap.firstEntry().getValue();
            if (amount >= 0) {
                askTreeMap.remove(askTreeMap.firstKey());
            } else {
                askTreeMap.put(askTreeMap.firstKey(), (amount * -1));
            }
        }
    }

    void sell(int amount) {
        while (!bidTreeMap.isEmpty() && amount > 0) {
            amount -= bidTreeMap.lastEntry().getValue();
            if (amount >= 0) {
                bidTreeMap.remove(bidTreeMap.lastKey());
            } else {
                bidTreeMap.put(bidTreeMap.lastKey(), (amount * -1));
            }
        }
    }
}
