import java.awt.*;

class Particle {
    double smoothRadius; // in meters
    double radius = 4;
    double mass;
    double density; 
    double pressure;
    double dampingFactor = 0.75;
    Vector2D position = new Vector2D(0, 0);
    Vector2D velocity = new Vector2D(0, 0);

    public Particle(Vector2D initialPosition, Vector2D initialVelocity, double smoothRadius, double mass) {
        this.smoothRadius = smoothRadius;
        this.mass = mass;
        this.density = 0;
        this.position.x = initialPosition.x;
        this.position.y = initialPosition.y;
        this.velocity.x = initialVelocity.x;
        this.velocity.y = initialVelocity.y;
    }

    public void applyGravity(double dt, Vector2D gravity) {
        this.position.x += this.velocity.x * dt + 0.5 * gravity.x * dt * dt;
        this.position.y += this.velocity.y * dt + 0.5 * gravity.y * dt * dt;
        this.velocity.x += gravity.x * dt;
        this.velocity.y += gravity.y * dt;
    }

    public void update(int windowWidth, int windowHeight,double dt, Vector2D gravity) {
        applyGravity(dt, gravity);        

        if (this.position.x < this.radius) {
            this.position.x = this.radius;
            this.velocity.x = -this.velocity.x;
            this.velocity.y *= dampingFactor;
            this.velocity.x *= dampingFactor;
        } else if (this.position.x > windowWidth - this.radius) {
            this.position.x = windowWidth - this.radius;
            this.velocity.x = -this.velocity.x;
            this.velocity.y *= dampingFactor;
            this.velocity.x *= dampingFactor;
        }
        if (this.position.y < this.radius) {
            this.position.y = this.radius;
            this.velocity.y = -this.velocity.y;
            this.velocity.y *= dampingFactor;
            this.velocity.x *= dampingFactor;
        } else if (this.position.y > windowHeight - this.radius) {
            this.position.y = windowHeight - this.radius;
            this.velocity.y = -this.velocity.y;
            this.velocity.y *= dampingFactor;
            this.velocity.x *= dampingFactor;
        }
    }

    public void show(Graphics2D g) {
        g.fillOval((int)position.x - (int)radius, (int)position.y - (int)radius, (int)(2 * radius), (int)(2 * radius));
    }

}