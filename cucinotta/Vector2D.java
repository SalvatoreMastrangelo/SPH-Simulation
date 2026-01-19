class Vector2D {
    double x;
    double y;
    double mod;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
        this.mod = Math.sqrt(x * x + y * y);
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    public Vector2D scale(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }
    
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double dot(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    public void normalize() {
        double mag = magnitude();
        if (mag != 0) {
            this.x /= mag;
            this.y /= mag;
        } else {
            System.err.println("Cannot normalize zero vector");
        }
    }
}