package handin2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.zip.ZipInputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import javafx.geometry.Point2D;

public class Model implements Serializable {
    List<Line> lines = new ArrayList<Line>();
    List<Way> ways = new ArrayList<Way>();

    private int fidelity = 0;

    double minlat, maxlat, minlon, maxlon;

    static Model load(String filename) throws FileNotFoundException, IOException, ClassNotFoundException, XMLStreamException, FactoryConfigurationError {
        if (filename.endsWith(".obj")) {
            try (var in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                return (Model) in.readObject();
            }
        }
        return new Model(filename);
    }
    

    public Model(String filename) throws XMLStreamException, FactoryConfigurationError, IOException {
        if (filename.endsWith(".osm.zip")) {
            parseZIP(filename);
        } else if (filename.endsWith(".osm")) {
            parseOSM(filename);
        } else {
            parseTXT(filename);
        }
        save("/Users/albert/IdeaProjects/BFST23/app/data/"+0+".obj");
    }

    void save(String filename) throws IOException {
        try (var out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        }
    }

    private void parseZIP(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        var input = new ZipInputStream(new FileInputStream(filename));
        input.getNextEntry();
        parseOSM(input);
    }

    private void parseOSM(String filename) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        parseOSM(new FileInputStream(filename));
    }

    private void parseOSM(InputStream inputStream) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        var input = XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(inputStream));
        var id2node = new HashMap<Long, Node>();
//        System.out.println("hello");
        var way = new ArrayList<Node>();
        var coast = false;
        var set = new HashSet<>();
        if (fidelity < 1) {
            //low level
            set.add("unclassified");
            set.add("residential");
        }
        if (fidelity < 2) {
            //medium level
            set.add("secondary");
            set.add("tertiary");
        }
        if (fidelity < 3) {
            //semi high
            set.add("trunk");
            set.add("primary");
        }
        if (fidelity < 4) {
            //high level
            set.add("coastline");
            set.add("motorway");
        }


        while (input.hasNext()) {
            var tagKind = input.next();
            if (tagKind == XMLStreamConstants.START_ELEMENT) {
                var name = input.getLocalName();
                if (name == "bounds") {
                    minlat = Double.parseDouble(input.getAttributeValue(null, "minlat"));
                    maxlat = Double.parseDouble(input.getAttributeValue(null, "maxlat"));
                    minlon = Double.parseDouble(input.getAttributeValue(null, "minlon"));
                    maxlon = Double.parseDouble(input.getAttributeValue(null, "maxlon"));
                } else if (name == "node") {
                    var id = Long.parseLong(input.getAttributeValue(null, "id"));
                    var lat = Double.parseDouble(input.getAttributeValue(null, "lat"));
                    var lon = Double.parseDouble(input.getAttributeValue(null, "lon"));
                    id2node.put(id, new Node(lat, lon));
                } else if (name == "way") {
                    way.clear();
                    coast = false;
                } else if (name == "tag") {
                    var v = input.getAttributeValue(null, "v");
                    if (set.contains(v)) {
                        coast = true;
                    }

                } else if (name == "nd") {
                    var ref = Long.parseLong(input.getAttributeValue(null, "ref"));
                    var node = id2node.get(ref);
                    way.add(node);
                }
            } else if (tagKind == XMLStreamConstants.END_ELEMENT) {
                var name = input.getLocalName();
                if (name == "way" && coast) {
                    ways.add(new Way(way));
                }
            }
        }
    }

    private void parseTXT(String filename) throws FileNotFoundException {
        File f = new File(filename);
        try (Scanner s = new Scanner(f)) {
            while (s.hasNext()) {
                lines.add(new Line(s.nextLine()));
            }
        }
    }

    public void add(Point2D p1, Point2D p2) {
        lines.add(new Line(p1, p2));
    }

    public int getFidelity() {
        return fidelity;
    }

    public void setFidelity(int fidelity) {
        this.fidelity = fidelity;
    }
}
