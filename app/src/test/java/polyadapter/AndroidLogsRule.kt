package polyadapter

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.shadows.ShadowLog
import java.io.PrintStream

class AndroidLogsRule(val outputStream: PrintStream = System.out) : TestRule {
  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        try {
          ShadowLog.stream = outputStream
          base.evaluate()
        } finally {
          ShadowLog.stream = null
        }
      }
    }
  }
}