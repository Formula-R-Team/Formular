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

    public float frontWheelRadius;

    public float rearWheelRadius;

    public float tireGrip;

    public float caF;

    public float caR;

    public static KartDefinition createKart2() {
        final KartDefinition definition = new KartDefinition();
        definition.wheelbase = inchToMeter(2.0F * 16.8F);
        final float t = 0.52F;
        definition.b = definition.wheelbase * t;
        definition.c = definition.wheelbase * (1.0F - t);
        definition.h = 0.2F;
        definition.mass = 82.0F;
        definition.inertia = 82.0F;
        definition.width = inchToMeter(2.0F * 10.8F);
        definition.length = inchToMeter(2.0F * 18.0F);
        definition.frontWheelRadius = inchToMeter(4.8F);
        definition.rearWheelRadius = inchToMeter(5.4F);
        definition.tireGrip = 2.0F;
        definition.caF = -4.8F;
        definition.caR = -5.0F;
        return definition;
    }

    public static float inchToMeter(final float x) {
        return 0.0254F * x;
    }
}
