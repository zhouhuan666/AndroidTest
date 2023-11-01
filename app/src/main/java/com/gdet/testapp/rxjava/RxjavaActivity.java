package com.gdet.testapp.rxjava;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdet.testapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-11-01
 * 描述：
 */
public class RxjavaActivity extends AppCompatActivity {

    private static final String TAG = "RxjavaActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rxjava);
        mergeArray();
    }

    void initMap() {
        Disposable disposable = Observable.just(1, 2, 3, 4, 5).map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) throws Exception {
                return integer * 10;
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.e(TAG, "accept: " + integer);
            }
        });

    }

    void initFlatMap() {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("A");
                emitter.onNext("B");
                emitter.onComplete();
            }
        }).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                List<String> list = new ArrayList<>();
                Log.e(TAG, "apply: " + s);
                for (int j = 0; j < 2; j++) {
                    list.add("拆分子事件" + s + j);
                }
                return Observable.fromIterable(list);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e(TAG, "accept: " + s);
            }
        });
    }


    void initConcatMap() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("A");
                emitter.onNext("B");
                emitter.onComplete();
            }
        }).concatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                List<String> strings = new ArrayList<>();
                Log.e(TAG, "apply: " + s);
                for (int j = 0; j < 2; j++) {
                    strings.add("concatMap " + s + j);
                }
                return Observable.fromIterable(strings);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e(TAG, "concatMap accept: " + s);
            }
        });
    }

    void initBuffer() {
        Observable.just("A", "B", "C", "D", "E").buffer(3, 2).subscribe(new Observer<List<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "onSubscribe: ");
            }

            @Override
            public void onNext(List<String> strings) {
                Log.e(TAG, "onNext: " + strings.size());
                for (String s : strings) {
                    Log.e(TAG, "onNext: " + s);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e);
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: ");
            }
        });
    }

    void concat() {
        Observable.concat(Observable.just(1, 2), Observable.just(3, 4),
                Observable.just(5, 6), Observable.just(7, 8)).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "onSubscribe: " + d);
            }

            @Override
            public void onNext(Integer integer) {
                Log.e(TAG, "onNext: " + integer);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e);
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: ");
            }
        });
    }

    void concatArray() {
        Observable.concatArray(Observable.just("一", "二"), Observable.just("三", "四"),
                Observable.just("五", "六"), Observable.just("七", "八")).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "onSubscribe: " + d);
            }

            @Override
            public void onNext(String s) {
                Log.e(TAG, "onNext: " + s);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e);
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: ");
            }
        });
    }

    void mergeArray() {
        Observable<Long> observable1 = Observable.
                intervalRange(1, 3, 1, 1, TimeUnit.SECONDS);
        Observable observable2 = Observable.
                intervalRange(10, 3, 1, 1, TimeUnit.SECONDS);
        Observable.merge(observable1, observable2).subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "onSubscribe: " + d);
            }

            @Override
            public void onNext(Object o) {
                Log.e(TAG, "onNext: " + o);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e);
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: ");
            }
        });

    }

    void startWith() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("Array:1");
        Observable.just("一", "二", "三", "四").
                startWith(strings).startWith("startWith:2").startWithArray("startWithArray:3", "startWithArray:4").
                subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e(TAG, "onSubscribe: " + d);
                    }

                    @Override
                    public void onNext(String s) {
                        Log.e(TAG, "onNext: " + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e);
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete: ");
                    }
                });

    }

}
