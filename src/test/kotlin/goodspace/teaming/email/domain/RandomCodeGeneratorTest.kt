package goodspace.teaming.email.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class RandomCodeGeneratorTest {
    private val randomCodeGenerator = RandomCodeGenerator()

    @Nested
    @DisplayName("generate")
    inner class Generate {
        @ParameterizedTest
        @ValueSource(ints = [1, 5, 10, 50, 100])
        fun `지정한 길이에 맞는 코드를 생성해 반환한다`(length: Int) {
            val code = randomCodeGenerator.generate(length)

            assertThat(code.length).isEqualTo(length)
        }
    }
}
