package edu.washington.escience.myria.api.encoding;

import java.util.List;
import java.util.Set;

import edu.washington.escience.myria.parallel.Consumer;
import edu.washington.escience.myria.parallel.ExchangePairID;

public abstract class AbstractConsumerEncoding<C extends Consumer> extends LeafOperatorEncoding<C> implements
    ExchangeEncoding {
  @Required
  public String argOperatorId;

  String getOperatorId() {
    return argOperatorId;
  }

  private Set<Integer> realWorkerIds;
  private List<ExchangePairID> realOperatorIds;

  @Override
  public final Set<Integer> getRealWorkerIds() {
    return realWorkerIds;
  }

  @Override
  public final void setRealWorkerIds(Set<Integer> w) {
    realWorkerIds = w;
  }

  @Override
  public final List<ExchangePairID> getRealOperatorIds() {
    return realOperatorIds;
  }

  @Override
  public final void setRealOperatorIds(List<ExchangePairID> operatorIds) {
    realOperatorIds = operatorIds;
  }

}