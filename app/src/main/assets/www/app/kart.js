define(function(require) {
    function Kart(mass) {
        this.mass = mass;
        return this;
    }

    Kart.prototype.tick = function() {};

    Kart.prototype.getMass = function() {
        return this.mass
    };

    Kart.prototype.setMass = function(mass) {
        return this.mass = mass;
    };

    return Kart;
});
