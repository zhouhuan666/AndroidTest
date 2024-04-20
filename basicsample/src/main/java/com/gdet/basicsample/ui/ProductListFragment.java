package com.gdet.basicsample.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import com.gdet.basicsample.R;
import com.gdet.basicsample.databinding.ListFragmentBinding;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-12-23
 * 描述：
 */
public class ProductListFragment extends Fragment {


    public static final String TAG = "ProductListFragment";
    private ListFragmentBinding mBinding;

    private ProductAdapter mProductAdapter;

    private final ProductClickCallBack mProductClickCallBack = product -> {
      if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)){
//          ((MainActivity)requireActivity())
      }

    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.list_fragment, container, false);

        mProductAdapter = new ProductAdapter();


        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        mBinding = null;

        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }


}
