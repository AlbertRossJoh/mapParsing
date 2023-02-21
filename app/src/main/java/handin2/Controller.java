package handin2;

import javafx.geometry.Point2D;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class Controller {
    double lastX;
    double lastY;

    public Controller(Model model, View view) {
        view.canvas.setOnMousePressed(e -> {
            lastX = e.getX();
            lastY = e.getY();
        });
        view.canvas.setOnMouseDragged(e -> {
//            if (e.isPrimaryButtonDown()) {
//                Point2D lastmodel = view.mousetoModel(lastX, lastY);
//                Point2D newmodel = view.mousetoModel(e.getX(), e.getY());
//                model.add(lastmodel, newmodel);
//                view.redraw();
//            } else {
//            }
                double dx = e.getX() - lastX;
                double dy = e.getY() - lastY;
                view.pan(dx, dy);
                view.redraw(view.model);

            lastX = e.getX();
            lastY = e.getY();
        });
        view.canvas.setOnScroll(e -> {
            double factor = e.getDeltaY();
            try {
                view.zoom(e.getX(), e.getY(), Math.pow(1.01, factor));
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

}
