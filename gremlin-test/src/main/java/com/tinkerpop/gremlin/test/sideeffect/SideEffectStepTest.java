package com.tinkerpop.gremlin.test.sideeffect;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.pipes.Pipe;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SideEffectStepTest extends TestCase {

    public void testCompliance() {
        assertTrue(true);
    }

    public void test_g_v1_sideEffectXstore_aX_propertyXnameX(final Pipe<Vertex, String> pipe) {
        assertEquals(pipe.next(), "marko");
        assertFalse(pipe.hasNext());
    }

    public void test_g_v1_out_sideEffectXincr_cX_propertyXnameX(final Pipe<Vertex, String> pipe) {
        List<String> names = new ArrayList<String>();
        while (pipe.hasNext()) {
            names.add(pipe.next());
        }
        assertEquals(names.size(), 3);
        assertTrue(names.contains("josh"));
        assertTrue(names.contains("lop"));
        assertTrue(names.contains("vadas"));
    }

    public void test_g_v1_out_sideEffectXfalseX_propertyXnameX(final Pipe<Vertex, String> pipe) {
        this.test_g_v1_out_sideEffectXincr_cX_propertyXnameX(pipe);
    }

}
