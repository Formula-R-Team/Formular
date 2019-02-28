package io.github.formular_team.formular;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
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
import com.google.ar.core.exceptions.NotYetAvailableException;
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
import java.util.function.Function;

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
                        /*final AnchorNode node = new AnchorNode(plane.createAnchor(pose));
                        MaterialFactory.makeOpaqueWithColor(MainActivity.this, new Color(0x0F42DA))
                            .thenAccept(material -> node.setRenderable(
                                ShapeFactory.makeCube(new Vector3(0.15F, 0.15F, 0.15F), new Vector3(0.0F, 0.15F / 2.0F, 0.0F), material)
                            ));
                        node.setParent(view.getScene());*/

                        final Pose planePose = plane.getCenterPose();
                        final float[] planeNormal = planePose.getYAxis();
                        final float[] planeTranslation = planePose.getTranslation();
                        final io.github.formular_team.formular.math.Plane fplane = new io.github.formular_team.formular.math.Plane();
                        fplane.setFromNormalAndCoplanarPoint(
                            new io.github.formular_team.formular.math.Vector3(planeNormal[0], planeNormal[1], planeNormal[2]),
                            new io.github.formular_team.formular.math.Vector3(planeTranslation[0], planeTranslation[1], planeTranslation[2])
                        );
                        final Camera camera = frame.getCamera();
                        final Matrix4 projMat = new Matrix4();
                        camera.getProjectionMatrix(projMat.elements(), 0, 0.01F, 10.0F);
                        final Matrix4 viewMat = new Matrix4();
                        camera.getViewMatrix(viewMat.elements(), 0);

                        final float[] in2 = new float[2];
                        final float[] out2 = new float[2];
                        in2[0] = event.getX();
                        in2[1] = event.getY();
                        frame.transformCoordinates2d(
                            Coordinates2d.VIEW, in2,
                            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, out2
                        );
                        final Ray pickRay = Projection.unproject(
                            new Vector2().fromArray(out2),
                            projMat,
                            viewMat
                        );
                        final int size = 128;
                        final float scale = 0.25F;
                        final Bitmap map = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                        pickRay.intersectPlane(fplane, new io.github.formular_team.formular.math.Vector3())
                            .ifPresent(v -> {
                                final Pose planeAtPick = Pose.makeTranslation(v.toArray(new float[3])).compose(planePose.extractRotation());
                                final Matrix4 planeModelMatrix = new Matrix4();
                                planeAtPick.toMatrix(planeModelMatrix.elements(), 0);
                                final Function<Vector2, Vector2> project = in -> {
                                    final io.github.formular_team.formular.math.Vector3 ndc = new io.github.formular_team.formular.math.Vector3(
                                        scale * (2.0F * in.x() / size - 1.0F),
                                        0,
                                        -scale * (2.0F * in.y() / size - 1.0F)
                                    ).applyMatrix4(planeModelMatrix).applyMatrix4(viewMat).applyMatrix4(projMat);
                                    in2[0] = ndc.x();
                                    in2[1] = ndc.y();
                                    frame.transformCoordinates2d(
                                        Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, in2,
                                        Coordinates2d.IMAGE_PIXELS, out2
                                    );
                                    return new Vector2().fromArray(out2);
                                };
                                final Vector2 b00 = project.apply(new Vector2(0, 0));
                                final Vector2 b01 = project.apply(new Vector2(0, size - 1));
                                final Vector2 b10 = project.apply(new Vector2(size - 1, 0));
                                final Vector2 b11 = project.apply(new Vector2(size - 1, size - 1));
                                final Vector2 imageMin = b00.copy().min(b01).min(b10).min(b11).floor();
                                final Vector2 imageMax = b00.copy().max(b01).max(b10).max(b11).ceil();
                                final Rect imageBounds = new Rect((int) imageMin.x(), (int) imageMin.y(), (int) imageMax.x(), (int) imageMax.y());
                                final Bitmap image;
                                try (final Image cameraImage = frame.acquireCameraImage()) {
                                    image = Images.yuvToBitmap(cameraImage, imageBounds);
                                } catch (final NotYetAvailableException e) {
                                    throw new RuntimeException(e);
                                }
                                final Vector2 outputPos = new Vector2();
                                for (int y = 0; y < size; y++) {
                                    for (int x = 0; x < size; x++) {
                                        final Vector2 imagePos = project.apply(outputPos.set(x, y)).sub(imageMin);
                                        final boolean inImage = imagePos.x() >= 0.0F && imagePos.y() >= 0.0F && imagePos.x() < image.getWidth() && imagePos.y() < image.getHeight();
                                        map.setPixel(x, y, inImage ? image.getPixel((int) imagePos.x(), (int) imagePos.y()) : android.graphics.Color.TRANSPARENT);
                                    }
                                }
                            });
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
