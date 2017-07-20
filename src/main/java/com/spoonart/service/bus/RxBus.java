package com.spoonart.service.bus;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by Doconium on 10/07/2017.
 */

public class RxBus {

    private static RxBus rxBus;

    private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());

    public void send(Object o) {
        _bus.onNext(o);
    }

    public Observable<Object> toObserverable() {
        return _bus;
    }

    public boolean hasObservers() {
        return _bus.hasObservers();
    }

    public static RxBus get(){
        if(rxBus == null){
            rxBus = new RxBus();
        }
        return rxBus;
    }
}
