package com.y3technologies.masters.service.impl;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseServiceImpl {
    protected List<Sort.Order> getSort (String sortBy){
        String[] result = sortBy.split("\\|");

        List<Sort.Order> orders = new ArrayList<>();
        for (String order : result) {
            orders.add(new Sort.Order(order.toUpperCase().contains("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, order.split(",")[0]));
        }
        return orders;
    }
}
