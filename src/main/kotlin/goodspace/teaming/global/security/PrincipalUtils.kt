package goodspace.teaming.global.security

import java.security.Principal

fun Principal.getUserId(): Long =
    this.name.toLong()
