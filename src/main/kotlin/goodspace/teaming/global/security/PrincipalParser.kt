package goodspace.teaming.global.security

import java.security.Principal

fun getUserIdFromPrincipal(principal: Principal): Long =
    principal.name.toLong()
