package io.github.formular_team.formular.math;

public final class Interpolations
{
    private static float CubicBezierP0(float t,float p){
        float k = 1-t;
        return k*k*k*p;
    }
    private static float CubicBezierP1(float t,float p){
        float k = 1-t;
        return 3 * k * k * t * p;
    }
    private static float CubicBezierP2(float t,float p){
        return 3 * ( 1 - t ) * t * t * p;
    }
    private static float CubicBezierP3(float t,float p){
        return t * t * t * p;
    }
    public static float CubicBezier(float t, float p0, float p1, float p2, float p3)
    {
        return CubicBezierP0(t,p0) + CubicBezierP1(t,p1) +CubicBezierP2(t,p2) +CubicBezierP3(t,p3);
    }
}
