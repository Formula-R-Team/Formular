package io.github.formular_team.formular.ar.scene;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.Log;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.github.formular_team.formular.Geometries;
import io.github.formular_team.formular.GraphicsPathVisitor;
import io.github.formular_team.formular.geometry.ExtrudeGeometry;
import io.github.formular_team.formular.geometry.Geometry;
import io.github.formular_team.formular.math.CubicBezierCurve3;
import io.github.formular_team.formular.math.CurvePath;
import io.github.formular_team.formular.math.LineCurve3;
import io.github.formular_team.formular.math.Matrix4;
import io.github.formular_team.formular.math.Mth;
import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.math.PathVisitor;
import io.github.formular_team.formular.math.Shape;
import io.github.formular_team.formular.math.Vector2;
import io.github.formular_team.formular.math.Vector3;
import io.github.formular_team.formular.server.Checkpoint;
import io.github.formular_team.formular.server.Course;
import io.github.formular_team.formular.server.Track;

public class CourseNode extends Node {
    private static final String TAG = "CourseNode";

    private final Node road;

    private final List<Node> checkpoints = Lists.newArrayList();

    private CourseNode(final Node road) {
        this.road = road;
        this.addChild(this.road);
    }

    public void add(final KartNode kart) {
        this.road.addChild(kart);
    }

    public static CompletableFuture<CourseNode> create(final Context context, final Course course) {
        final Track track = course.getTrack();
        final Path path = track.getRoadPath();
        final float roadWidth = track.getRoadWidth();
        final Bitmap courseDiffuse = createDiffuse(context, course, 2048);
        return Texture.builder().setSource(courseDiffuse).build()
            .thenCompose(diffuse -> MaterialFactory.makeOpaqueWithTexture(context, diffuse))
            .thenApply(material -> {
                final float roadHeight = 0.225F;
                final Shape roadShape = new Shape();
                roadShape.moveTo(0.0F, -0.5F * (roadWidth + 0.75F));
                roadShape.lineTo(-roadHeight, -0.5F * (roadWidth + 0.75F));
                roadShape.lineTo(-roadHeight, 0.5F * (roadWidth + 0.75F));
                roadShape.lineTo(0.0F, 0.5F * (roadWidth + 0.75F));
                roadShape.closePath();
                final CurvePath trackPath3 = toCurve3(path);
                final Geometry roadGeom = new ExtrudeGeometry(ImmutableList.of(roadShape), new ExtrudeGeometry.ExtrudeGeometryParameters() {{
                    this.steps = (int) (6 * trackPath3.getLength());
                    this.extrudePath = trackPath3;
                    this.uvGenerator = ExtrudeGeometry.VertexUVGenerator.transform(new Matrix4()
                        .multiply(new Matrix4().makeTranslation(0.5F, 0.0F, 0.5F))
                        .multiply(new Matrix4().makeScale(1.0F / course.getSize(), 1.0F / course.getSize(), 1.0F / course.getSize()))
                    );
                }});
                final float wallHeight = 0.42F, wallWidth = 0.34F;
                final CurvePath wallLeftPath = toCurve3(new Path().fromPoints(track.getCheckpoints().stream().map(Checkpoint::getP1).collect(Collectors.toList()), true));
                final CurvePath wallRightPath = toCurve3(new Path().fromPoints(track.getCheckpoints().stream().map(Checkpoint::getP2).collect(Collectors.toList()), true));
                final Shape wallShape = new Shape();
                wallShape.moveTo(-roadHeight, -wallWidth * 0.5F);
                wallShape.lineTo(-roadHeight - wallHeight, 0.0F);
//                wallShape.lineTo(-roadHeight - wallHeight, wallWidth * 0.5F);
                wallShape.lineTo(-roadHeight, wallWidth * 0.5F);
                wallShape.closePath();
                final Geometry wallLeft = new ExtrudeGeometry(ImmutableList.of(wallShape), new ExtrudeGeometry.ExtrudeGeometryParameters() {{
                    this.steps = (int) (wallLeftPath.getLength());
                    this.extrudePath = wallLeftPath;
                    this.uvGenerator = new ExtrudeGeometry.VertexUVGenerator(v -> new Vector3(0.0F, 0.0F, 1.0F));
                }});
                final Geometry wallRight = new ExtrudeGeometry(ImmutableList.of(wallShape), new ExtrudeGeometry.ExtrudeGeometryParameters() {{
                    this.steps = (int) (wallRightPath.getLength());
                    this.extrudePath = wallRightPath;
                    this.uvGenerator = new ExtrudeGeometry.VertexUVGenerator(v -> new Vector3(0.0F, 0.0F, 1.0F));
                }});
                final Node road = new Node();
                road.setLocalPosition(new com.google.ar.sceneform.math.Vector3(0.0F, roadHeight, 0.0F));
                final CourseNode node = new CourseNode(road);
                final Node trackNode = new Node();
                final ModelRenderable trackRenderable = Geometries.toRenderable(ImmutableList.of(roadGeom, wallLeft, wallRight), material);
                trackNode.setRenderable(trackRenderable);
                node.addChild(trackNode);
                return node;
            })
//            .thenCombine(MaterialFactory.makeOpaqueWithColor(context, new com.google.ar.sceneform.rendering.Color(Color.RED)), (node, material) -> {
//                final Node center = new Node();
//                center.setRenderable(ShapeFactory.makeSphere(1.0F, new com.google.ar.sceneform.math.Vector3(), material));
//                node.addChild(center);
//                return node;
//            })
//            .thenCombine(MaterialFactory.makeOpaqueWithColor(context, new com.google.ar.sceneform.rendering.Color()), (node, material) -> {
//                final ImmutableList<Checkpoint> cc = track.getCheckpoints();
//                final ImmutableList.Builder<Vector3> points = ImmutableList.builder();
//                final ImmutableList.Builder<Vector3> pointsr = ImmutableList.builder();
//                material.setFloat("roughness", 1.0F);
//                material.setFloat("reflectance", 0.0F);
//                final Material checkpointMat = material.makeCopy();
//                checkpointMat.setFloat3("color", new com.google.ar.sceneform.rendering.Color(1.0F, 1.0F, 1.0F));
//                final Material checkpointrMat = material.makeCopy();
//                checkpointrMat.setFloat3("color", new com.google.ar.sceneform.rendering.Color(1.0F, 0.0F, 0.0F));
//                for (int n = 0; n < cc.size(); n++) {
//                    final Checkpoint f0 = cc.get(n);
//                    final Checkpoint f1 = cc.get(Math.floorMod(n + 1, cc.size()));
//                    final float height = track.getRoadWidth();
//                    (f0.isRequired() || cc.get(Math.floorMod(n - 1, cc.size())).isRequired() ? pointsr : points).add(
//                        Vector3.xz(f0.getP1(), 0.0F), Vector3.xz(f0.getP2(), 0.0F),
//                        Vector3.xz(f0.getP1(), height), Vector3.xz(f0.getP2(), height),
//                        Vector3.xz(f0.getP1(), 0.0F), Vector3.xz(f0.getP1(), height),
//                        Vector3.xz(f0.getP2(), 0.0F), Vector3.xz(f0.getP2(), height)
//                    );
//                    (f0.isRequired() ? pointsr : points).add(
//                        Vector3.xz(f0.getP1(), 0.0F), Vector3.xz(f1.getP1(), 0.0F),
//                        Vector3.xz(f0.getP2(), 0.0F), Vector3.xz(f1.getP2(), 0.0F),
//                        Vector3.xz(f0.getP1(), height), Vector3.xz(f1.getP1(), height),
//                        Vector3.xz(f0.getP2(), height), Vector3.xz(f1.getP2(), height)
//                    );
//                }
//                final Node frameNode = new Node();
//                frameNode.setRenderable(Geometries.lines(ImmutableList.of(points.build(), pointsr.build()), 0.05F, ImmutableList.of(checkpointMat, checkpointrMat)));
//                node.addChild(frameNode);
//                return node;
//            })
            ;
    }

