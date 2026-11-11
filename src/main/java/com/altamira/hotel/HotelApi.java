package com.altamira.hotel;

import com.altamira.hotel.HotelDatos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
class HotelApi {
    private final Recepcion recepcion;

    HotelApi(Recepcion recepcion) {
        this.recepcion = recepcion;
    }

    @PostMapping("/clientes")
    @ResponseStatus(HttpStatus.CREATED)
    Cliente crearCliente(@Valid @RequestBody AltaCliente datos) {
        return recepcion.registrarCliente(datos);
    }

    @GetMapping("/clientes")
    List<Cliente> clientes() {
        return recepcion.clientes();
    }

    @GetMapping("/clientes/{id}")
    Cliente cliente(@PathVariable long id) {
        return recepcion.cliente(id);
    }

    @GetMapping("/clientes/documento/{identificacion}")
    Cliente clientePorDocumento(@PathVariable String identificacion) {
        return recepcion.clientePorDocumento(identificacion);
    }

    @GetMapping("/habitaciones")
    List<Habitacion> habitaciones() {
        return recepcion.habitaciones();
    }

    @GetMapping("/habitaciones/{numero}")
    Habitacion habitacion(@PathVariable int numero) {
        return recepcion.habitacion(numero);
    }

    @GetMapping("/habitaciones/disponibles/{tipo}")
    List<Habitacion> disponibles(@PathVariable TipoHabitacion tipo) {
        return recepcion.disponibles(tipo);
    }

    @PostMapping("/checkin")
    @ResponseStatus(HttpStatus.CREATED)
    RegistroEntrada checkIn(@Valid @RequestBody Entrada datos) {
        return recepcion.checkIn(datos);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    Factura checkOut(@Valid @RequestBody Salida datos) {
        return recepcion.checkOut(datos);
    }

    @GetMapping("/facturas")
    List<Factura> facturas() {
        return recepcion.facturas();
    }

    @GetMapping("/facturas/{id}")
    Factura factura(@PathVariable long id) {
        return recepcion.factura(id);
    }
}
