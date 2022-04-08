/*
 * Copyright (C) 2017 Artem Hluhovskyi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hluhovskyi.camerabutton.rxjava2;

import com.hluhovskyi.camerabutton.CameraButton;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

final class ProgressEventObservable extends Observable<ProgressEvent> {

    private final CameraButton button;

    ProgressEventObservable(CameraButton button) {
        this.button = button;
    }

    @Override
    protected void subscribeActual(Observer<? super ProgressEvent> observer) {
        if (!Preconditions.checkMainThread(observer)) {
            return;
        }
        Listener listener = new Listener(button, observer);
        observer.onSubscribe(listener);
        button.setOnProgressChangeListener(listener);
    }

    static final class Listener extends MainThreadDisposable implements CameraButton.OnProgressChangeListener {

        private final CameraButton button;
        private final Observer<? super ProgressEvent> observer;

        Listener(CameraButton button, Observer<? super ProgressEvent> observer) {
            this.button = button;
            this.observer = observer;
        }

        @Override
        public void onProgressChanged(float progress) {
            if (!isDisposed()) {
                observer.onNext(ProgressEvent.create(button, progress));
            }
        }

        @Override
        protected void onDispose() {
            button.setOnProgressChangeListener(null);
        }
    }
}
