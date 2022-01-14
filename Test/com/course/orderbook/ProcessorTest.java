package com.course.orderbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class ProcessorTest {

    @Test
    void addedThreeAsk_shouldReturnBestNonZeroAsk_processQuery() {
        Processor processor = new Processor();
        processor.processLine("u,99,0,ask");
        processor.processLine("u,98,50,ask");
        processor.processLine("u,97,100,ask");
        processor.processLine("u,97,0,ask");
        var res = processor.processQuery(new String[]{"q", "best_ask"});
        assertEquals("98,50", res);
    }

    @Test
    void addTwoBidForOnePrice_shouldContainsOneElementInBidTreeMap_processUpdate() {
        TreeMap<Integer, Integer> bidTreeMap = new TreeMap<>();
        TreeMap<Integer, Integer> askTreeMap = new TreeMap<>();
        bidTreeMap.put(9, 11);
        Processor processor = new Processor();
        processor.processLine("u,9,1,bid");
        processor.processLine("u,9,10,bid");
        assertEquals(bidTreeMap, processor.getBidTreeMap());
        assertEquals(1, bidTreeMap.size());
        assertEquals(askTreeMap, processor.getAskTreeMap());
    }

    @Test
    void processUpdateAsk() {
        TreeMap<Integer, Integer> bidTreeMap = new TreeMap<>();
        bidTreeMap.put(9, 1);
        Processor processor = new Processor();
        processor.processLine("u,9,1,bid");
        assertEquals(bidTreeMap, processor.getBidTreeMap());
    }

    @Test
    @DisplayName("getSizeByPrice()")
    void getSizeByPrice() {
        Processor processor = new Processor();
        processor.processLine("u,20,5,bid");
        processor.processLine("u,21,10,bid");
        processor.processLine("u,22,15,bid");
        processor.processLine("u,10,5,ask");
        processor.processLine("u,9,10,ask");
        processor.processLine("u,8,15,ask");

        assertEquals("5", processor.processQuery(new String[]{"q", "size", "20"}));
        assertEquals("10", processor.processQuery(new String[]{"q", "size", "21"}));
        assertEquals("15", processor.processQuery(new String[]{"q", "size", "22"}));
        assertEquals("5", processor.processQuery(new String[]{"q", "size", "10"}));
        assertEquals("10", processor.processQuery(new String[]{"q", "size", "9"}));
        assertEquals("15", processor.processQuery(new String[]{"q", "size", "8"}));
        assertEquals("0", processor.processQuery(new String[]{"q", "size", "888"}));
    }

    @Test
    @DisplayName("insertAskOrder()")
    void insertAskOrder() {
        Processor processor = new Processor();
        processor.processLine("u,20,5,ask");
        processor.processLine("u,21,10,ask");
        processor.processLine("u,22,15,ask");
        assertEquals(3, processor.getAskTreeMap().size());
    }

    @Test
    @DisplayName("insertBidOrder()")
    void insertBidOrder() {
        Processor processor = new Processor();
        processor.processLine("u,10,5,bid");
        processor.processLine("u,9,10,bid");
        processor.processLine("u,8,15,bid");
        assertEquals(3, processor.getBidTreeMap().size());
    }

    @Test
    void removeOrders() {
        Processor processor = new Processor();

        processor.processLine("u,22,15,ask");
        processor.processLine("u,21,10,ask");
        processor.processLine("u,20,5,ask");
        processor.processLine("u,10,5,bid");
        processor.processLine("u,9,10,bid");
        processor.processLine("u,8,15,bid");

        processor.processLine("o,sell,3");
        assertEquals("10,2", processor.processQuery(new String[]{"q", "best_bid"}));

        processor.processLine("o,sell,7");
        assertEquals("9,5", processor.processQuery(new String[]{"q", "best_bid"}));

        processor.processLine("o,buy,3");
        assertEquals("20,2", processor.processQuery(new String[]{"q", "best_ask"}));

        processor.processLine("o,buy,7");
        assertEquals("21,5", processor.processQuery(new String[]{"q", "best_ask"}));
    }

    @Test
    @DisplayName("insertMiddleOrder()")
    void insertMiddleOrder() {
        Processor processor = new Processor();

        processor.processLine("u,22,15,ask");
        processor.processLine("u,21,10,ask");
        processor.processLine("u,20,20,bid");
        processor.processLine("u,21,20,bid");

        assertEquals("21,20", processor.processQuery(new String[]{"q", "best_bid"}));

        processor.processLine("u,10,5,bid");
        processor.processLine("u,9,10,bid");
        processor.processLine("u,8,15,bid");
        processor.processLine("u,9,30,ask");

        assertEquals("9,30", processor.processQuery(new String[]{"q", "best_ask"}));
    }

    @Test
    @DisplayName("insertMiddleOrder()")
    void insertMiddleOrder2() {
        Processor processor = new Processor();

        processor.processLine("u,22,15,ask");
        processor.processLine("u,21,10,ask");
        processor.processLine("u,20,5,ask");
        processor.processLine("u,23,10,bid");

        assertEquals("20,5", processor.processQuery(new String[]{"q", "best_ask"}));
        assertEquals("10", processor.processQuery(new String[]{"q", "size", "21"}));


        processor.processLine("u,10,5,bid");
        processor.processLine("u,9,10,bid");
        processor.processLine("u,8,15,bid");
        processor.processLine("u,5,10,ask");

        assertEquals("5,10", processor.processQuery(new String[]{"q", "best_ask"}));

        assertEquals("23,10", processor.processQuery(new String[]{"q", "best_bid"}));
        assertEquals("10", processor.processQuery(new String[]{"q", "size", "9"}));
    }

    @Test
    @DisplayName("removeAllOrders()")
    void removeAllOrders() {
        Processor processor = new Processor();

        processor.processLine("u,20,5,ask");
        processor.processLine("u,21,10,ask");
        processor.processLine("u,22,15,ask");
        processor.processLine("u,10,5,bid");
        processor.processLine("u,9,10,bid");
        processor.processLine("u,8,15,bid");

        processor.processLine("o,buy,100");
        processor.processLine("o,sell,100");

        assertEquals(0, processor.getBidTreeMap().size());
        assertEquals(0, processor.getAskTreeMap().size());
        assertEquals("0", processor.processQuery(new String[]{"q", "size", "100"}));
    }
}