package com.huyingbao.core.store;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModel;

import com.huyingbao.core.dispatcher.Dispatcher;
import com.huyingbao.core.dispatcher.RxActionDispatch;


/**
 * RxStore是一个抽象类,注册到dispatcher中,用来管理订阅,按顺序接收actions.
 * 这个类必须被应用的每个store扩展实现,来接收被调度传递过来的actions
 * 每一个Store仅仅负责一片逻辑相关的UI区域，用来维护这片UI的状态，
 * Store对外仅仅提供get方法，它的更新通过Dispatcher派发的Action来更新，
 * 当有新的Action进来的时候，它会负责处理Action，并转化成UI需要的数据。
 * 所有的数据都通过Dispatcher这个枢纽中心传递。
 * Action通过ActionCreator的帮助类产生并传递给Dispatcher，
 * Action大部分情况下是在用户和View交互的时候产生。
 * 然后Dispatcher会调用Store注册在其(Dispatcher)中的回调方法,
 * 把Action发送到所有注册的Store。
 * 在Store的回调方法内，Store可以处理任何和自身状态有关联的Action。
 * Store接着会触发一个 change 事件来告知Controller-View数据层发生变化。
 * Created by liujunfeng on 2017/12/7.
 */
public abstract class RxStore extends ViewModel implements RxActionDispatch, LifecycleObserver {
    private final Dispatcher mDispatcher;

    public RxStore(Dispatcher dispatcher) {
        this.mDispatcher = dispatcher;
    }

    /**
     * 需要将store注册到dispatcher中
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void register() {
        mDispatcher.subscribeRxStore(this);
    }

    /**
     * 从dispatcher中解除注册
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void unregister() {
        mDispatcher.unsubscribeRxStore(this);
    }

    /**
     * 传递更改,传递一个RxStoreChange,
     * 每一个RxStoreChange由storeId和action组成
     *
     * @param change
     */
    protected void postChange(RxStoreChange change) {
        mDispatcher.postRxStoreChange(change);
    }
}
