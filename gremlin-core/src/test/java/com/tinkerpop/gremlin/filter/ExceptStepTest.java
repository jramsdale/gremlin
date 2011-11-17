package com.tinkerpop.gremlin.filter;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.gremlin.pipes.GremlinPipeline;
import com.tinkerpop.gremlin.test.UtilitiesTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ExceptStepTest extends com.tinkerpop.gremlin.test.filter.ExceptStepTest {

    Graph g = TinkerGraphFactory.createTinkerGraph();

    public void testCompliance() {
        UtilitiesTest.testCompliance(this.getClass());
    }

    public void test_g_v1_out_exceptXg_v2X() {
        super.test_g_v1_out_exceptXg_v2X(new GremlinPipeline(g.getVertex(1)).out().except(Arrays.asList(g.getVertex(2))));
    }

    public void test_g_v1_out_aggregateXxX_out_exceptXxX() {
        Set<Vertex> x = new HashSet<Vertex>();
        super.test_g_v1_out_aggregateXxX_out_exceptXxX(new GremlinPipeline(g.getVertex(1)).out().aggregate(x).out().except(x));
    }
}