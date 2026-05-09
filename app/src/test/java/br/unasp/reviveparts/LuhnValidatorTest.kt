package br.unasp.reviveparts

import br.unasp.reviveparts.data.payments.LuhnValidator
import org.junit.Assert.*
import org.junit.Test

class LuhnValidatorTest {
    @Test fun validVisaPasses() = assertTrue(LuhnValidator.isValid("4111111111111111"))
    @Test fun validMastercardPasses() = assertTrue(LuhnValidator.isValid("5500 0000 0000 0004"))
    @Test fun invalidFails() = assertFalse(LuhnValidator.isValid("4111111111111112"))
    @Test fun shortFails() = assertFalse(LuhnValidator.isValid("1234"))
    @Test fun nonDigitsFails() = assertFalse(LuhnValidator.isValid("abcd1111"))
}
