package io.github.smit48ai.fastflow;

import java.util.Map;
import java.util.Set;

public final class FastFlowResult {

  private final Map<String, Object> results;

  public FastFlowResult(Map<String, Object> results) {
    this.results = Map.copyOf(results);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String taskName) {

    if (!results.containsKey(taskName)) {
      throw new IllegalArgumentException("No task found with name: " + taskName);
    }

    return (T) results.get(taskName);
  }

  public boolean contains(String taskName) {
    return results.containsKey(taskName);
  }

  public Set<String> taskNames() {
    return results.keySet();
  }

  public int size() {
    return results.size();
  }
}