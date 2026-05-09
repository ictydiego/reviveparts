package br.unasp.reviveparts.data.payments

object LuhnValidator {
    fun isValid(input: String): Boolean {
        val digits = input.filter { it.isDigit() }
        if (digits.length < 13 || input.any { !it.isDigit() && !it.isWhitespace() }) return false
        var sum = 0
        var alt = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i].digitToInt()
            if (alt) { n *= 2; if (n > 9) n -= 9 }
            sum += n
            alt = !alt
        }
        return sum % 10 == 0
    }
}
