package io.github.formular_team.formular;

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
import io.github.formular_team.formular.math.Vector3;

public final class Geometries {
    private Geometries() {}

    public static ModelRenderable toRenderable(final Geometry geometry, final Material material) {
        final List<Vector3> vertexArray = geometry.getVertices();
        final List<Face3> faces = geometry.getFaces();
        final Iterator<List<Vector2>> uvArray = geometry.getFaceVertexUvs().get(0).iterator();
        // TODO remove duplicates
        final ImmutableList.Builder<Vertex> vertices = ImmutableList.builder();
        final ImmutableList.Builder<Integer> indices = ImmutableList.builder();
        int i = 0;
        for (final Face3 face : faces) {
            final List<Vector2> uv = uvArray.next();
            final int[] v = face.getFlat();
            for (int j = 0; j < 3; j++) {
                vertices.add(Vertex.builder()
                    .setPosition(v(vertexArray.get(v[j])))
                    .setUvCoordinate(new Vertex.UvCoordinate(uv.get(j).getX(), uv.get(j).getY()))
                    .setNormal(v(face.getNormal()))
                    .build()
                );
                indices.add(i++);
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

    private static com.google.ar.sceneform.math.Vector3 v(final Vector3 vector) {
        return new com.google.ar.sceneform.math.Vector3(vector.getX(), vector.getY(), vector.getZ());
    }
}
