package com.shoplane.parceltracking.presentation.backoffice

import com.shoplane.parceltracking.application.access.BackOfficeActor
import com.shoplane.parceltracking.application.access.BackOfficeRole
import com.shoplane.parceltracking.domain.parcel.SellerId
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class MissingBackOfficeIdentityException : RuntimeException("Missing back-office identity headers")

@Component
class BackOfficeActorArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == BackOfficeActor::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): BackOfficeActor {
        val userId = webRequest.getHeader(USER_ID_HEADER)?.takeIf { it.isNotBlank() }
            ?: throw MissingBackOfficeIdentityException()
        return BackOfficeActor(
            userId = userId,
            sellerId = webRequest.getHeader(SELLER_ID_HEADER)?.toLongOrNull()?.let(::SellerId),
            roles = parseRoles(webRequest.getHeader(ROLES_HEADER)),
        )
    }

    private fun parseRoles(header: String?): Set<BackOfficeRole> =
        header.orEmpty()
            .split(',')
            .map { it.trim().uppercase() }
            .mapNotNull { role -> BackOfficeRole.entries.firstOrNull { it.name == role } }
            .toSet()

    companion object {
        const val USER_ID_HEADER = "X-Backoffice-User-Id"
        const val SELLER_ID_HEADER = "X-Backoffice-Seller-Id"
        const val ROLES_HEADER = "X-Backoffice-Roles"
    }
}
