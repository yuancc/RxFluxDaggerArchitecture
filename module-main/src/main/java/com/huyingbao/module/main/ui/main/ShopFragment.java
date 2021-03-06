package com.huyingbao.module.main.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.widget.TextView;

import com.huyingbao.core.custom.CommonFragment;
import com.huyingbao.core.scope.PerActivity;
import com.huyingbao.module.main.R;
import com.huyingbao.module.main.R2;
import com.huyingbao.module.main.action.MainActionCreator;
import com.huyingbao.module.main.ui.main.model.Shop;
import com.huyingbao.module.main.ui.main.module.MainStore;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Created by liujunfeng on 2017/12/7.
 */
@PerActivity
public class ShopFragment extends CommonFragment {
    @Inject
    MainActionCreator mActionCreator;
    @BindView(R2.id.tv_shop_name)
    TextView mTvShopName;
    @BindView(R2.id.tv_shop_login)
    TextView mTvShopLogin;
    @BindView(R2.id.tv_shop_statistics)
    TextView mTvShopStatistics;
    private MainStore mStore;

    @Inject
    public ShopFragment() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_shop;
    }

    @Override
    public void afterCreate(Bundle savedInstanceState) {
        initActionBar("店铺信息");
        mStore = ViewModelProviders.of(getActivity(), mViewModelFactory).get(MainStore.class);
        mStore.getShopId().observe(this, integer -> mActionCreator.getShop(mContext, integer));
        mStore.getShop().observe(this, shop -> showShopInfo(shop));
    }

    /**
     * 显示店铺信息
     *
     * @param shop
     */
    private void showShopInfo(Shop shop) {
        mTvShopLogin.setText(shop.getCode() + "");
        mTvShopName.setText(shop.getShopName());
        mTvShopStatistics.setText(shop.getShopType() + "");
    }
}
