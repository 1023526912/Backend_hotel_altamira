package com.altamira.hotel;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
class ErroresApi {
    record Respuesta(int codigoHttp, String error, String mensaje, LocalDateTime fechaHora) {}

    @ExceptionHandler(HotelError.class)
    ResponseEntity<Respuesta> negocio(HotelError error) {
        return respuesta(error.estado, error.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Respuesta> validacion(MethodArgumentNotValidException error) {
        String detalle = error.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return respuesta(HttpStatus.BAD_REQUEST, detalle);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<Respuesta> formatoInvalido() {
        return respuesta(HttpStatus.BAD_REQUEST, "Revisa los tipos de datos y los valores enviados");
    }

    private ResponseEntity<Respuesta> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado)
                .body(new Respuesta(estado.value(), estado.getReasonPhrase(), mensaje, LocalDateTime.now()));
    }
}
