package com.example.android.askquastionapp.utils;


import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class SimpleObserver<T, V> implements Observer<T> {
    V v;
    boolean control;

    public SimpleObserver(V v, boolean control) {
        this.v = v;
        this.control = control;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {
        onNext(t, v);
    }

    public abstract void onNext(T t, V v);

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
