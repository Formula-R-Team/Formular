package io.github.formular_team.formular;

import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.rendering.Vertex;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;

import java.util.Iterator;
import java.util.List;

import io.github.formular_team.formular.geometry.Face3;
import io.github.formular_team.formular.geometry.Geometry;
import io.github.formular_team.formular.math.Vector2;

// TODO: mesh construction api
public final class Geometries {
    private Geometries() {}

    public static ModelRenderable lines(final List<List<io.github.formular_team.formular.math.Vector3>> pointsLists, final float width, final List<Material> materials) {
        final ImmutableList.Builder<Vertex> vertices = ImmutableList.builder();
        final ImmutableList.Builder<RenderableDefinition.Submesh> submeshes = ImmutableList.builder();
        final Iterator<List<io.github.formular_team.formular.math.Vector3>> pointsIter = pointsLists.iterator();
        final Iterator<Material> materialIter = materials.iterator();
        int box = 0;
        while (pointsIter.hasNext() && materialIter.hasNext()) {
            box += lines(pointsIter.next(), materialIter.next(), width, vertices, submeshes, box);
        }
        final RenderableDefinition def = RenderableDefinition.builder()
            .setVertices(vertices.build())
            .setSubmeshes(submeshes.build())
            .build();
        return Futures.getUnchecked(ModelRenderable.builder().setSource(def).build());
    }

    private static int lines(final List<io.github.formular_team.formular.math.Vector3> points, final Material material, final float width, final ImmutableList.Builder<Vertex> vertices, final ImmutableList.Builder<RenderableDefinition.Submesh> submeshs, int box) {
        final ImmutableList.Builder<Integer> indices = ImmutableList.builder();
        final Vector3 worldUp = Vector3.up();
        final Vector3 worldRight = Vector3.right();
        final Vertex.UvCoordinate uv00 = new Vertex.UvCoordinate(0.0F, 0.0F);
        final Vertex.UvCoordinate uv10 = new Vertex.UvCoordinate(1.0F, 0.0F);
        final Vertex.UvCoordinate uv01 = new Vertex.UvCoordinate(0.0F, 1.0F);
        final Vertex.UvCoordinate uv11 = new Vertex.UvCoordinate(1.0F, 1.0F);
        for (int i = 0; i < points.size(); box++) {
            final Vector3 v0 = v(points.get(i++));
            final Vector3 v1 = v(points.get(i++));
            final Vector3 dir = Vector3.subtract(v1, v0);
            final Vector3 up = dir.normalized();
            final Vector3 down = up.negated();
            final Vector3 left = Vector3.cross(Math.abs(Vector3.dot(worldUp, up)) > 0.5F ? worldRight : worldUp, up).normalized();
            final Vector3 front = Vector3.cross(up, left).normalized();
            final Vector3 right = left.negated();
            final Vector3 back = front.negated();
            final Vector3 dirLeft = left.scaled(0.5F * width);
            final Vector3 dirFront = front.scaled(0.5F * width);
            final Vector3 dirRight = dirLeft.negated();
            final Vector3 dirBack = dirFront.negated();
            final Vector3 p3 = Vector3.add(Vector3.add(v0, dirLeft), dirBack);
            final Vector3 p0 = Vector3.add(Vector3.add(v0, dirLeft), dirFront);
            final Vector3 p1 = Vector3.add(Vector3.add(v0, dirRight), dirFront);
            final Vector3 p2 = Vector3.add(Vector3.add(v0, dirRight), dirBack);
            final Vector3 p7 = Vector3.add(p3, dir);
            final Vector3 p4 = Vector3.add(p0, dir);
            final Vector3 p5 = Vector3.add(p1, dir);
            final Vector3 p6 = Vector3.add(p2, dir);
            vertices.add(
                Vertex.builder().setPosition(p0).setNormal(down).setUvCoordinate(uv01).build(),
                Vertex.builder().setPosition(p1).setNormal(down).setUvCoordinate(uv11).build(),
                Vertex.builder().setPosition(p2).setNormal(down).setUvCoordinate(uv10).build(),
                Vertex.builder().setPosition(p3).setNormal(down).setUvCoordinate(uv00).build(),

                Vertex.builder().setPosition(p7).setNormal(left).setUvCoordinate(uv01).build(),
                Vertex.builder().setPosition(p4).setNormal(left).setUvCoordinate(uv11).build(),
                Vertex.builder().setPosition(p0).setNormal(left).setUvCoordinate(uv10).build(),
                Vertex.builder().setPosition(p3).setNormal(left).setUvCoordinate(uv00).build(),

                Vertex.builder().setPosition(p4).setNormal(front).setUvCoordinate(uv01).build(),
                Vertex.builder().setPosition(p5).setNormal(front).setUvCoordinate(uv11).build(),
                Vertex.builder().setPosition(p1).setNormal(front).setUvCoordinate(uv10).build(),
                Vertex.builder().setPosition(p0).setNormal(front).setUvCoordinate(uv00).build(),

                Vertex.builder().setPosition(p6).setNormal(back).setUvCoordinate(uv01).build(),
                Vertex.builder().setPosition(p7).setNormal(back).setUvCoordinate(uv11).build(),
                Vertex.builder().setPosition(p3).setNormal(back).setUvCoordinate(uv10).build(),
                Vertex.builder().setPosition(p2).setNormal(back).setUvCoordinate(uv00).build(),

                Vertex.builder().setPosition(p5).setNormal(right).setUvCoordinate(uv01).build(),
                Vertex.builder().setPosition(p6).setNormal(right).setUvCoordinate(uv11).build(),
                Vertex.builder().setPosition(p2).setNormal(right).setUvCoordinate(uv10).build(),
                Vertex.builder().setPosition(p1).setNormal(right).setUvCoordinate(uv00).build(),

                Vertex.builder().setPosition(p7).setNormal(up).setUvCoordinate(uv01).build(),
                Vertex.builder().setPosition(p6).setNormal(up).setUvCoordinate(uv11).build(),
                Vertex.builder().setPosition(p5).setNormal(up).setUvCoordinate(uv10).build(),
                Vertex.builder().setPosition(p4).setNormal(up).setUvCoordinate(uv00).build()
            );
            for (int j = 0; j < 6; j++) {
                indices.add(box * 24 + 3 + 4 * j);
                indices.add(box * 24 + 1 + 4 * j);
                indices.add(box * 24 + 0 + 4 * j);
                indices.add(box * 24 + 3 + 4 * j);
                indices.add(box * 24 + 2 + 4 * j);
                indices.add(box * 24 + 1 + 4 * j);
            }
        }
        submeshs.add(RenderableDefinition.Submesh.builder()
            .setTriangleIndices(indices.build())
            .setMaterial(material)
            .build());
        return box;
    }

