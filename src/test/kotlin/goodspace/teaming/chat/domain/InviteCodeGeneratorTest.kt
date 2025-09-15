package goodspace.teaming.chat.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

private const val DEFAULT_CODE_LENGTH = 100

class InviteCodeGeneratorTest {
    @ParameterizedTest
    @ValueSource(ints = [1, 10, 100, 1000])
    fun `지정한 길이의 코드를 생성한다`(codeLength: Int) {
        // given
        val inviteCodeGenerator = InviteCodeGenerator(codeLength = codeLength)

        // when
        val code = inviteCodeGenerator.generate()

        // then
        assertThat(code.length).isEqualTo(codeLength)
    }

    @ParameterizedTest
    @ValueSource(strings = ["a", "abc", "abcde", "abcdefg"])
    fun `지정한 문자들만 포함한 코드를 생성한다`(allowedCharacters: String) {
        // given
        val inviteCodeGenerator = InviteCodeGenerator(
            codeLength = DEFAULT_CODE_LENGTH,
            allowedCharacters = allowedCharacters
        )

        // when
        val code = inviteCodeGenerator.generate()

        // then
        val allowedCharacterSet = allowedCharacters.toSet()
        assertThat(code.all { it in allowedCharacterSet }).isTrue()
    }

    @ParameterizedTest
    @ValueSource(ints = [-100, -10, 0])
    fun `문자 길이가 0 이하면 예외가 발생한다`(illegalCodeLength: Int) {
        assertThatThrownBy { InviteCodeGenerator(codeLength = illegalCodeLength) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `허용 문자가 비어 있다면 예외가 발생한다`() {
        assertThatThrownBy { InviteCodeGenerator(allowedCharacters = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
