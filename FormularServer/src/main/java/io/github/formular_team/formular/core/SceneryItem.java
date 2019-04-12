package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.math.Matrix3;

public final class SceneryItem {
    private final NamespacedString type;

    private final Matrix3 transform;

    private SceneryItem(final NamespacedString type, final Matrix3 transform){
        this.type = type;
        this.transform = transform;
    }

    public NamespacedString getType(){
        return this.type;
    }

    public Matrix3 getTransform(){
        return this.transform;
    }

    public static SceneryItem create(final NamespacedString type, final Matrix3 transform) {
        return new SceneryItem(type, transform);
    }
}
