import java.awt.*;

class Particle {
    double smoothRadius;
    int radius;
    double mass;
    double density; 
    double pressure;
    double dampingFactor = 0.75;
    Vector2D position = new Vector2D(0, 0);
    Vector2D velocity = new Vector2D(0, 0);
    Vector2D acceleration = new Vector2D(0, 0);
    Vector2D cell = new Vector2D(0, 0);
    long hash;

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

    private void boudaryForce(int windowWidth, int windowHeight) {
        double boundaryStiffness = 1000.0;
        double boundaryDamping = 50.0;

        if (this.position.x < this.radius) {
            double penetration = this.radius - this.position.x;
            double forceX = boundaryStiffness * penetration - boundaryDamping * this.velocity.x;
            applyForce(new Vector2D(forceX, 0));
        } else if (this.position.x > windowWidth - this.radius) {
            double penetration = this.position.x - (windowWidth - this.radius);
            double forceX = -boundaryStiffness * penetration - boundaryDamping * this.velocity.x;
            applyForce(new Vector2D(forceX, 0));
        }

        if (this.position.y < this.radius) {
            double penetration = this.radius - this.position.y;
            double forceY = boundaryStiffness * penetration - boundaryDamping * this.velocity.y;
            applyForce(new Vector2D(0, forceY));
        } else if (this.position.y > windowHeight - this.radius) {
            double penetration = this.position.y - (windowHeight - this.radius);
            double forceY = -boundaryStiffness * penetration - boundaryDamping * this.velocity.y;
            applyForce(new Vector2D(0, forceY));
        }
    }
    public void computeHash() {
        this.cell.x = Math.floor(this.position.x / this.smoothRadius);
        this.cell.y = Math.floor(this.position.y / this.smoothRadius);
        this.hash = Utils.getHash((int)this.cell.x, (int)this.cell.y);
    }

    public void update(int windowWidth, int windowHeight,double dt, Vector2D gravity) {
        boudaryForce(windowWidth, windowHeight);
        this.position.x += this.velocity.x * dt + 0.5 * this.acceleration.x * dt * dt;
        this.position.y += this.velocity.y * dt + 0.5 * this.acceleration.y * dt * dt;
        this.velocity.x += this.acceleration.x * dt;
        this.velocity.y += this.acceleration.y * dt;      

        // if (this.position.x < this.radius) {
        //     this.position.x = this.radius;
        //     this.velocity.x = -this.velocity.x;
        //     this.velocity.x *= Math.pow(dampingFactor, 20);
        // } else if (this.position.x > windowWidth - this.radius) {
        //     this.position.x = windowWidth - this.radius;
        //     this.velocity.x = -this.velocity.x;
        //     this.velocity.x *= Math.pow(dampingFactor, 20);
        // }
        // if (this.position.y < this.radius) {
        //     this.position.y = this.radius;
        //     this.velocity.y = -this.velocity.y;
        //     this.velocity.y *= Math.pow(dampingFactor, 20);
        // } else if (this.position.y > windowHeight - this.radius) {
        //     this.position.y = windowHeight - this.radius;
        //     this.velocity.y = -this.velocity.y;
        //     this.velocity.y *= Math.pow(dampingFactor, 20);
        // }

        this.velocity.x *= dampingFactor;
        this.velocity.y *= dampingFactor;
        this.acceleration.x = 0;
        this.acceleration.y = 0;
        this.cell.x = Math.floor(this.position.x / this.smoothRadius);
        this.cell.y = Math.floor(this.position.y / this.smoothRadius);    
    }

    public void show(Graphics2D g) {
        double speed = velocity.magnitude();
        double maxSpeed = 3.5;

        double t = speed / maxSpeed;
        if (t > 1) t = 1;

        // Map speed to a heatmap: blue (cold) -> red (hot)
        float hue = (float) ((2.0 / 3.0) * (1.0 - t)); // 2/3=blue, 0=red
        Color heatColor = Color.getHSBColor(hue, 1.0f, 1.0f);

        g.setColor(heatColor);
        g.fillOval((int)position.x - (int)radius, (int)position.y - (int)radius, (int)(2 * radius), (int)(2 * radius));
    }

}