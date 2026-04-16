package View;

import Components.Triangle;
import Components.Vertex;
import Tools.Matrix3;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DemoViewer {
    public static void main(String[] args){
        JFrame frame = new JFrame("3d Viewer");
        frame.setSize(500,500);
        frame.setVisible(true);
        JLabel label = new JLabel("Ismail is from Turkey");
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());
        pane.add(label);
        // slider to control horizontal rotation
        JSlider headingSlider = new JSlider(0, 360, 180);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // slider to control vertical rotation
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);



        ArrayList<Triangle> tris = new ArrayList<>();
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(-100, 100, -100),
                Color.WHITE));
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, -100),
                Color.RED));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(100, 100, 100),
                Color.GREEN));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(-100, -100, 100),
                Color.BLUE));

        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // rendering magic will happen here

                // ROTATION OVER X
                double heading = Math.toRadians(headingSlider.getValue());
                Matrix3 headingTransform = new Matrix3(new double[] {
                        Math.cos(heading), 0, Math.sin(heading),
                        0, 1, 0,
                        -Math.sin(heading), 0, Math.cos(heading)
                });
                double pitch = Math.toRadians(pitchSlider.getValue());
                Matrix3 pitchTransform = new Matrix3(new double[] {
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch),
                        0, -Math.sin(pitch), Math.cos(pitch)
                });
                Matrix3 transform = headingTransform.multiply(pitchTransform);

                // with Java Graphics Engine
                // g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.WHITE);
                BufferedImage img =
                        new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (Triangle t : tris) {
                    // transform the points due to the rotation
                    Vertex v1 = transform.transform(t.getV1());
                    Vertex v2 = transform.transform(t.getV2());
                    Vertex v3 = transform.transform(t.getV3());


                    // translate to center of screen Manually
                    v1.setX(v1.getX()+getWidth() / 2);
                    v1.setY(v1.getY()+ getHeight() / 2);
                    v2.setX(v2.getX()+getWidth() / 2);
                    v2.setY(v2.getY()+getHeight() / 2);
                    v3.setX(v3.getX()+getWidth() / 2);
                    v3.setY(v3.getY()+getHeight() / 2);
                    //Bounding box : the smallest rectangle that contains the whole Triangle
                    // we search for the left edge of triangle
                    // if triangle is not fully appearing, then left edge's x is negative=> then we don't color it
                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.getX(), Math.min(v2.getX(), v3.getX()))));
                    // searching for right edge
                    int maxX = (int) Math.min(img.getWidth() - 1,
                            Math.floor(Math.max(v1.getX(), Math.max(v2.getX(), v3.getX()))));
                    // same process for Y axis
                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.getY(), Math.min(v2.getY(), v3.getY()))));
                    int maxY = (int) Math.min(img.getHeight() - 1,
                            Math.floor(Math.max(v1.getY(), Math.max(v2.getY(), v3.getY()))));
                    // Wireframe
//                    Path2D path = new Path2D.Double();
//                    // with rotation
//                    path.moveTo(v1.getX(), v1.getY());
//                    path.lineTo(v2.getX(), v2.getY());
//                    path.lineTo(v3.getX(), v3.getY());
//                    // without rotation
////                    path.moveTo(t.getV1().getX(), t.getV1().getY());
////                    path.lineTo(t.getV2().getX(), t.getV2().getY());
////                    path.lineTo(t.getV3().getX(), t.getV3().getY());
//                    path.closePath();
//                    g2.draw(path);
                    // Physical Draw:
                    // ici c l aire du parallelogramme / 2
                    // vu que on va deja / 2 l'aire des triangles a laquel appartient le pixel P
                    // donc c pas la peine de mentionner / 2
                    double triangleArea =
                            (v1.getY() - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - v1.getX());

                    for(int y= minY; y<=maxY;y++){
                        for(int x=minX;x<=maxX;x++){
                            double b1 = ((y - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - x)) / triangleArea;
                            double b2 = ((y - v1.getY()) * (v3.getX() - v1.getX()) + (v3.getY() - v1.getY()) * (v1.getX() - x)) / triangleArea;
                            double b3 = ((y - v2.getY()) * (v1.getX() - v2.getX()) + (v1.getY() - v2.getY()) * (v2.getX() - x)) / triangleArea;
                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                img.setRGB(x, y, t.getColor().getRGB());
                            }
                        }
                    }
                }
                // drawing the canvas
                g2.drawImage(img,0,0,null);
            }
        };
        pane.add(renderPanel, BorderLayout.CENTER);
        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());
//        headingSlider.addChangeListener(e->{
//            renderPanel.repaint();
//        });
    }
}