    public static ModelRenderable toRenderable(final List<Geometry> geometries, final Material material) {
        final ImmutableList.Builder<Vertex> vertices = ImmutableList.builder();
        final ImmutableList.Builder<Integer> indices = ImmutableList.builder();
        int vertexIndex = 0;
        for (final Geometry geometry : geometries) {
            final Iterator<Face3> faces = geometry.getFaces().iterator();
            final Iterator<List<Vector2>> uvArray = geometry.getFaceVertexUvs().get(0).iterator();
            while (faces.hasNext() && uvArray.hasNext()) {
                final Face3 face = faces.next();
                final List<Vector2> uv = uvArray.next();
                final int[] v = face.getFlat();
                for (int i = 0; i < v.length; i++) {
                    vertices.add(Vertex.builder()
                        .setPosition(v(geometry.getVertices().get(v[i])))
                        .setUvCoordinate(new Vertex.UvCoordinate(uv.get(i).getX(), uv.get(i).getY()))
                        .setNormal(v(face.getVertexNormals().isEmpty() ? face.getNormal() : face.getVertexNormals().get(i)))
                        .build()
                    );
                    indices.add(vertexIndex++);
                }
            }
        }
        final RenderableDefinition.Submesh mesh = RenderableDefinition.Submesh.builder()
            .setTriangleIndices(indices.build())
            .setMaterial(material)
            .build();
        final RenderableDefinition def = RenderableDefinition.builder()
            .setVertices(vertices.build())
            .setSubmeshes(ImmutableList.of(mesh))
            .build();
        return Futures.getUnchecked(ModelRenderable.builder().setSource(def).build());
    }

    private static Vector3 v(final io.github.formular_team.formular.math.Vector3 vector) {
        return new Vector3(vector.getX(), vector.getY(), vector.getZ());
    }
}
