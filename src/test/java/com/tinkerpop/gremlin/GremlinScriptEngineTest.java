package com.tinkerpop.gremlin;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.gremlin.compiler.Tokens;
import com.tinkerpop.gremlin.compiler.context.GremlinScriptContext;
import com.tinkerpop.gremlin.compiler.types.Atom;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GremlinScriptEngineTest extends BaseTest {

    public void testBasicMathStatements() throws Exception {
        GremlinScriptContext context = new GremlinScriptContext();

        Object result = evaluateGremlinScriptPrimitive("1 + 2", context, true);
        assertEquals(result, 3);

        result = evaluateGremlinScriptPrimitive("1 div (2 * 2)", context, true);
        assertEquals(result.getClass(), Double.class);
        assertEquals(result, 0.25d);

        result = evaluateGremlinScriptPrimitive("(1l - 2) * 2.0d", context, true);
        assertEquals(result, -2.0d);

        result = evaluateGremlinScriptPrimitive("2 + (4l * 2.0d) - 7", context, true);
        assertEquals(result, 3.0d);

        result = evaluateGremlinScriptPrimitive("-2 + 1", context, true);
        assertEquals(result, -1);

        // TODO: bad that substraction requires spacing.
        /*result = evaluateGremlinScriptPrimitive("1-2", context, true);
        assertEquals(result, -1);*/
    }

    public void testBasicTruthStatements() throws Exception {
        GremlinScriptContext context = new GremlinScriptContext();

        Object result = evaluateGremlinScriptPrimitive("true or false", context, true);
        assertTrue((Boolean) result);

        result = evaluateGremlinScriptPrimitive("true and false", context, true);
        assertFalse((Boolean) result);

        result = evaluateGremlinScriptPrimitive("false or false", context, true);
        assertFalse((Boolean) result);

        result = evaluateGremlinScriptPrimitive("false and false", context, true);
        assertFalse((Boolean) result);

        result = evaluateGremlinScriptPrimitive("true or (true and false)", context, true);
        assertTrue((Boolean) result);

        result = evaluateGremlinScriptPrimitive("true and (true or false)", context, true);
        assertTrue((Boolean) result);

        result = evaluateGremlinScriptPrimitive("false or (false and true) or (true and (false and true))", context, true);
        assertFalse((Boolean) result);
    }

    public void testSideEffects() throws Exception {
        GremlinScriptContext context = new GremlinScriptContext();

        evaluateGremlinScriptIterable("g:list(1,2,3)[g:p(g:assign($x,.))]", context, true);
        assertEquals(context.getVariableByName("$x").getValue(), 3);

        evaluateGremlinScriptIterable("g:list(1,2,3)[g:p(g:assign($y,.))][true]", context, true);
        assertEquals(context.getVariableByName("$y").getValue(), 3);

        evaluateGremlinScriptIterable("g:list(1,2,3)[g:p(g:assign($z,.))][false]", context, true);
        assertEquals(context.getVariableByName("$z").getValue(), 3);
    }

    public void testGPathInExpression() throws Exception {
        GremlinScriptContext context = new GremlinScriptContext();
        assertEquals(evaluateGremlinScriptPrimitive("g:list(1,2,3)[1] + 10", context, true), 12);
        assertEquals(evaluateGremlinScriptPrimitive("g:map('marko',2)/@marko + 10", context, true), 12);
    }

    public void testEmbeddedFunctions() throws Exception {
        GremlinScriptContext context = new GremlinScriptContext();
        List<Integer> results = evaluateGremlinScriptIterable("g:list(1,2,3)[g:p(. > 1)]", context, true);
        assertEquals(results.size(), 3);
        assertEquals(results.get(0), new Integer(1));
        assertEquals(results.get(1), new Integer(2));
        assertEquals(results.get(2), new Integer(3));

    }

    public void testAssignmentOperation() throws Exception {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        GremlinScriptContext context = new GremlinScriptContext();

        context.getVariableLibrary().declare(Tokens.GRAPH_VARIABLE, new Atom<Graph>(graph));
        context.getVariableLibrary().declare(Tokens.ROOT_VARIABLE, new Atom<Vertex>(graph.getVertex(1)));

        assertEquals(evaluateGremlinScriptPrimitive("$x := 1", context, true), 1);
        assertEquals(evaluateGremlinScriptPrimitive("$x", context, true), 1);
        assertEquals(evaluateGremlinScriptPrimitive("$x := $x + 1", context, true), 2);
        assertEquals(evaluateGremlinScriptPrimitive("$x", context, true), 2);
        assertEquals(evaluateGremlinScriptPrimitive("./@name := 'name'", context, true), "name");
        assertEquals(evaluateGremlinScriptPrimitive("./@name", context, true), "name");
        assertEquals(evaluateGremlinScriptPrimitive("./outE/inV[0]/@name := 'vname'", context, true), "vname");
        assertEquals(evaluateGremlinScriptPrimitive("./outE/inV[0]/@name", context, true), "vname");
        assertEquals(evaluateGremlinScriptIterable("./outE/inV/@age := 20", context, true), Arrays.asList(20, null, 32));
        assertEquals(evaluateGremlinScriptIterable("./outE/inV/@age := g:list(21, 30, 31)", context, true), Arrays.asList(21, 30, 31));

        evaluateGremlinScriptPrimitive("$m := g:map()", context, false);
        assertEquals(evaluateGremlinScriptPrimitive("$m/@a := 5", context, true), 5);
        assertEquals(evaluateGremlinScriptPrimitive("$m/@a", context, true), 5);
        assertEquals(evaluateGremlinScriptPrimitive("$m/@b := (($m/@a + 6) - 4) div 2.67", context, true), 2.6217227715275007);
        assertEquals(evaluateGremlinScriptPrimitive("$m/@b", context, true), 2.6217227715275007);

        Map m = (Map) evaluateGremlinScriptPrimitive("$m", context, false);
        assertEquals(m.get("a"), 5);
        assertEquals(m.get("b"), 2.6217227715275007);

        evaluateGremlinScriptPrimitive("$m := g:map('marko',0,'pavel',1,'others',g:list('comm',g:map('peter',2,'josh',3)))", context, false);
        m = (Map) evaluateGremlinScriptPrimitive("$m", context, false);
        assertEquals(m.get("marko"), 0);
        assertEquals(evaluateGremlinScriptPrimitive("$m/@marko := 5", context, true), 5);
        assertEquals(m.get("marko"), 5);
        assertEquals(evaluateGremlinScriptPrimitive("$m/@others[0][1]/@peter", context, true), 2);
        assertEquals(((Map) ((List) (m.get("others"))).get(1)).get("peter"), 2);
        assertEquals(evaluateGremlinScriptPrimitive("$m/@others[0][1]/@peter := 10", context, true), 10);
        assertEquals(((Map) ((List) (m.get("others"))).get(1)).get("peter"), 10);
    }

    public void testNumberFunctions() throws Exception {
        GremlinScriptContext context = new GremlinScriptContext();
        context.getFunctionLibrary().loadFunctions("com.tinkerpop.gremlin.compiler.functions.PlayFunctions");
        assertNotNull(context.getFunctionLibrary().getFunction("play", "play-number"));
        assertEquals(evaluateGremlinScriptPrimitive("g:list(1,2,3,4)[play:play-number(2)]", context, true), 3);

        // only one number should be returned
        assertNotNull(evaluateGremlinScriptPrimitive("g:list(1,2,3,4)[g:rand-nat(4)]", context, true));

        List results = evaluateGremlinScriptIterable("g:list(1,2,3,4,5,6)[g:set(0,3,4)]", context, true);
        assertEquals(results.size(), 3);
        assertEquals(results.get(0), 1);
        assertEquals(results.get(1), 4);
        assertEquals(results.get(2), 5);

        results = evaluateGremlinScriptIterable("g:list(1,2,3,4,5,6)[g:set(0,3,4)[0..2]]", context, true);
        assertEquals(results.size(), 2);
        assertEquals(results.get(0), 1);
        assertEquals(results.get(1), 4);
    }

    // GRAPH RELATED TEST CASES

    public void testBasicGraphStatements() throws Exception {
        Graph graph = TinkerGraphFactory.createTinkerGraph();

        GremlinScriptContext context = new GremlinScriptContext();
        context.getVariableLibrary().declare(Tokens.GRAPH_VARIABLE, new Atom<Graph>(graph));
        context.getVariableLibrary().declare(Tokens.ROOT_VARIABLE, new Atom<Vertex>(graph.getVertex(1)));

        List<Vertex> results = evaluateGremlinScriptIterable("./outE/inV", context, true);
        assertEquals(results.size(), 3);
        String name = (String) results.get(0).getProperty("name");
        assertTrue(name.equals("vadas") || name.equals("josh") || name.equals("lop"));
        name = (String) results.get(1).getProperty("name");
        assertTrue(name.equals("vadas") || name.equals("josh") || name.equals("lop"));
        name = (String) results.get(2).getProperty("name");
        assertTrue(name.equals("vadas") || name.equals("josh") || name.equals("lop"));

        results = evaluateGremlinScriptIterable("./outE[@label='created' or @label='knows']/inV", context, true);
        assertEquals(results.size(), 3);
        name = (String) results.get(0).getProperty("name");
        assertTrue(name.equals("vadas") || name.equals("josh") || name.equals("lop"));
        name = (String) results.get(1).getProperty("name");
        assertTrue(name.equals("vadas") || name.equals("josh") || name.equals("lop"));
        name = (String) results.get(2).getProperty("name");
        assertTrue(name.equals("vadas") || name.equals("josh") || name.equals("lop"));

        Vertex result = (Vertex) evaluateGremlinScriptPrimitive("./outE[@label='created']/inV", context, true);
        name = (String) result.getProperty("name");
        assertTrue(name.equals("lop"));

        results = evaluateGremlinScriptIterable("./outE[@label='knows']/inV", context, true);
        assertEquals(results.size(), 2);
        name = (String) results.get(0).getProperty("name");
        assertTrue(name.equals("josh") || name.equals("vadas"));
        name = (String) results.get(1).getProperty("name");
        assertTrue(name.equals("josh") || name.equals("vadas"));

        results = evaluateGremlinScriptIterable("./outE[./@weight >= 0.5]/inV/././.", context, true);
        assertEquals(results.size(), 2);
        name = (String) results.get(0).getProperty("name");
        assertTrue(name.equals("vadas") || name.equals("josh"));
        name = (String) results.get(1).getProperty("name");
        assertTrue(name.equals("vadas") || name.equals("josh"));

        results = evaluateGremlinScriptIterable("./outE[./@weight >= $_/outE/@weight[0]]/inV", context, true);
        assertEquals(results.size(), 2);
        name = (String) results.get(0).getProperty("name");
        assertTrue(name.equals("vadas") || name.equals("josh"));
        name = (String) results.get(1).getProperty("name");
        assertTrue(name.equals("vadas") || name.equals("josh"));

        results = evaluateGremlinScriptIterable("./inE", context, true);
        assertNull(results);

        results = evaluateGremlinScriptIterable("./outE/inV[@blah != null]", context, true);
        assertNull(results);

        results = evaluateGremlinScriptIterable("./outE/inV[@blah = null]", context, true);
        assertEquals(results.size(), 3);
    }

    public void testHistoryOnGraph() throws Exception {
        Graph graph = TinkerGraphFactory.createTinkerGraph();

        GremlinScriptContext context = new GremlinScriptContext();
        context.getVariableLibrary().declare(Tokens.GRAPH_VARIABLE, new Atom<Graph>(graph));
        context.getVariableLibrary().declare(Tokens.ROOT_VARIABLE, new Atom<Vertex>(graph.getVertex(1)));

        Vertex result = (Vertex) evaluateGremlinScriptPrimitive("./outE/inV/../..", context, true);
        String name = (String) result.getProperty("name");
        assertEquals(name, "marko");

        List<Vertex> results = evaluateGremlinScriptIterable("./outE/inV/../../..", context, true);
        assertNull(results);

        result = (Vertex) evaluateGremlinScriptPrimitive("./outE/inV/outE/inV/../..", context, true);
        name = (String) result.getProperty("name");
        assertEquals(name, "josh");

        result = (Vertex) evaluateGremlinScriptPrimitive("./outE/inV/outE/inV[@name='lop']/../..", context, true);
        name = (String) result.getProperty("name");
        assertEquals(name, "josh");

        result = (Vertex) evaluateGremlinScriptPrimitive("./outE/inV/outE/inV[@name='ripple']/../..", context, true);
        name = (String) result.getProperty("name");
        assertEquals(name, "josh");

        results = evaluateGremlinScriptIterable("./outE/inV/outE/inV[@name='ripple' or @name='lop' or @name='blah']/../../outE/inV", context, true);
        assertEquals(results.size(), 2);
        name = (String) results.get(0).getProperty("name");
        assertTrue(name.equals("ripple") || name.equals("lop"));
        name = (String) results.get(1).getProperty("name");
        assertTrue(name.equals("ripple") || name.equals("lop"));
    }

    public void testIdAndLabelProperties() throws Exception {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        GremlinScriptContext context = new GremlinScriptContext();
        context.getVariableLibrary().declare(Tokens.GRAPH_VARIABLE, new Atom<Graph>(graph));
        context.getVariableLibrary().declare(Tokens.ROOT_VARIABLE, new Atom<Vertex>(graph.getVertex(1)));

        String result = (String) evaluateGremlinScriptPrimitive("./@id", context, true);
        assertEquals(result, "1");

        List<Vertex> results = evaluateGremlinScriptIterable("./outE/inV/@id", context, true);
        assertEquals(results.size(), 3);
        assertTrue(results.contains("2"));
        assertTrue(results.contains("3"));
        assertTrue(results.contains("4"));

        results = evaluateGremlinScriptIterable("./outE/@label", context, true);
        assertEquals(results.size(), 3);
        assertTrue(results.contains("created"));
        assertTrue(results.contains("knows"));
    }

    public void testVertexEdgeGraphProperties() throws Exception {
        Graph graph = TinkerGraphFactory.createTinkerGraph();

        GremlinScriptContext context = new GremlinScriptContext();
        context.getVariableLibrary().declare(Tokens.GRAPH_VARIABLE, new Atom<Graph>(graph));
        context.getVariableLibrary().declare(Tokens.ROOT_VARIABLE, new Atom<Vertex>(graph.getVertex(1)));

        List<Element> results = evaluateGremlinScriptIterable("$_g/V", context, true);
        assertEquals(results.size(), 6);
        assertTrue(results.contains(graph.getVertex("1")));
        assertTrue(results.contains(graph.getVertex("2")));
        assertTrue(results.contains(graph.getVertex("3")));
        assertTrue(results.contains(graph.getVertex("4")));
        assertTrue(results.contains(graph.getVertex("5")));
        assertTrue(results.contains(graph.getVertex("6")));

        results = evaluateGremlinScriptIterable("$_g/V/@id", context, true);
        assertEquals(results.size(), 6);
        assertTrue(results.contains("1"));
        assertTrue(results.contains("2"));
        assertTrue(results.contains("3"));
        assertTrue(results.contains("4"));
        assertTrue(results.contains("5"));
        assertTrue(results.contains("6"));

        results = evaluateGremlinScriptIterable("$_g/E", context, true);
        assertEquals(results.size(), 6);
        assertTrue(results.contains(graph.getEdge("7")));
        assertTrue(results.contains(graph.getEdge("8")));
        assertTrue(results.contains(graph.getEdge("9")));
        assertTrue(results.contains(graph.getEdge("10")));
        assertTrue(results.contains(graph.getEdge("11")));
        assertTrue(results.contains(graph.getEdge("12")));


        results = evaluateGremlinScriptIterable("$_g/E/@id", context, true);
        assertEquals(results.size(), 6);
        assertTrue(results.contains("7"));
        assertTrue(results.contains("8"));
        assertTrue(results.contains("9"));
        assertTrue(results.contains("10"));
        assertTrue(results.contains("11"));
        assertTrue(results.contains("12"));

    }

    public void testGPathInExpressionGraph() throws Exception {
        Graph graph = TinkerGraphFactory.createTinkerGraph();

        GremlinScriptContext context = new GremlinScriptContext();
        context.getVariableLibrary().declare(Tokens.GRAPH_VARIABLE, new Atom<Graph>(graph));
        context.getVariableLibrary().declare(Tokens.ROOT_VARIABLE, new Atom<Vertex>(graph.getVertex(1)));
        assertEquals(count(evaluateGremlinScriptIterable("./outE[./@label = 'knows']", context, true)), 2);
        assertEquals(evaluateGremlinScriptPrimitive("./outE[./inV/@name = 'vadas']/inV/@name", context, true), "vadas");
        assertEquals(evaluateGremlinScriptPrimitive("./outE[./inV[@name = 'vadas']/@name = 'vadas']/inV/@name", context, true), "vadas");
        assertEquals(evaluateGremlinScriptPrimitive(".[g:count(./outE/inV/@name) = 3l]/@name", context, true), "marko");
        assertEquals(evaluateGremlinScriptPrimitive(".[./outE/inV/@age >= 27]/@name", context, true), "marko");
    }

}
