package de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.SubSuperSetGraph}
 *
 * @author Jens Hildebrandt
 * @author Jakob Zwiener
 */
public class SubSuperSetGraphTest {

    /**
     * Test method for {@link de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.SubSuperSetGraph#add(ColumnCombinationBitset)}
     *
     * After inserting a column combination a subgraph for every set bit should exist.
     * Add should return the graph after addition.
     */
    @Test
    public void testAdd() {
        // Setup
        SubSuperSetGraph graph = new SubSuperSetGraph();
        ColumnCombinationBitset columnCombination = new ColumnCombinationBitset(2, 4, 7);

        // Execute functionality
        SubSuperSetGraph graphAfterAdd = graph.add(columnCombination);

        // Check result
        // Check existence of column indices in subgraphs by iterating
        SubSuperSetGraph actualSubGraph = graph;
        for (int setColumnIndex : columnCombination.getSetBits()) {
            assertTrue(actualSubGraph.subGraphs.containsKey(setColumnIndex));
            actualSubGraph = actualSubGraph.subGraphs.get(setColumnIndex);
        }
        // Check add return value
        assertSame(graph, graphAfterAdd);
    }

    /**
     * TODO docs
     */
    @Test
    public void testAddSubset() {
        // TODO implement
    }

    /**
     * Test method for {@link de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.SubSuperSetGraph#addAll(java.util.Collection)}
     * <p/>
     * After inserting all column combinations the graph should be equal to the expected graph from the fixture.
     * AddAll should return the graph after addition.
     */
    @Test
    public void testAddAll() {
        // Setup
        SubSuperSetGraphFixture fixture = new SubSuperSetGraphFixture();
        SubSuperSetGraph graph = new SubSuperSetGraph();
        // Expected values
        Collection<ColumnCombinationBitset> expectedColumnCombinations = fixture.getExpectedIncludedColumnCombinations();
        SubSuperSetGraph expectedGraph = fixture.getGraph();

        // Execute functionality
        SubSuperSetGraph graphAfterAddAll = graph.addAll(expectedColumnCombinations);

        // Check result
        assertEquals(expectedGraph, graph);
        assertSame(graph, graphAfterAddAll);
    }

    /**
     * Test method for {@link de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.SubSuperSetGraph#getExistingSubsets(ColumnCombinationBitset)}
     */
    @Test
    public void testGetExistingSubsets() {
        // Setup
        SubSuperSetGraphFixture fixture = new SubSuperSetGraphFixture();
        SubSuperSetGraph graph = fixture.getGraph();
        ColumnCombinationBitset columnCombinationToQuery = fixture.getColumnCombinationForSubsetQuery();

        // Execute functionality
        List<ColumnCombinationBitset> actualSubsets = graph.getExistingSubsets(columnCombinationToQuery);

        // Check result
        assertThat(actualSubsets,
                IsIterableContainingInAnyOrder.containsInAnyOrder(fixture.getExpectedSubsetsFromQuery()));
    }

    /**
     * Test method for {@link SubSuperSetGraph#isEmpty()}
     */
    @Test
    public void testIsEmpty() {
        // Setup
        SubSuperSetGraph emptyGraph = new SubSuperSetGraph();
        SubSuperSetGraph nonEmptyGraph = new SubSuperSetGraph();
        nonEmptyGraph.add(new ColumnCombinationBitset(10));

        // Execute functionality
        // Check result
        assertTrue(emptyGraph.isEmpty());
        assertFalse(nonEmptyGraph.isEmpty());
    }

    /**
     * Test method  {@link SubSuperSetGraph#equals(Object)} and
     * {@link de.uni_potsdam.hpi.metanome.algorithm_helper.data_structures.SubSuperSetGraph#hashCode()}
     */
    @Test
    public void testEqualsAndHashCode() {
        // Setup
        SubSuperSetGraph actualGraph = new SubSuperSetGraph();
        SubSuperSetGraph equalsGraph = new SubSuperSetGraph();
        SubSuperSetGraph notEqualsGraph = new SubSuperSetGraph();

        actualGraph.add(new ColumnCombinationBitset(2, 5, 10, 20));
        actualGraph.add((new ColumnCombinationBitset(2, 5, 8, 15)));

        equalsGraph.add((new ColumnCombinationBitset(2, 5, 8, 15)));
        equalsGraph.add(new ColumnCombinationBitset(2, 5, 10, 20));

        notEqualsGraph.add(new ColumnCombinationBitset(2, 5, 12, 20));
        notEqualsGraph.add((new ColumnCombinationBitset(2, 5, 10, 15)));

        // Execute functionality
        // Check result
        assertNotSame(actualGraph, equalsGraph);
        assertEquals(actualGraph.hashCode(), equalsGraph.hashCode());
        assertEquals(actualGraph, equalsGraph);
        assertNotEquals(actualGraph.hashCode(), notEqualsGraph.hashCode());
        assertNotEquals(actualGraph, notEqualsGraph);
    }
}
