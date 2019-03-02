package io.github.formular_team.formular.server;

import io.github.formular_team.formular.math.Matrix3;

public final class SceneryItem {

    private NamespacedString mType;

    private Matrix3 mTransform;

    SceneryItem(NamespacedString type, Matrix3 transform){
        mType = type;
        mTransform = transform;
    }

    public NamespacedString getType(){
        return mType;
    }

    public Matrix3 getTransform(){
        return mTransform;
    }

    public void setType(NamespacedString type){
        mType = type;
    }

    public void setTransform(Matrix3 transform){
        mTransform = transform;
    }
}
