package com.spoonart.service.rxservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.spoonart.service.IBinderStub;
import com.spoonart.service.bus.RxBus;
import com.spoonart.service.event.base.BaseEvent;
import com.spoonart.service.event.base.BaseEventUI;
import com.spoonart.service.listener.ServiceReceiverListener;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * Created by Doconium on 17/07/2017.
 */

@SuppressWarnings("unchecked")
public final class ServiceManager<SE extends BaseEvent> {


    private final Context context;
    private Subscription subscription;
    private ServiceReceiverListener listener;

    private Class mService;
    private Class<SE> mEvent;

    private ServiceConnection serviceConnection;
    private Intent serviceIntent;

    public ServiceManager(Context context) {
        this.context = context;
    }

    public ServiceManager setProperties(Class service, Class busEventClass) {
        this.mEvent = busEventClass;
        this.mService = service;
        this.serviceIntent = new Intent(context, mService);
        initBus();
        return this;
    }


    public ServiceManager setListener(ServiceReceiverListener<SE> listener) {
        this.listener = listener;
        return this;
    }

    private void initBus() {
        getBus().toObserverable()
                .ofType(mEvent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<SE>() {
                    @Override
                    public void onNext(SE se) {
                        if (listener != null) {
                            listener.onReceiveUpdate(se);
                        }
                    }
                });
    }

    public static RxBus getBus() {
        return RxBus.get();
    }

    public ServiceManager startRunning(boolean stayAlive) {
        subscription = startLive(stayAlive)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean isConnect) {
                        if (isConnect) {
                            if (listener != null) {
                                listener.onStartService();
                            }
                            connectService();
                        }
                    }
                });

        return this;
    }

    public void unsubscribe() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private Observable<Boolean> startLive(boolean stayAlive) {
        return executeService(stayAlive);
    }

    private Observable<Boolean> callBackBinder(IBinderStub binderStub) {
        return Observable.create(subscriber -> {
            subscriber.add(Subscriptions.create(() -> {
                try {
                    binderStub.onUnbind();
                    subscriber.onNext(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }));
            try {
                binderStub.onBind();
                subscriber.onNext(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private Observable<Boolean> executeService(boolean stayAlive) {
        boolean isRun = false;
        if (stayAlive && !SpoonartService.isRunning(context, mService)) {
            context.startService(serviceIntent);
            isRun = true;
        }
        return Observable.just(isRun);
    }

    private void connectService() {
        serviceConnection = getServiceConnection();
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void disconnectService() {
        context.unbindService(getServiceConnection());
    }

    private ServiceConnection getServiceConnection() {
        return new ServiceConnection() {
            private IBinderStub binderStub;
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                System.out.println("service connected!");
                binderStub = IBinderStub.Stub.asInterface(service);
                subscription = callBackBinder(binderStub)
                        .subscribe(new SimpleObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean aBoolean) {
                                System.out.println("service listen to binder");
                            }
                        });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                System.out.println("service disconnected!");
                if(binderStub != null){
                    try {
                        binderStub.onUnbind();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public <T extends BaseEventUI> void sendToService(T event) {
        sendEventToService(event);
    }

    //MARK #Utility
    public static <T extends BaseEvent> void sendEventToUI(T serviceEvent) {
        ServiceManager.getBus().send(serviceEvent);
    }

    public static <T extends BaseEventUI> void sendEventToService(T uiEvent) {
        ServiceManager.getBus().send(uiEvent);
    }
    //END


    class SimpleObserver<T> implements Observer<T> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            System.out.println("SimpleObserver.onError : " + e.getMessage());
        }

        @Override
        public void onNext(T t) {

        }
    }
}
