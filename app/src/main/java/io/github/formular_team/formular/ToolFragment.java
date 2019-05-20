package io.github.formular_team.formular;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.ux.BaseArFragment;

import java.util.concurrent.TimeUnit;

import io.github.formular_team.formular.core.course.Capture;
import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.course.CourseMetadata;
import io.github.formular_team.formular.core.course.CourseReader;

public class ToolFragment extends Fragment {
    private static final String TAG = "ToolFragment";

    private View view;

    private ArActivity activity;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_tool, container, false);
        final BottomNavigationView view = this.view.findViewById(R.id.toolbar);
        view.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
            case R.id.tool_shop:
                break;
            case R.id.tool_create:
                ToolFragment.this.activity.setOnTapArPlaneListener(new CreateListener());
                break;
            case R.id.tool_join:
                ToolFragment.this.activity.setOnTapArPlaneListener(new JoinListener());
                break;
            }
            return true;
        });
        return this.view;
    }

    private class CreateListener implements BaseArFragment.OnTapArPlaneListener {
        @Override
        public void onTapPlane(final HitResult result, final Plane plane, final MotionEvent event) {
            final Capture capture;
            try (final Capturer capturer = new Capturer(ToolFragment.this.activity.createRectifier())) {
                capture = capturer.capture(result.getHitPose(), 0.25F, 200);
            }
            final CourseReader reader = new CourseReader();
            reader.read(capture,
                CourseMetadata.create(ToolFragment.this.activity.getUser(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), "My Circuit"),
                new CourseReader.ResultConsumer() {
                    @Override
                    public void onSuccess(final Course course) {
                        ToolFragment.this.activity.createRace(result.createAnchor(), course);
                    }

                    @Override
                    public void onFail() {}
                });
        }
    }

    private class JoinListener implements BaseArFragment.OnTapArPlaneListener {
        @Override
        public void onTapPlane(final HitResult result, final Plane plane, final MotionEvent event) {
            ToolFragment.this.activity.joinRace(result.createAnchor());
        }
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof ArActivity) {
            this.activity = (ArActivity) context;
        } else {
            throw new RuntimeException(context + " must implement ArActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }
}
