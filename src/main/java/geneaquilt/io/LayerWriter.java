package geneaquilt.io;

import geneaquilt.data.Network;
import geneaquilt.data.Vertex;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Class LayerWriter
 * 
 * @author Jean-Daniel Fekete
 */
public class LayerWriter {
    Network network;
    
    /**
     * Creates a LayerWriter for a specified network
     * @param network the network
     */
    public LayerWriter(Network network) {
        this.network = network;
    }
    
    /**
     * Writes the layers into a specified file
     * @param filename the file name
     * @throws IOException if a writing error occurs
     */
    public void write(String filename) throws IOException {
        PrintWriter out = new PrintWriter(filename);
        try {
            write(out);
        }
        finally {
            out.close();
        }
    }
    
    /**
     * Write the layers into a specified PrintWriter.
     * @param out the writer
     * @throws IOException if a writing error occurs
     */
    public void write(PrintWriter out) throws IOException {
        for (Vertex v : network.getVertices()) {
            out.print(v.getId());
            out.print(" ");
            out.print(v.getX());
            out.print(" ");
            out.print(v.getLayer());
            out.println();
        }
    }
}
