package io.github.formular_team.formular.core;

public class KartDefinition {
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

    public float wheelradius;

    public float tireGrip;

    public float caF;

    public float caR;

    public static KartDefinition createDefault() {
        final KartDefinition definition = new KartDefinition();
        definition.b = 0.9F;
        definition.c = 0.82F;
        definition.wheelbase = definition.b + definition.c;
        definition.h = 0.7F;
        definition.mass = 1500.0F;
        definition.inertia = 1500.0F;
        definition.width = 1.5F;
        definition.length = 3.0F;
        definition.wheelradius = 0.7F;
        definition.tireGrip = 2.0F;
        definition.caF = -5.0F;
        definition.caR = -5.2F;
        return definition;
    }
}
