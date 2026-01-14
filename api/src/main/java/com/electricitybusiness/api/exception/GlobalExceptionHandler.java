package com.electricitybusiness.api.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.electricitybusiness.api.dto.error.ErrorResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // EXCEPTIONS SÉCURITÉ (401 Unauthorized)

    /**
     * Gère les erreurs d'identifiants incorrects (email ou mot de passe invalide).
     * Message volontairement générique pour éviter l'énumération de comptes.
     * @param ex      L'exception BadCredentialsException levée lors de l'échec d'authentification.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec un statut 401 et un message d'erreur générique.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Failed")
                .message("Invalid credentials")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Gère les erreurs liées aux tokens JWT (expiration, signature invalide, format incorrect).
     * Regroupe toutes les exceptions JWT sous un message générique.
     * @param ex      L'exception liée au JWT levée lors de la validation du token.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec un statut 401 et un message d'erreur générique.
     */
    @ExceptionHandler({
            ExpiredJwtException.class,
            MalformedJwtException.class,
            SignatureException.class,
            UnsupportedJwtException.class,
            JwtException.class
    })
    public ResponseEntity<ErrorResponse> handleJwtException(
            Exception ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Invalid Token")
                .message("Invalid or expired token")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Gère les cas où l'utilisateur n'est pas trouvé en base de données.
     * Message générique pour protéger contre l'énumération de comptes.
     * @param ex      L'exception UsernameNotFoundException levée lors de la recherche de l'utilisateur.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec un statut 401 et un message d'erreur générique.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(
            UsernameNotFoundException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Failed")
                .message("Invalid credentials")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // EXCEPTION ACCÈS REFUSÉ (403 Forbidden)

    /**
     * Gère les exceptions d'accès refusé.
     * Fournit un message clair indiquant que l'utilisateur n'a pas la permission d'accéder à la ressource.
     * @param ex      L'exception AccessDeniedException levée lors de l'accès refusé.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec un statut 403 et un message d'erreur.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Access Denied")
                .message("You don't have permission to access this resource")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // EXCEPTIONS VALIDATION (400 Bad Request)

    /**
     * Gère les exceptions de validation métier des réservations.
     * Fournit un message clair avec le champ en erreur.
     * @param ex      L'exception InvalidBookingException levée lors de la validation.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec un statut 400 et les détails de l'erreur.
     */
    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBookingException(
            InvalidBookingException ex,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getField(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Booking")
                .message("La réservation contient des données invalides.")
                .errors(errors)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les erreurs de validation des arguments de méthode.
     * Collecte les erreurs de champ et les inclut dans la réponse.
     * @param ex      L'exception MethodArgumentNotValidException levée lors de la validation.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec un statut 400 et les détails des erreurs de validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation failed")
                .message("Some fields are invalid.")
                .errors(errors)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les erreurs de conversion de type pour les arguments de méthode.
     * Fournit des informations sur la valeur invalide et le type attendu.
     * @param ex      L'exception MethodArgumentTypeMismatchException levée lors de la conversion.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec un statut 400 et les détails de l'erreur de conversion.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown type";
        String message = String.format("Failed to convert value '%s' to required type '%s'", ex.getValue(), requiredType);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // EXCEPTION RESSOURCE NON TROUVÉE (404 Not Found)

    /**
     * Gère les exceptions de ressource non trouvée.
     * Fournit un message clair indiquant que la ressource demandée est introuvable.
     * @param ex      L'exception ResourceNotFoundException levée lorsque la ressource est absente.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec un statut 404 et un message d'erreur.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // EXCEPTION CONFLIT (409 Conflict)

    /**
     * Gère les exceptions de conflit de ressource.
     * Fournit un message clair indiquant qu'il y a un conflit avec l'état actuel de la ressource.
     * @param ex      L'exception ConflictException levée lors d'un conflit de ressource.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec un statut 409 et un message d'erreur.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // EXCEPTIONS GÉNÉRIQUES (500 Internal Server Error)

    /**
     * Gère les exceptions de type ResponseStatusException.
     * Permet de capturer les exceptions personnalisées avec des statuts HTTP spécifiques.
     * @param ex      L'exception ResponseStatusException levée.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec le statut et le message définis dans l'exception.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        HttpStatus httpStatus = (HttpStatus) ex.getStatusCode();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(ex.getReason())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    /**
     * Gère les exceptions d'exécution génériques.
     * Utilisé pour capturer les erreurs inattendues lors du traitement des tokens.
     * @param ex      L'exception RuntimeException levée.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec un statut 401 et un message d'erreur générique.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Token Processing Error")
                .message("Error processing token")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Gère les exceptions génériques non capturées.
     * Fournit un message d'erreur interne du serveur.
     * @param ex      L'exception levée.
     * @param request Le contexte de la requête web.
     * @return Une réponse HTTP avec un statut 500 et un message d'erreur.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
