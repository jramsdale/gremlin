package com.tinkerpop.gremlin.java.transform;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.gremlin.test.ComplianceTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MapStepTest extends com.tinkerpop.gremlin.test.transform.MapStepTest {

    Graph g = TinkerGraphFactory.createTinkerGraph();

    public void testCompliance() {
        ComplianceTest.testCompliance(this.getClass());
    }

    public void test_g_v1_map() {
        super.test_g_v1_map(new GremlinPipeline(g.getVertex(1)).map());
    }

    public void test_g_v1_outXknowsX_map() {
        super.test_g_v1_outXknowsX_map(new GremlinPipeline(g.getVertex(1)).out("knows").map());
    }
}
