package com.itprom.jet.ofiice.service;

import com.itprom.jet.common.bean.Route;
import com.itprom.jet.common.bean.RoutePath;
import com.itprom.jet.common.bean.RoutePoint;
import com.itprom.jet.ofiice.provider.AirPortsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class WaitingRoutesService {
    private final Lock lock = new ReentrantLock(true);
    private final List<Route> list = new ArrayList<>();

    public List<Route> list(){
        return list;
    }

    public void add(Route route){
        try {
            lock.lock();
            list.add(route);
        } finally {
            lock.unlock();
        }
    }

    public void remove(Route route) {
        try {
            lock.lock();
            list.removeIf(route::equals);
        } finally {
            lock.unlock();
        }
    }

}