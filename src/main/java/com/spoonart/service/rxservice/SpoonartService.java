package com.spoonart.service.rxservice;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.spoonart.service.IBinderStub;
import com.spoonart.service.bus.RxBus;
import com.spoonart.service.event.base.BaseEventUI;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Doconium on 17/07/2017.
 */

public abstract class SpoonartService<BEU extends BaseEventUI> extends Service{

    private RxBus rxBus;

    private final IBinder binder = new IBinderStub.Stub(){

        @Override
        public int getPid(int pid) throws RemoteException {
            return android.os.Process.myPid();
        }

        /**
         * @method onBind() called if service already connected
         * we will initialize bus here, to listen incoming data.
         * */
        @Override
        public void onBind() throws RemoteException {
            initBus();
        }

        /**
         * @method onUnbind() called if service is disconnected
         * */
        @Override
        public void onUnbind() throws RemoteException {
            rxBus = null;
        }
    };

    private final void initBus(){
        rxBus.toObserverable()
                .ofType(getEventTypeClass())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> onServiceReceiveEvent(data));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(!(binder instanceof IBinderStub))
        {
            throw new Error("binder must be instance of IBinderStub ");
        }
        rxBus = ServiceManager.getBus();
    }

    /**
     * @method set event class type for bus event
     * */
    public abstract Class<BEU> getEventTypeClass();

    public void onServiceReceiveEvent(BEU data) {

    }

    /**
     * check if service is running or not
     * */
    @SuppressWarnings("unused")
    public final static boolean isRunning(Context context, Class yourServiceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (yourServiceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart();
        return Service.START_STICKY_COMPATIBILITY;
    }

    /**
    * @method called when onStartCommand() triggered
    */
    public abstract void onStart();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }



}
