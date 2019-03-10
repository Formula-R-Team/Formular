package io.github.formular_team.formular.car;

public class CarDefinition {
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
}
