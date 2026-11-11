package com.altamira.hotel;

import org.springframework.http.HttpStatus;

final class HotelError extends RuntimeException {
    final HttpStatus estado;

    HotelError(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    static HotelError noEncontrado(String mensaje) {
        return new HotelError(HttpStatus.NOT_FOUND, mensaje);
    }

    static HotelError conflicto(String mensaje) {
        return new HotelError(HttpStatus.CONFLICT, mensaje);
    }
}
