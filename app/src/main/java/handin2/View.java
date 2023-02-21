package handin2;

import java.io.IOException;
import java.util.Iterator;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;

import javax.xml.stream.XMLStreamException;

public class View {
    Canvas canvas = new Canvas(640, 480);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    double x1 = 100;
    double y1 = 100;
    double x2 = 200;
    double y2 = 800;

    Affine trans = new Affine();

    Model model;

    public View(Model model, Stage primaryStage) throws XMLStreamException, IOException, ClassNotFoundException {
        this.model = model;
        primaryStage.setTitle("Draw Lines");
        BorderPane pane = new BorderPane(canvas);
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();
        redraw(model);
        pan(-0.56*model.minlon, model.maxlat);
        zoom(0, 0, canvas.getHeight() / (model.maxlat - model.minlat));
    }

    void redraw(Model model) {
        gc.setTransform(new Affine());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setTransform(trans);
        gc.setLineWidth(1/Math.sqrt(trans.determinant()));
        for (var line : model.lines) {
            line.draw(gc);
        }
        for (var way : model.ways) {
            way.draw(gc);
        }
    }

    void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        redraw(model);
    }

    void zoom(double dx, double dy, double factor) throws XMLStreamException, IOException, ClassNotFoundException {
        pan(-dx, -dy);
        System.out.println(trans.getMxx());
        trans.prependScale(factor, factor);
        pan(dx, dy);
        if (trans.getMxx() < 400){
            model = Model.load("/Users/albert/IdeaProjects/BFST23/app/data/3.obj");
        } else if (trans.getMxx() < 1050){
            model = Model.load("/Users/albert/IdeaProjects/BFST23/app/data/2.obj");
        } else if (trans.getMxx() < 5000){
            model = Model.load("/Users/albert/IdeaProjects/BFST23/app/data/1.obj");
        } else {
            model = Model.load("/Users/albert/IdeaProjects/BFST23/app/data/0.obj");
        }
        redraw(model);
    }

    public Point2D mousetoModel(double lastX, double lastY) {
        try {
            return trans.inverseTransform(lastX, lastY);
        } catch (NonInvertibleTransformException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }

    }
}