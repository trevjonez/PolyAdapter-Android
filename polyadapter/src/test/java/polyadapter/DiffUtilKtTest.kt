package polyadapter

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DiffUtilKtTest {
  @Test
  fun `data class is not equal identity with different instances`() {
    val old = TestInput("foo", true)
    val new = old.copy()

    val defaultDiffCallback = equalityItemCallback<TestInput>()

    assertThat(defaultDiffCallback.areItemsTheSame(old, new)).isFalse()
  }

  @Test
  fun `data class is equal identity with different instances with custom identifier impl`() {
    val old = TestInput("foo", true)
    val new = old.copy()

    val defaultDiffCallback = equalityItemCallback<TestInput> { foo.hashCode() }

    assertThat(defaultDiffCallback.areItemsTheSame(old, new)).isTrue()
  }

  @Test
  fun `data class is equal content with different instances`() {
    val old = TestInput("foo", true)
    val new = old.copy()

    val defaultDiffCallback = equalityItemCallback<TestInput>()

    assertThat(defaultDiffCallback.areContentsTheSame(old, new)).isTrue()
  }
}

data class TestInput(val foo: String, val bar: Boolean)
