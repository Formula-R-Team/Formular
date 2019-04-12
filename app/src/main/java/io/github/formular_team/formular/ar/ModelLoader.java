/*
 * Copyright 2018 Google LLC
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
package io.github.formular_team.formular.ar;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.google.ar.sceneform.rendering.ModelRenderable;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;

import io.github.formular_team.formular.RaceActivity;

/**
 * Model loader class to avoid memory leaks from the activity. Activity and Fragment controller
 * classes have a lifecycle that is controlled by the UI thread. When a reference to one of these
 * objects is accessed by a background thread it is "leaked". Using that reference to a
 * lifecycle-bound object after Android thinks it has "destroyed" it can produce bugs. It also
 * prevents the Activity or Fragment from being garbage collected, which can leak the memory
 * permanently if the reference is held in the singleton scope.
 *
 * <p>To avoid this, use a non-nested class which is not an activity nor fragment. Hold a weak
 * reference to the activity or fragment and use that when making calls affecting the UI.
 */
@SuppressWarnings({ "AndroidApiChecker" })
public class ModelLoader {
    private static final String TAG = "ModelLoader";

    private final SparseArray<CompletableFuture<ModelRenderable>> futureSet = new SparseArray<>();

    private final WeakReference<Listener> owner;

    public ModelLoader(final Listener owner) {
        this.owner = new WeakReference<>(owner);
    }

    /**
     * Starts loading the model specified. The result of the loading is returned asynchronously via
     * {@link RaceActivity#setRenderable(int, ModelRenderable)} or {@link
     * RaceActivity#onException(int, Throwable)}.
     *
     * <p>Multiple models can be loaded at a time by specifying separate ids to differentiate the
     * result on callback.
     *
     * @param id         the id for this call to loadModel.
     * @param resourceId the resource id of the .sfb to load.
     * @return true if loading was initiated.
     */
    public boolean loadModel(final int id, final int resourceId) {
        final Listener activity = this.owner.get();
        if (activity == null) {
            Log.d(TAG, "Activity is null, cannot load model");
            return false;
        }
        final CompletableFuture<ModelRenderable> future =
            ModelRenderable.builder()
                .setSource(activity.getContext(), resourceId)
                .build()
                .thenApply(renderable -> this.setRenderable(id, renderable))
                .exceptionally(throwable -> this.onException(id, throwable));
        if (future != null) {
            this.futureSet.put(id, future);
        }
        return future != null;
    }

    ModelRenderable onException(final int id, final Throwable throwable) {
        final Listener activity = this.owner.get();
        if (activity != null) {
            activity.onException(id, throwable);
        }
        this.futureSet.remove(id);
        return null;
    }

    ModelRenderable setRenderable(final int id, final ModelRenderable modelRenderable) {
        final Listener activity = this.owner.get();
        if (activity != null) {
            activity.setRenderable(id, modelRenderable);
        }
        this.futureSet.remove(id);
        return modelRenderable;
    }

    public interface Listener {
        Context getContext();

        void setRenderable(final int id, final ModelRenderable renderable);

        void onException(final int id, final Throwable throwable);
    }
}