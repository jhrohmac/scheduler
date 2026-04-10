package com.scheduler.comm.util;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VoSorter<T> {
	
	// List<StockInfoVo> list = new ArrayList<>();
    // Populate the list with StockInfoVo objects

    // Sorting parameters
	// String sortField = "stock_open"; // Field to sort by
    // String sortOrder = "ASC"; // Sorting order

     // Create an instance of StockInfoSorter and use it to sort the list
	
     //StockInfoSorter<StockInfoVo> sorter = new StockInfoSorter<>();
     //sorter.sort(list, sortField, sortOrder);

    public void sort(List<T> list, String sortField, String sortOrder) {
        // Comparator based on the sorting parameters
        Comparator<T> comparator = (o1, o2) -> {
            String value1 = getValue(o1, sortField);
            String value2 = getValue(o2, sortField);

            // Convert values to Double for comparison
            double doubleValue1 = Double.parseDouble(value1);
            double doubleValue2 = Double.parseDouble(value2);

            // Compare based on sorting order
            return sortOrder.equals("ASC") ? Double.compare(doubleValue1, doubleValue2) :
                    Double.compare(doubleValue2, doubleValue1); // DESC order
        };

        // Sort the list using the comparator
        Collections.sort(list, comparator);
    }

    // Method to get the value of a field using reflection
    private String getValue(T object, String fieldName) {
        try {
            // Use reflection to get the value of the field
            Method method = object.getClass().getMethod("get" + capitalize(fieldName));
            return (String) method.invoke(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to capitalize the first letter of a string
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
