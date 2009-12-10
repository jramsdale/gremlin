package com.tinkerpop.gremlin.model;

import org.openrdf.model.Literal;
import org.openrdf.model.impl.LiteralImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class VertexTestSuite extends ModelTestSuite {

    public VertexTestSuite(SuiteConfiguration config) {
        super(config);
    }

    public void testAddVertex(Graph graph) {
        if (config.supportsVertexIteration) {
            Vertex v1 = graph.addVertex(convertId("1"));
            Vertex v2 = graph.addVertex(convertId("2"));
            assertEquals(countIterator(graph.getVertices()), 2);
            graph.addVertex(v1.getId());
            assertEquals(countIterator(graph.getVertices()), 2);
            Vertex v3 = graph.addVertex(convertId("3"));
            assertEquals(countIterator(graph.getVertices()), 3);
            graph.addVertex(v2.getId());
            assertEquals(countIterator(graph.getVertices()), 3);
            graph.addVertex(v2.getId());
            assertEquals(countIterator(graph.getVertices()), 3);
            graph.addVertex(v3.getId());
            assertEquals(countIterator(graph.getVertices()), 3);
        }
    }

    public void testRemoveVertex(Graph graph) {
        Vertex v1 = graph.addVertex(convertId("1"));
        if (config.supportsVertexIteration)
            assertEquals(countIterator(graph.getVertices()), 1);
        graph.removeVertex(v1);
        if (config.supportsVertexIteration)
            assertEquals(countIterator(graph.getVertices()), 0);

        Set<Vertex> vertices = new HashSet<Vertex>();
        for (int i = 0; i < 1000; i++) {
            vertices.add(graph.addVertex(null));
        }
        if (config.supportsVertexIteration)
            assertEquals(countIterator(graph.getVertices()), 1000);
        for (Vertex v : vertices) {
            graph.removeVertex(v);
        }
        if (config.supportsVertexIteration)
            assertEquals(countIterator(graph.getVertices()), 0);
    }

    public void testRemoveVertexNullId(Graph graph) {
        Vertex v1 = graph.addVertex(null);
        if (config.supportsVertexIteration)
            assertEquals(countIterator(graph.getVertices()), 1);
        graph.removeVertex(v1);
        if (config.supportsVertexIteration)
            assertEquals(countIterator(graph.getVertices()), 0);

        Set<Vertex> vertices = new HashSet<Vertex>();
        for (int i = 0; i < 1000; i++) {
            vertices.add(graph.addVertex(null));
        }
        if (config.supportsVertexIteration)
            assertEquals(countIterator(graph.getVertices()), 1000);
        for (Vertex v : vertices) {
            graph.removeVertex(v);
        }
        if (config.supportsVertexIteration)
            assertEquals(countIterator(graph.getVertices()), 0);

    }

    public void testVertexIterator(Graph graph) {
        if (config.supportsVertexIteration) {
            for (int i = 0; i < 1000; i++) {
                graph.addVertex(null);
            }
            assertEquals(countIterator(graph.getVertices()), 1000);
        }
    }

    public void testAddVertexProperties(Graph graph) {
        if (!config.isRDFModel) {
            Vertex v1 = graph.addVertex(convertId("1"));
            Vertex v2 = graph.addVertex(convertId("2"));

            v1.setProperty("key1", "value1");
            v1.setProperty("key2", 10);
            v2.setProperty("key2", 20);

            assertEquals(v1.getProperty("key1"), "value1");
            assertEquals(v1.getProperty("key2"), 10);
            assertEquals(v2.getProperty("key2"), 20);
        } else {
            Vertex v1 = graph.addVertex("\"1\"^^<http://www.w3.org/2001/XMLSchema#int>");
            assertEquals(v1.getProperty("datatype"), "http://www.w3.org/2001/XMLSchema#int");
            assertNull(v1.getProperty("language"));
            assertNull(v1.getProperty("random something"));

            Vertex v2 = graph.addVertex("\"hello\"@en");
            assertEquals(v2.getProperty("language"), "en");
            assertNull(v2.getProperty("datatype"));
            assertNull(v2.getProperty("random something"));
        }
    }

    /*public static void testRemoveVertexProperties(Graph graph) {
        Vertex v1 = graph.addVertex("1");
        Vertex v2 = graph.addVertex("2");
        v1.setProperty("key1", "value1");
        v1.setProperty("key2", 10);
        v2.setProperty("key2", 20);

        assertEquals(v1.removeProperty("key1"), "value1");
        assertEquals(v1.removeProperty("key2"), 10);
        assertEquals(v2.removeProperty("key2"), 20);

        assertNull(v1.removeProperty("key1"));
        assertNull(v1.removeProperty("key2"));
        assertNull(v2.removeProperty("key2"));

    }

    */


}