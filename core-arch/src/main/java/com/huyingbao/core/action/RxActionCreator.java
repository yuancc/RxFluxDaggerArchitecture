package com.huyingbao.core.action;

import android.support.annotation.NonNull;

import com.huyingbao.core.dispatcher.Dispatcher;
import com.huyingbao.core.dispatcher.DisposableManager;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * 这个类必须被继承,提供一个可以创建RxAction的方法.
 * 按钮被点击触发回调方法，在回调方法中调用ActionCreator提供的有语义的的方法，
 * ActionCreator会根据传入参数创建Action并通过Dispatcher发送给Store，
 * 所有订阅了这个Action的Store会接收到订阅的Action并消化Action，
 * 然后Store会发送UI状态改变的事件给相关的Activity（或Fragment)，
 * Activity在收到状态发生改变的事件之后，开始更新UI（更新UI的过程中会从Store获取所有需要的数据）。
 * Created by liujunfeng on 2017/12/7.
 */
public abstract class RxActionCreator {
    public static final String RESPONSE = "response";

    private final Dispatcher mDispatcher;
    private final DisposableManager mDisposableManager;

    public RxActionCreator(Dispatcher dispatcher, DisposableManager disposableManager) {
        this.mDispatcher = dispatcher;
        this.mDisposableManager = disposableManager;
    }

    /**
     * 主要是为了和RxJava整合,用在调用网络接口api获取数据之后,被观察者得到数据,发生订阅关系,将返回的数据
     * 或者error封装成action,postAction或者postError出去
     * 订阅管理,将RxAction和Disposable添加到DisposableManager
     *
     * @param rxAction
     * @param disposable
     */
    private void addRxAction(RxAction rxAction, Disposable disposable) {
        mDisposableManager.add(rxAction, disposable);
    }

    /**
     * 订阅管理器是否已经有了该action
     *
     * @param rxAction
     * @return
     */
    private boolean hasRxAction(RxAction rxAction) {
        return mDisposableManager.contains(rxAction);
    }

    /**
     * 订阅管理器,移除该action
     *
     * @param rxAction
     */
    private void removeRxAction(RxAction rxAction) {
        mDisposableManager.remove(rxAction);
    }

    /**
     * 创建新的RxAction
     *
     * @param actionId action type对应具体是什么样的方法
     * @param data     键值对型的参数pair-value parameters(Key - Object))
     * @return
     */
    protected RxAction newRxAction(@NonNull String actionId, Object... data) {
        if (data != null) {
            if (data.length % 2 != 0)
                throw new IllegalArgumentException("Data must be a valid list of key,value pairs");
        }
        RxAction.Builder actionBuilder = new RxAction.Builder(actionId);
        int i = 0;
        while (i < data.length) {
            String key = (String) data[i++];
            Object value = data[i++];
            actionBuilder.put(key, value);
        }
        return actionBuilder.build();
    }

    /**
     * 通过调度器dispatcher将action推出去
     *
     * @param action
     */
    private void postRxAction(@NonNull RxAction action) {
        mDispatcher.postRxAction(action);
        removeRxAction(action);
    }

    /**
     * 通过调度器dispatcher将error action推出去
     *
     * @param action
     * @param throwable
     */
    private void postError(@NonNull RxAction action, Throwable throwable) {
        mDispatcher.postRxAction(RxError.newRxError(action, throwable));
        removeRxAction(action);
    }

    /**
     * 发送LocalAction
     *
     * @param actionId
     * @param data
     */
    public void postLocalAction(@NonNull String actionId, @NonNull Object... data) {
        postRxAction(newRxAction(actionId, data));
    }

    /**
     * 发送网络action
     *
     * @param rxAction
     * @param httpObservable
     */
    protected <T> void postHttpAction(RxAction rxAction, Observable<T> httpObservable) {
        if (hasRxAction(rxAction)) return;
        addRxAction(rxAction, getDisposable(rxAction, httpObservable));
    }

    /**
     * 调用网络接口,传入接口自己的回调(非RxFlux模式接口,无法发送接口数据,eg:新闻模块获取新闻列表接口)调用接口,发送接口返回数据
     *
     * @param rxAction
     * @param httpObservable
     * @return
     */
    private <T> Disposable getDisposable(RxAction rxAction, Observable<T> httpObservable) {
        return httpObservable// 1:指定IO线程
                .subscribeOn(Schedulers.io())// 1:指定IO线程
                .observeOn(AndroidSchedulers.mainThread())// 2:指定主线程
                .subscribe(// 2:指定主线程
                        richHttpResponse -> {
                            rxAction.getData().put(RESPONSE, richHttpResponse);
                            postRxAction(rxAction);
                        },
                        throwable -> postError(rxAction, throwable)
                );
    }
}
