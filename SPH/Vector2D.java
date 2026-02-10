class Vector2D {
    public double x;
    public double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    public Vector2D scale(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    public Vector2D addThis(Vector2D other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vector2D scaleThis(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }
    
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double magnitudeSquared() {
        return x * x + y * y;
    }

    public double dot(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    public Vector2D normalize() {
        double mag = magnitude();
        if (mag != 0) {
            return new Vector2D(this.x / mag, this.y / mag);
        } else {
            System.err.println("Cannot normalize zero vector");
            return new Vector2D(0, 0);
        }
    }

    public Vector2D normalizeThis() {
        double mag = magnitude();
        if (mag != 0) {
            this.x /= mag;
            this.y /= mag;
        } else {
            System.err.println("Cannot normalize zero vector");
            this.x = 0;
            this.y = 0;
        }
        return this;
    }
}