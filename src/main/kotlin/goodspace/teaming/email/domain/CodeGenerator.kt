package goodspace.teaming.email.domain

interface CodeGenerator {
    fun generate(length: Int): String
}
