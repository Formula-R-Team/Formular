package io.github.formular_team.formular.car;

public class CarDefinition {

    private static final float CA_R = -5.2F; // cornering stiffness

    private static final float CA_F = -5.0F; // cornering stiffness
    // wheelbase in m
    public float wheelbase;
    // in m, distance from CG to front axle
    public float b;
    // in m, idem to rear axle
    public float c;
    // in m, height of CM from ground
    public float h;
    // in kg
    public float mass;
    // in kg.m
    public float inertia;

    // m, must be greater than wheelbase
    public float length;

    public float width;

    public float wheellength;

    public float wheelwidth;

    public static CarDefinition createDefault() {
        final CarDefinition definition = new CarDefinition();
        definition.b = 0.9F;
        definition.c = 0.82F;
        definition.wheelbase = definition.b + definition.c;
        definition.h = 0.7F;
        definition.mass = 1500.0F;
        definition.inertia = 1500.0F;
        definition.width = 1.5F;
        definition.length = 3.0F;
        definition.wheellength = 0.7F;
        definition.wheelwidth = 0.3F;
        return definition;
    }

    public float getCaR(){
        return CA_R;
    }

    public float getCaF(){
        return CA_F;
    }
}
