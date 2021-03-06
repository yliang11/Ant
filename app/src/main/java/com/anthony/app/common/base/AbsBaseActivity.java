package com.anthony.app.common.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.anthony.app.common.data.EventPosterHelper;
import com.anthony.app.common.injection.component.ActivityComponent;
import com.anthony.app.common.injection.component.DaggerActivityComponent;
import com.anthony.app.common.injection.module.ActivityModule;
import com.anthony.app.common.utils.ToastUtils;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Subscription;
import timber.log.Timber;


/**
 * Created by Anthony on 2016/4/24.
 * Class Note:
 * 1 all activities implement from this class
 *
 * todo add Umeng analysis
 */
public abstract class AbsBaseActivity extends AppCompatActivity  {

    protected static String TAG_LOG = null;// Log tag

    protected Context mContext = null;//context
    private ActivityComponent mActivityComponent;
    protected Subscription mSubscription;
    private Unbinder mUnbinder;

    @Inject
    ToastUtils toastUtils;

    @Inject
    EventPosterHelper eventPosterHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = this;

//set timber tag
        TAG_LOG = this.getClass().getSimpleName();
        Timber.tag(TAG_LOG);
//save activities stack
        BaseAppManager.getInstance().addActivity(this);
//set content view
        if (getContentViewID() != 0)
            setContentView(getContentViewID());
//bind this after setContentView
        mUnbinder = ButterKnife.bind(this);
//inject Dagger2 here
        injectDagger(activityComponent());


//register EventBus
        eventPosterHelper.getBus().register(this);
//sample        eventPosterHelper.postEventSafely(xxx);

//init views and events
        initViewsAndEvents();


    }


    public ActivityComponent activityComponent() {
        if (mActivityComponent == null) {
            mActivityComponent = DaggerActivityComponent.builder()
                    .activityModule(new ActivityModule(this))
                    .applicationComponent(MyApplication.get(this).getAppComponent())
                    .build();
        }
        return mActivityComponent;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        ButterKnife.unbind(this);
        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void finish() {
        super.finish();
        BaseAppManager.getInstance().removeActivity(this);
    }


    /**
     * bind layout resource file
     */
    protected abstract int getContentViewID();

    /**
     * Dagger2 use in your application module(not used in 'base' module)
     */
    protected void injectDagger(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    /**
     * init views and events here
     */
    protected abstract void initViewsAndEvents();


    protected void showToast(String content) {
        toastUtils.showToast(content);
    }


}

