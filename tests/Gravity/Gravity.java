import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class Vector {
    double x;
    double y;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector add(Vector other) {
        return new Vector(this.x + other.x, this.y + other.y);
    }

    public Vector scale(double scalar) {
        return new Vector(this.x * scalar, this.y * scalar);
    }
    
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double dot(Vector other) {
        return this.x * other.x + this.y * other.y;
    }
}

class Ball {
    double radius; // in meters
    Vector position = new Vector(0, 0);
    Vector velocity = new Vector(0, 0);

    public Ball(double radius, Vector initialPosition, Vector initialVelocity) {
        this.radius = radius;
        this.position.x = initialPosition.x;
        this.position.y = initialPosition.y;
        this.velocity.x = initialVelocity.x;
        this.velocity.y = initialVelocity.y;
    }

    public void updatePosition(double timeInterval, Vector gravity, int windowWidth, int windowHeight, double dampingFactor) {
        this.position.x += this.velocity.x * timeInterval + 0.5 * gravity.x * timeInterval * timeInterval;
        this.position.y += this.velocity.y * timeInterval + 0.5 * gravity.y * timeInterval * timeInterval;
        this.velocity.x += gravity.x * timeInterval;
        this.velocity.y += gravity.y * timeInterval;
        

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

}


public class Gravity {
    public static void main(String[] args) {
        System.out.println("Gravity module is active.");

        double angle = Math.toRadians(90);
        double dt = 0.16;
        double g = 9.81;
        double damp = 1;
        int startx = 400;
        int starty = 300;
        Vector gravity = new Vector(Math.cos(angle) * g, Math.sin(angle) * g);
        ArrayList<Ball> balls = new ArrayList<>();
        balls.add(new Ball(20, new Vector(startx, starty), new Vector(50, 0)));
        balls.add(new Ball(40, new Vector(startx - 100, starty - 100), new Vector(100, -20)));
        balls.add(new Ball(10, new Vector(startx + 50, starty + 50), new Vector(20, 10)));

        int width = 800;
        int height = 500;

        JFrame f = new JFrame("Swing Graphics");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(width, height);

        JPanel panel = new JPanel() {
            {
                new Timer(16, e -> { // ~165 FPS
                    if (getWidth() == 0 || getHeight() == 0) return;
                    for (Ball b : balls) {
                        b.updatePosition(dt, gravity, getWidth(), getHeight(), damp);
                    }
                    repaint();
                }).start();
            }
            
            @Override protected void paintComponent(Graphics gr) {
                super.paintComponent(gr);
                Graphics2D g = (Graphics2D) gr;
                for (Ball ball : balls) g.fillOval((int)ball.position.x - (int)ball.radius, (int)ball.position.y - (int)ball.radius, (int)(2 * ball.radius), (int)(2 * ball.radius));
            }
        };

        f.add(panel);
        f.setVisible(true);
    }
}
