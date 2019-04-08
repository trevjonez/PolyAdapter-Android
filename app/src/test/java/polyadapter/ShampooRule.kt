package polyadapter

import androidx.annotation.IntRange
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ShampooRule(
    @IntRange(from = 1) val iterations: Int,
    val printIterationLabels: Boolean = true
) : TestRule {
  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        (0 until iterations).forEach {
          if (printIterationLabels) println("\n\n Iteration: $it")
          try {
            base.evaluate()
          } catch (error: Throwable) {
            throw AssertionError("Failed at iteration: $it", error)
          }
        }
      }
    }
  }
}