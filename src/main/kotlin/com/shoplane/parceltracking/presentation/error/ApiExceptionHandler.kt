package com.shoplane.parceltracking.presentation.error

import com.shoplane.parceltracking.application.access.AccessDeniedException
import com.shoplane.parceltracking.domain.carrier.RetiredCarrierException
import com.shoplane.parceltracking.domain.carrier.UnknownCarrierException
import com.shoplane.parceltracking.domain.parcel.DuplicateTrackingNumberException
import com.shoplane.parceltracking.domain.parcel.IllegalStatusTransitionException
import com.shoplane.parceltracking.domain.parcel.ParcelNotFoundException
import com.shoplane.parceltracking.presentation.backoffice.MissingBackOfficeIdentityException
import com.shoplane.parceltracking.presentation.webhook.InvalidWebhookSignatureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import tools.jackson.core.JacksonException

data class ApiError(val code: String, val message: String)

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ParcelNotFoundException::class)
    fun notFound(exception: ParcelNotFoundException) = error(HttpStatus.NOT_FOUND, "parcel_not_found", exception)

    @ExceptionHandler(AccessDeniedException::class)
    fun forbidden(exception: AccessDeniedException) = error(HttpStatus.FORBIDDEN, "access_denied", exception)

    @ExceptionHandler(MissingBackOfficeIdentityException::class, InvalidWebhookSignatureException::class)
    fun unauthorized(exception: RuntimeException) = error(HttpStatus.UNAUTHORIZED, "unauthenticated", exception)

    @ExceptionHandler(IllegalStatusTransitionException::class)
    fun illegalTransition(exception: IllegalStatusTransitionException) =
        error(HttpStatus.CONFLICT, "illegal_status_transition", exception)

    @ExceptionHandler(DuplicateTrackingNumberException::class)
    fun duplicate(exception: DuplicateTrackingNumberException) =
        error(HttpStatus.CONFLICT, "duplicate_tracking_number", exception)

    @ExceptionHandler(UnknownCarrierException::class, RetiredCarrierException::class)
    fun unprocessableCarrier(exception: RuntimeException) =
        error(HttpStatus.UNPROCESSABLE_CONTENT, "carrier_not_accepted", exception)

    @ExceptionHandler(
        IllegalArgumentException::class,
        MethodArgumentTypeMismatchException::class,
        MethodArgumentNotValidException::class,
        JacksonException::class,
    )
    fun badRequest(exception: Exception) = error(HttpStatus.BAD_REQUEST, "invalid_request", exception)

    private fun error(status: HttpStatus, code: String, exception: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(status).body(ApiError(code, exception.message ?: status.reasonPhrase))
}
