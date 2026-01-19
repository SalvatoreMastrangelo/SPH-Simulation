import java.awt.*;

class Particle {
    double smoothRadius; // in meters
    int radius;
    double mass;
    double density; 
    double pressure;
    double dampingFactor = 0.75;
    Vector2D position = new Vector2D(0, 0);
    Vector2D velocity = new Vector2D(0, 0);
    Vector2D acceleration = new Vector2D(0, 0);

    public Particle(int radius, Vector2D initialPosition, Vector2D initialVelocity, double smoothRadius, double mass, double dampingFactor) {
        this.radius = radius;
        this.smoothRadius = smoothRadius;
        this.mass = mass;
        this.density = 0;
        this.position.x = initialPosition.x;
        this.position.y = initialPosition.y;
        this.velocity.x = initialVelocity.x;
        this.velocity.y = initialVelocity.y;
        this.dampingFactor = dampingFactor;
    }

    public void applyForce(Vector2D force) {
        this.acceleration.x += force.x / this.mass;
        this.acceleration.y += force.y / this.mass;
    }

    public void update(int windowWidth, int windowHeight,double dt, Vector2D gravity) {
        this.position.x += this.velocity.x * dt + 0.5 * this.acceleration.x * dt * dt;
        this.position.y += this.velocity.y * dt + 0.5 * this.acceleration.y * dt * dt;
        this.velocity.x += this.acceleration.x * dt;
        this.velocity.y += this.acceleration.y * dt;      

        if (this.position.x < this.radius) {
            this.position.x = this.radius;
            this.velocity.x = -this.velocity.x;
            this.velocity.x *= Math.pow(dampingFactor, 10);
        } else if (this.position.x > windowWidth - this.radius) {
            this.position.x = windowWidth - this.radius;
            this.velocity.x = -this.velocity.x;
            this.velocity.x *= Math.pow(dampingFactor, 10);
        }
        if (this.position.y < this.radius) {
            this.position.y = this.radius;
            this.velocity.y = -this.velocity.y;
            this.velocity.y *= Math.pow(dampingFactor, 10);
        } else if (this.position.y > windowHeight - this.radius) {
            this.position.y = windowHeight - this.radius;
            this.velocity.y = -this.velocity.y;
            this.velocity.y *= Math.pow(dampingFactor, 10);
        }

        this.velocity.x *= dampingFactor;
        this.velocity.y *= dampingFactor;
        this.acceleration.x = 0;
        this.acceleration.y = 0;
    }

    public void show(Graphics2D g) {
        g.fillOval((int)position.x - (int)radius, (int)position.y - (int)radius, (int)(2 * radius), (int)(2 * radius));
    }

}