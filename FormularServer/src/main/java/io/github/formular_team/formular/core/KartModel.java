package io.github.formular_team.formular.core;

import io.github.formular_team.formular.core.math.Intersections;
import io.github.formular_team.formular.core.math.LineCurve;
import io.github.formular_team.formular.core.math.Mth;
import io.github.formular_team.formular.core.math.Vector2;

public class KartModel implements Kart {
    private static final float GRAVITY = 9.8F; // m/s^2

    private static final float DRAG = 5.0F; // factor for air resistance (drag)

    private static final float RESISTANCE = 30.0F; // factor for rolling resistance

    private final GameModel game;

    private final int uniqueId;

    private final KartDefinition definition;

    // position of car center in world coordinates
    private final Vector2 position = new Vector2();

    // velocity vector of car in world coordinates
    private final Vector2 linearVelocity = new Vector2();

    // angle of car body orientation (in rads)
    private float rotation;

    private float angularVelocity;

    private float steerangle;

    private float throttle;

    private float brake;

    private float wheelAngularVelocity;

    public KartModel(final GameModel game, final int uniqueId, final KartDefinition type) {
        this.game = game;
        this.uniqueId = uniqueId;
        this.definition = type;
    }

    @Override
    public KartDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public int getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public void setPosition(final Vector2 position) {
        this.position.copy(position);
    }

    @Override
    public Vector2 getPosition() {
        return this.position;
    }

    @Override
    public void setRotation(final float rotation) {
        this.rotation = rotation;
    }

    @Override
    public float getRotation() {
        return this.rotation;
    }

    @Override
    public float getWheelAngularVelocity() {
        return this.wheelAngularVelocity;
    }

    @Override
    public Kart.ControlState getControlState() {
        return new ControlState();
    }

    private class ControlState implements Kart.ControlState {
        @Override
        public void setThrottle(final float throttle) {
            KartModel.this.throttle = throttle;
        }

        @Override
        public float getThrottle() {
            return KartModel.this.throttle;
        }

        @Override
        public void setBrake(final float brake) {
            KartModel.this.brake = brake;
        }

        @Override
        public float getBrake() {
            return KartModel.this.brake;
        }

        @Override
        public void setSteeringAngle(final float steeringAngle) {
            KartModel.this.steerangle = steeringAngle;
        }

        @Override
        public float getSteeringAngle() {
            return KartModel.this.steerangle;
        }
    }

    public void reset() {
        this.steerangle = 0.0F;
        this.throttle = 0.0F;
        this.brake = 0.0F;
        this.linearVelocity.set(0.0F, 0.0F);
        this.angularVelocity = 0.0F;
        this.wheelAngularVelocity = 0.0F;
    }

    public void step(final float dt) {
        final float sn = Mth.sin(this.rotation);
        final float cs = Mth.cos(this.rotation);
        // SAE convention: x is to the front of the car, y is to the right, z is down
        // transform velocity in world reference frame to velocity in car reference frame
        final Vector2 velocity = new Vector2(
            cs * this.linearVelocity.getY() + sn * this.linearVelocity.getX(),
            -sn * this.linearVelocity.getY() + cs * this.linearVelocity.getX()
        );

        this.wheelAngularVelocity = velocity.getX() / this.definition.wheelradius;

        // Lateral force on wheels
        //
        // Resulting velocity of the wheels as result of the yaw rate of the car body
        // v = yawrate * r where r is distance of wheel to CG (approx. half wheel base)
        // yawrate (ang.velocity) must be in rad/s
        //
        // todo front/rear yaw speed
        final float yawSpeed = this.definition.wheelbase * 0.5F * this.angularVelocity;

        // Calculate slip angles for front and rear wheels (a.k.a. alpha)
        final float slipanglefront = Mth.atan2(velocity.getY() + yawSpeed, Math.abs(velocity.getX())) - Math.signum(velocity.getX()) * this.steerangle;
        final float slipanglerear = Mth.atan2(velocity.getY() - yawSpeed, Math.abs(velocity.getX()));

        // weight per axle = half car mass times 1G (=9.8m/s^2)
        final float weight = this.definition.mass * GRAVITY * 0.5F;

        final float fTireGrip = this.definition.tireGrip;
        final float rTireGrip = this.definition.tireGrip;

        // TODO: weight transfer
        // lateral force on front wheels = (Ca * slip angle) capped to friction circle * load
        final Vector2 flatf = new Vector2(0.0F, Mth.clamp(this.definition.caF * slipanglefront, -fTireGrip, fTireGrip) * weight);

        // lateral force on rear wheels
        final Vector2 flatr = new Vector2(0.0F, Mth.clamp(this.definition.caR * slipanglerear, -rTireGrip, rTireGrip) * weight);

        // longitudinal force on rear wheels - very simple traction model
        final Vector2 ftraction = new Vector2(100 * (this.throttle - this.brake * Math.signum(velocity.getX())), 0.0F);

        //
        // Forces and torque on body
        //

        // drag and rolling resistance
        final Vector2 resistance = new Vector2(
            -(RESISTANCE * velocity.getX() + DRAG * velocity.getX() * Math.abs(velocity.getX())),
            -(RESISTANCE * velocity.getY() + DRAG * velocity.getY() * Math.abs(velocity.getY()))
        );

        // sum forces
        final Vector2 force = new Vector2(
            ftraction.getX() + resistance.getX(),
            ftraction.getY() + Mth.cos(this.steerangle) * flatf.getY() + flatr.getY() + resistance.getY()
        );

        final float torque = this.definition.b * flatf.getY() - this.definition.c * flatr.getY();

        final Vector2 linearAcceleration = new Vector2(
            force.getX() / this.definition.mass,
            force.getY() / this.definition.mass
        );
        final Vector2 linearAccelerationWC = new Vector2(
            cs * linearAcceleration.getY() + sn * linearAcceleration.getX(),
            -sn * linearAcceleration.getY() + cs * linearAcceleration.getX()
        );
        this.linearVelocity.add(linearAccelerationWC.multiply(dt));
        final float angularAcceleration = torque / this.definition.inertia;
        this.angularVelocity += dt * angularAcceleration;

        if (this.linearVelocity.length() < 0.5F && Math.abs(this.throttle) < 1e-6F) {
            this.linearVelocity.set(0.0F, 0.0F);
            this.angularVelocity = 0.0F;
        }

        final Vector2 oldPosition = this.position.clone();
        this.position.add(this.linearVelocity.clone().multiply(dt));
        this.rotation += dt * this.angularVelocity;

        // TODO: good collision
        for (final LineCurve wall : this.game.getWalls()) {
            if (Intersections.lineCircle(wall.getStart(), wall.getEnd(), this.position, this.definition.length * 0.4F)) {
                final Vector2 normal = wall.getEnd().sub(wall.getStart()).normalize().rotateAround(new Vector2(), 0.5F * Mth.PI);
                final float dot = this.linearVelocity.dot(normal);
                // orient to direction kart is hitting
                if (dot > 0.0F) {
                    normal.negate();
                }
                this.linearVelocity.reflect(normal).multiply(0.2F);
                this.position.copy(oldPosition);
                this.position.add(this.linearVelocity.clone().multiply(dt));
                this.angularVelocity = -this.angularVelocity;
                break;
            }
        }
    }
}
