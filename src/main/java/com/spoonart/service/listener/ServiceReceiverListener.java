package com.spoonart.service.listener;


import com.spoonart.service.event.base.BaseEvent;

/**
 * Created by Lafran on 7/19/17.
 */

public interface ServiceReceiverListener<EVT2 extends BaseEvent> {
    void onStartService();
    void onReceiveUpdate(EVT2 data);
}
