/**
 *
 */
package edu.washington.escience.myria.operator.agg;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import edu.washington.escience.myria.DbException;
import edu.washington.escience.myria.Schema;
import edu.washington.escience.myria.expression.evaluate.GenericEvaluator;
import edu.washington.escience.myria.expression.evaluate.PythonUDFEvaluator;
import edu.washington.escience.myria.expression.evaluate.ScriptEvalInterface;
import edu.washington.escience.myria.storage.AppendableTable;
import edu.washington.escience.myria.storage.ReadableTable;
import edu.washington.escience.myria.storage.Tuple;

/**
 * 
 */
public class StatefulUserDefinedAggregator extends UserDefinedAggregator {

  /** Required for Java serialization. */
  private static final long serialVersionUID = 1L;
  /** logger for this class. */
  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StatefulUserDefinedAggregator.class);

  private final List<ReadableTable> ltb = new ArrayList<ReadableTable>();

  /**
   * @param state the initialized state of the tuple
   * @param updateEvaluator updates the state given an input row
   * @param emitEvaluators the evaluators that finalize the state
   * @param resultSchema the schema of the tuples produced by this aggregator
   */

  public StatefulUserDefinedAggregator(final Tuple state, final ScriptEvalInterface updateEvaluator,
      final List<PythonUDFEvaluator> pyUDFEvaluators, final List<GenericEvaluator> emitEvaluators,
      final Schema resultSchema) {
    super(state, updateEvaluator, pyUDFEvaluators, emitEvaluators, resultSchema);
  }

  @Override
  public void add(final ReadableTable from, final Object state) throws DbException {
    ltb.add(from);
  }

  @Override
  public void addRow(final ReadableTable from, final int row, final Object state) throws DbException {
    throw new DbException("Add Row not supported for StatefulUDA");
  }

  @Override
  public void getResult(final AppendableTable dest, final int destColumn, final Object state) throws DbException,
      IOException {

    Tuple stateTuple = (Tuple) state;

    // compute results over the tuplebatch list
    if (pyUDFEvaluators.size() > 0) {
      for (int i = 0; i < pyUDFEvaluators.size(); i++) {
        // LOGGER.info("trying to update state variable");
        pyUDFEvaluators.get(i).evalBatch(ltb, stateTuple, stateTuple);
      }
    }
    // emit results

    for (int index = 0; index < emitEvaluators.size(); index++) {
      final GenericEvaluator evaluator = emitEvaluators.get(index);
      try {
        evaluator.eval(null, 0, dest.asWritableColumn(destColumn + index), stateTuple);
      } catch (InvocationTargetException e) {
        throw new DbException("Error finalizing aggregate", e);
      }
    }

  }

  @Override
  public Schema getResultSchema() {
    return resultSchema;
  }

  @Override
  public Object getInitialState() {

    return initialState.clone();
  }
}