package com.course.orderbook;

import java.util.AbstractMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

class Processor {
    private final TreeMap<Integer, Integer> bidTreeMap = new TreeMap<>();
    private final TreeMap<Integer, Integer> askTreeMap = new TreeMap<>();

    public TreeMap<Integer, Integer> getBidTreeMap() {
        return bidTreeMap;
    }

    public TreeMap<Integer, Integer> getAskTreeMap() {
        return askTreeMap;
    }

    void process(Supplier<String> source,
                 Consumer<String> drain) {
        String s;
        while ((s = source.get()) != null) {
            processLine(s).forEach(drain);
        }
    }

    void process(Stream<String> source, Consumer<String> drain) {
        try {
            source.flatMap(this::processLine).forEach(drain);
        } catch (Exception e) {
        }
    }

    Stream<String> processLine(String s) {
        String res = null;
        if (s.length() > 0) {
            String[] values = s.split(",");//getting values of price, quantity, type of order
            switch (s.charAt(0)) {
                case 'q' -> res = processQuery(values);
                case 'u' -> processUpdate(values);//getting values of price, quantity, type of order
                case 'o' -> processOrder(values);
                default -> System.out.println("No such option");
            }
        }
        return Stream.ofNullable(res);
    }

    public String processQuery(String[] values) {
        String output = "";
        Integer price, quantity;
        Map.Entry<Integer, Integer> entry;
        if (values.length == 2) {
            switch (values[1]) {
                case "best_bid" -> {
                    entry = bidTreeMap.lastEntry();
                    price = entry.getKey();
                    quantity = entry.getValue();
                    output = "%d,%d".formatted(price, quantity);
                }
                case "best_ask" -> {
                    entry = askTreeMap.firstEntry();
                    price = entry.getKey();
                    quantity = entry.getValue();
                    output = "%d,%d".formatted(price, quantity);
                }
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
        Integer price;
        Integer quantity;

        price = Integer.parseInt(values[1]);
        quantity = Integer.parseInt(values[2]);
        String typeOfOrder = values[3];
        switch (typeOfOrder) {
            case "bid" -> {
                if (bidTreeMap.containsKey(price) && quantity == 0) {
                    bidTreeMap.remove(price);
                } else if (quantity == 0) {
                    return;
                } else if (bidTreeMap.containsKey(price)) {
                    bidTreeMap.put(price, bidTreeMap.get(price) + quantity);
                } else {
                    bidTreeMap.put(price, quantity);
                }
            }
            case "ask" -> {
                if (askTreeMap.containsKey(price) && quantity == 0) {
                    askTreeMap.remove(price);
                } else if (quantity == 0) {
                    return;
                } else if (askTreeMap.containsKey(price)) {
                    askTreeMap.put(price, askTreeMap.get(price) + quantity);
                } else {
                    askTreeMap.put(price, quantity);
                }
            }
        }

    }

    public void processOrder(String[] values) {
        String typeOfOrder = values[1];
        int amount = Integer.parseInt(values[2]);
        switch (typeOfOrder) {
            case "buy" -> {
                try {
                    while (amount != 0 || askTreeMap.isEmpty()) {
                        if (askTreeMap.get(askTreeMap.firstKey()) > 0) {
                            askTreeMap.merge(askTreeMap.firstKey(), 1, (integer, integer2) -> integer - integer2);
                            amount--;
                        } else {
                            askTreeMap.remove(askTreeMap.firstKey());
                        }
                    }
                } catch (NoSuchElementException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "sell" -> {
                try {
                    while (amount != 0 || bidTreeMap.isEmpty()) {
                        if (bidTreeMap.get(bidTreeMap.lastKey()) > 0) {
                            bidTreeMap.merge(bidTreeMap.lastKey(), 1, (integer, integer2) -> integer - integer2);
                            amount--;
                        } else {
                            bidTreeMap.remove(bidTreeMap.lastKey());
                        }
                    }
                } catch (NoSuchElementException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
