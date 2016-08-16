package com.itheima.mobilesafe.ui.my_viewpager;

import android.view.View;

import com.itheima.mobilesafe.ui.view_helper.ViewHelper;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:15/6/19 上午8:41
 * 描述:
 */
public class ZoomStackPageTransformer extends BasePageTransformer {

    @Override
    public void handleInvisiblePage(View view, float position) {
    }

    @Override
    public void handleLeftPage(View view, float position) {
        ViewHelper.setTranslationX(view, -view.getWidth() * position);

        ViewHelper.setPivotX(view, view.getWidth() * 0.5f);
        ViewHelper.setPivotY(view, view.getHeight() * 0.5f);
        ViewHelper.setScaleX(view, 1 + position);
        ViewHelper.setScaleY(view, 1 + position);

        if (position < -0.95f) {
            ViewHelper.setAlpha(view, 0);
        } else {
            ViewHelper.setAlpha(view, 1);
        }
    }

    @Override
    public void handleRightPage(View view, float position) {
        ViewHelper.setTranslationX(view, -view.getWidth() * position);

        ViewHelper.setPivotX(view, view.getWidth() * 0.5f);
        ViewHelper.setPivotY(view, view.getHeight() * 0.5f);
        ViewHelper.setScaleX(view, 1 + position);
        ViewHelper.setScaleY(view, 1 + position);

        if (position > 0.95f) {
            ViewHelper.setAlpha(view, 0);
        } else {
            ViewHelper.setAlpha(view, 1);
        }
    }

}