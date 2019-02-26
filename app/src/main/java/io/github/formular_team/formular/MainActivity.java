package io.github.formular_team.formular;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import com.google.ar.core.Camera;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.rendering.Vertex;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;

import java.util.Collections;
import java.util.Comparator;

import io.github.formular_team.formular.math.Matrix4;
import io.github.formular_team.formular.math.Ray;
import io.github.formular_team.formular.math.Vector2;
import io.github.formular_team.formular.server.Game;
import io.github.formular_team.formular.server.ServerController;
import io.github.formular_team.formular.server.SimpleServer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ServerController controller;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        final ArFragment fragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ar);
        final ArSceneView view = fragment.getArSceneView();
        view.getScene().setOnTouchListener(new Scene.OnTouchListener() {
            final Node overlay = new Node();
            {
                MaterialFactory.makeOpaqueWithColor(MainActivity.this, new Color(0x7F7F7F))
                    .thenAccept(material -> {
                        final ModelRenderable cube = ShapeFactory.makeCube(new Vector3(0.1F, 0.15F, 0.0F), new Vector3(-0.075F, 0.2F, -0.5F), material);
                        cube.setShadowReceiver(false);
                        cube.setShadowCaster(false);
                        this.overlay.setRenderable(cube);
                    });
                view.getScene().getCamera().addChild(this.overlay);
            }

            @Override
            public boolean onSceneTouch(final HitTestResult hit, final MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return true;
                }
                final Frame frame = view.getArFrame();
                // frame.hitTest(view.getWidth() / 2.0F, view.getHeight() / 2.0F)
                for (final HitResult result : frame.hitTest(event))
                    if (result.getTrackable() instanceof Plane) {
                        final Plane plane = (Plane) result.getTrackable();
                        final Pose pose = result.getHitPose();
                        final AnchorNode node = new AnchorNode(plane.createAnchor(pose));
                        MaterialFactory.makeOpaqueWithColor(MainActivity.this, new Color(0x0F42DA))
                            .thenAccept(material -> node.setRenderable(
                                ShapeFactory.makeCube(new Vector3(0.15F, 0.15F, 0.15F), new Vector3(0.0F, 0.15F / 2.0F, 0.0F), material)
                            ));
                        node.setParent(view.getScene());

                        final float[] planeNormal = plane.getCenterPose().getYAxis();
                        final float[] planeTranslation = plane.getCenterPose().getTranslation();
                        final io.github.formular_team.formular.math.Plane fplane = new io.github.formular_team.formular.math.Plane();
                        fplane.setFromNormalAndCoplanarPoint(
                            new io.github.formular_team.formular.math.Vector3(planeNormal[0], planeNormal[1], planeNormal[2]),
                            new io.github.formular_team.formular.math.Vector3(planeTranslation[0], planeTranslation[1], planeTranslation[2])
                        );
                        final Camera camera = frame.getCamera();
                        final Matrix4 modelMat = new Matrix4();
                        pose.toMatrix(modelMat.elements(), 0);
                        final Matrix4 projMat = new Matrix4();
                        camera.getProjectionMatrix(projMat.elements(), 0, 0.01F, 10.0F);
                        final Matrix4 viewMat = new Matrix4();
                        camera.getViewMatrix(viewMat.elements(), 0);

                        final float[] imageCoords = new float[2];
                        frame.transformCoordinates2d(
                            Coordinates2d.VIEW, new float[] { event.getX(), event.getY() },
                            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, imageCoords
                        );
                        final Ray r = Projection.unproject(
                            new Vector2(imageCoords[0], imageCoords[1]),
                            projMat,
                            viewMat
                        );
                        r.intersectPlane(fplane, new io.github.formular_team.formular.math.Vector3())
                            .ifPresent(v -> {

                            });
                        final int size = 128;
                        final Bitmap map = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
//                        Log.i("waldo", "img int " + Arrays.toString(frame.getCamera().getImageIntrinsics().getImageDimensions()) + ", " + Arrays.toString(frame.getCamera().getTextureIntrinsics().getImageDimensions()));
                        /*try (final Image image = frame.acquireCameraImage()) {
                            // TODO: crop?
                            final float normalizedX = event.getX() / view.getWidth();
                            final float normalizedY = event.getY() / view.getHeight();
                            final ByteBuffer bbuf = ByteBuffer.allocateDirect(2 * Float.BYTES);
                            bbuf.order(ByteOrder.nativeOrder());
                            final FloatBuffer buf = bbuf.asFloatBuffer();
                            buf.put(normalizedX).put(normalizedY);
                            buf.position(0);
                            frame.transformDisplayUvCoords(buf, buf);
                            final float sampleX = buf.get();
                            final float sampleY = buf.get();
                            final int centerX = (int) (sampleX * image.getWidth());
                            final int centerY = (int) (sampleY * image.getHeight());
                            size = Ints.min(centerX, centerY, image.getWidth() - centerX, image.getHeight() - centerY);
                            final Rect rect = new Rect(0, 0, size, size);
                            rect.offsetTo(centerX - rect.width() / 2, centerY - rect.height() / 2);
                            map = Images.yuvToBitmap(image, rect); // new Rect(0, 0, Math.min(image.getWidth(), image.getHeight()), Math.min(image.getWidth(), image.getHeight()))
                        } catch (final NotYetAvailableException e) {
                            throw new RuntimeException(e);
                        }*/
                        Texture.builder().setSource(map).build().thenAccept(texture -> {
                            MaterialFactory.makeOpaqueWithTexture(MainActivity.this, texture)
                                .thenAccept(material -> {
                                    final Vector3 extents = new Vector3(0.15F, 0.0F, 0.15F).scaled(0.5F);
                                    final Vector3 p0 = new Vector3(-extents.x, extents.y, -extents.z);
                                    final Vector3 p1 = new Vector3(extents.x, extents.y, -extents.z);
                                    final Vector3 p4 = new Vector3(-extents.x, extents.y, extents.z);
                                    final Vector3 p5 = new Vector3(extents.x, extents.y, extents.z);
                                    final Vector3 front = Vector3.up();
                                    final ImmutableList<Vertex> vertices = ImmutableList.of(
                                        Vertex.builder().setPosition(p4).setNormal(front).setUvCoordinate(new Vertex.UvCoordinate(0.0F, 1.0F)).build(),
                                        Vertex.builder().setPosition(p5).setNormal(front).setUvCoordinate(new Vertex.UvCoordinate(1.0F, 1.0F)).build(),
                                        Vertex.builder().setPosition(p1).setNormal(front).setUvCoordinate(new Vertex.UvCoordinate(1.0F, 0.0F)).build(),
                                        Vertex.builder().setPosition(p0).setNormal(front).setUvCoordinate(new Vertex.UvCoordinate(0.0F, 0.0F)).build()
                                    );
                                    final RenderableDefinition.Submesh mesh = RenderableDefinition.Submesh.builder()
                                        .setTriangleIndices(ImmutableList.of(3, 1, 0, 3, 2, 1))
                                        .setMaterial(material)
                                        .build();
                                    final RenderableDefinition def = RenderableDefinition.builder()
                                        .setVertices(vertices)
                                        .setSubmeshes(Collections.singletonList(mesh))
                                        .build();
                                    final ModelRenderable renderable = Futures.getUnchecked(ModelRenderable.builder().setSource(def).build());
                                    renderable.setShadowReceiver(false);
                                    renderable.setShadowCaster(false);
                                    this.overlay.setRenderable(renderable);
                                    this.overlay.setLocalPosition(new Vector3(-0.075F, 0.215F, -0.5F));
                                    this.overlay.setLocalRotation(Quaternion.multiply(Quaternion.axisAngle(Vector3.left(), 90.0F), Quaternion.axisAngle(front, 90.0F)));
                                });
                        });
                        return true;
                    }
                return false;
            }
        });
        view.getScene().addOnUpdateListener(new Scene.OnUpdateListener() {
            boolean needUpdate = false;

            @Override
            public void onUpdate(final FrameTime frameTime) {
                if (this.needUpdate) {
                    final Session session = fragment.getArSceneView().getSession();
                    session.pause();
                    session.getSupportedCameraConfigs().stream()
                        .max(Comparator.comparingDouble(config -> config.getImageSize().getHeight()))
                        .ifPresent(session::setCameraConfig);
                    try {
                        session.resume();
                    } catch (final CameraNotAvailableException e) {
                        throw new AssertionError(e);
                    }
                    this.needUpdate = false;
                }
            }
        });
        /*ModelRenderable.builder()
            .setSource(this, Uri.parse("teapot.sfb"))
            .build()
            .thenAccept(renderable -> {
                Node node = new Node();
                node.setParent(fragment.getArSceneView().getScene());
                node.setRenderable(renderable);
            })
            .exceptionally(throwable -> {
                Log.e("formular", "Unable to load Renderable.", throwable);
                return null;
            });*/

    }

    private static Vector3 createVector3(final float[] xyz) {
        return new Vector3(xyz[0], xyz[1], xyz[2]);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Game game = new Game();
        this.controller = ServerController.create(SimpleServer.create(game, 20));
        this.controller.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.controller.stop();
    }
}