    private static Bitmap createDiffuse(final Context context, final Course course, final int resolution) {
        final float courseRange = course.getSize() * 0.5F;
        final float wallTileSize = 1.0F;
        final Bitmap courseDiffuse = Bitmap.createBitmap(resolution, resolution, Bitmap.Config.ARGB_8888);
        final Bitmap pavementDiffuse = loadBitmap(context, "materials/pavement_diffuse.png");
        final Bitmap finishLineDiffuse = loadBitmap(context, "materials/finish_line_diffuse.png");
        final Canvas canvas = new Canvas(courseDiffuse);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final float courseToMap = resolution / course.getSize();
        final Matrix mat = new Matrix();
        mat.preScale(courseToMap, -courseToMap);
        mat.preTranslate(courseRange, -courseRange);
        canvas.setMatrix(mat);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(createTileShader(pavementDiffuse, 1.0F));
        canvas.drawRect(-courseRange, -courseRange, courseRange, courseRange, paint);
        paint.setShader(null);
        // begin wall tile
        paint.setColor(0xFF212023);
        canvas.drawRect(-courseRange, courseRange, -courseRange + wallTileSize, courseRange - wallTileSize, paint);
        // end wall tile
        final android.graphics.Path graphicsTrackPath = new android.graphics.Path();
        final Path path = course.getTrack().getRoadPath();
        path.visit(new GraphicsPathVisitor(graphicsTrackPath));
        graphicsTrackPath.close();
        final int paintWhite = 0xFFF2F3F4;
        {
            final float courseRoadMargin = 0.1F;
            final float courseRoadStripeWidth = 0.2F;
            final float outerRoadStripeWidth = course.getTrack().getRoadWidth() - 2.0F * courseRoadMargin;
            final float innerRoadStripeWidth = outerRoadStripeWidth - courseRoadStripeWidth;
            final Bitmap roadStripDiffuse = Bitmap.createBitmap(resolution, resolution, Bitmap.Config.ARGB_8888);
            final Canvas roadStripCanvas = new Canvas(roadStripDiffuse);
            roadStripCanvas.setMatrix(mat);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(outerRoadStripeWidth);
            paint.setColor(paintWhite);
            roadStripCanvas.drawPath(graphicsTrackPath, paint);
            paint.setStrokeWidth(innerRoadStripeWidth);
            paint.setColor(Color.TRANSPARENT);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            roadStripCanvas.drawPath(graphicsTrackPath, paint);
            paint.setXfermode(null);
            final Matrix m = new Matrix();
            m.setScale(1.0F / courseToMap, -1.0F / courseToMap);
            m.postTranslate(-courseRange, courseRange);
            canvas.drawBitmap(roadStripDiffuse, m, null);
        }
        final float finishline = course.getTrack().getFinishLinePosition();
        final Vector2 flPos = path.getPoint(finishline);
        final Vector2 flDir = path.getTangent(finishline).negate().rotate();
        final Matrix flMat = new Matrix(mat);
        flMat.preTranslate(flPos.getX(), flPos.getY());
        flMat.preRotate(Mth.toDegrees(Mth.atan2(flDir.getY(), flDir.getX())));
        canvas.setMatrix(flMat);
        final Shader shader = createTileShader(finishLineDiffuse, 1.0F);
        final Matrix m = new Matrix();
        shader.getLocalMatrix(m);
        m.postTranslate(0.0F, -0.5F);
        shader.setLocalMatrix(m);
        paint.setShader(shader);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0.5f * course.getTrack().getRoadWidth(), 0.5F, -0.5f * course.getTrack().getRoadWidth(), -0.5F, paint);
        paint.setShader(null);
//        paint.setTextAlign(Paint.Align.CENTER);
//        paint.setTextSize(1.0F);
//        paint.setColor(paintWhite);
//        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
//        canvas.drawText("FINISH", 0.0F, -1.25F, paint);
        canvas.setMatrix(mat);
        paint.setColor(paintWhite);
        paint.setStyle(Paint.Style.FILL);
//        for (int n = 0; n < 6; n++) {
//            final Track.Pose p = course.getTrack().getStartPlacement(n);
//            canvas.drawCircle(p.position.getX(), p.position.getY(), 0.2F, paint);
//        }
        return courseDiffuse;
    }

    private static CurvePath toCurve3(final Path path) {
        final CurvePath curve3 = new CurvePath();
        path.visit(new PathVisitor() {
            private Vector3 last = new Vector3();

            @Override
            public void moveTo(final float x, final float y) {
                this.last = this.map(x, y);
            }

            @Override
            public void lineTo(final float x, final float y) {
                curve3.add(new LineCurve3(this.last, this.last = this.map(x, y)));
            }

            @Override
            public void bezierCurveTo(final float aCP1x, final float aCP1y, final float aCP2x, final float aCP2y, final float aX, final float aY) {
                curve3.add(new CubicBezierCurve3(this.last, this.map(aCP1x, aCP1y), this.map(aCP2x, aCP2y), this.last = this.map(aX, aY)));
            }

            @Override
            public void closePath() {}

            private Vector3 map(final float x, final float y) {
                return new Vector3(x, 0.0F, y);
            }
        });
        return curve3;
    }
    private static final class MissingBitmap {
        private static final Bitmap INSTANCE = Bitmap.createBitmap(
            new int[] {
                0xFF000000, 0xFFFF00FF,
                0xFFFF00FF, 0xFF000000
            }, 2, 2, Bitmap.Config.ARGB_8888
        );
    }

    private static Bitmap loadBitmap(final Context context, final String fileName) {
        final AssetManager assets = context.getAssets();
        Bitmap map = null;
        try (final InputStream is = assets.open(fileName)) {
            map = BitmapFactory.decodeStream(is);
            if (map == null) {
                Log.e(TAG, "Unable to decode bitmap '" + fileName + "'");
            }
        } catch (final IOException e) {
            Log.e(TAG, "Unable to read bitmap '" + fileName + "'", e);
        }
        if (map == null) {
            return MissingBitmap.INSTANCE;
        }
        return map;
    }

    private static Shader createTileShader(final Bitmap bitmap, final float size) {
        final Shader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        final Matrix local = new Matrix();
        local.setScale(size / bitmap.getWidth(), size / bitmap.getHeight());
        shader.setLocalMatrix(local);
        return shader;
    }
}
