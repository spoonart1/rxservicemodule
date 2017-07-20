// ICounterService.aidl
package com.spoonart.service;

// Declare any non-default types here with import statements
import com.spoonart.service.ICounterServiceCallback;

interface ICounterService {
    long getPID();
    void registerCallback(ICounterServiceCallback callback);
    void unregisterCallback();
}
