// ICounterServiceCallback.aidl
package com.spoonart.service;

// Declare any non-default types here with import statements

interface ICounterServiceCallback {
    oneway void onCounterEvent(long count);
}
