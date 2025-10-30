package Servicios;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import entidades.TemperaturaRegistro;

public class TemperaturaServicio {
    private List<TemperaturaRegistro> registros = new ArrayList<>();
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

public void cargarDesdeCSV(String rutaArchivo) {
    try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
        registros = br.lines()
                .filter(linea -> !linea.startsWith("Ciudad")) // Saltar encabezado
                .map(linea -> linea.split(","))
                .map(partes -> new TemperaturaRegistro(
                        partes[0],
                        LocalDate.parse(partes[1], FORMATO_FECHA),
                        Double.parseDouble(partes[2])
                ))
                .collect(Collectors.toList());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public Map<String, Double> calcularPromedioPorCiudad(LocalDate inicio, LocalDate fin) {
        return registros.stream()
                .filter(r -> !r.getFecha().isBefore(inicio) && !r.getFecha().isAfter(fin))
                .collect(Collectors.groupingBy(
                        TemperaturaRegistro::getCiudad,
                        Collectors.averagingDouble(TemperaturaRegistro::getTemperatura)
                ));
    }

    public Optional<TemperaturaRegistro> obtenerCiudadMasCalurosa(LocalDate fecha) {
        return registros.stream()
                .filter(r -> r.getFecha().equals(fecha))
                .max(Comparator.comparingDouble(TemperaturaRegistro::getTemperatura));
    }

    public Optional<TemperaturaRegistro> obtenerCiudadMasFria(LocalDate fecha) {
        return registros.stream()
                .filter(r -> r.getFecha().equals(fecha))
                .min(Comparator.comparingDouble(TemperaturaRegistro::getTemperatura));
    }

    public List<TemperaturaRegistro> getRegistros() {
        return registros;
    }

    public static Map<String, Double> promedioPorCiudad(List<TemperaturaRegistro> datos, LocalDate desde, LocalDate hasta) {
        return datos.stream()
            .filter(r -> !r.getFecha().isBefore(desde) && !r.getFecha().isAfter(hasta))
            .collect(Collectors.groupingBy(
                TemperaturaRegistro::getCiudad,
                Collectors.averagingDouble(TemperaturaRegistro::getTemperatura)
            ));
    }
   
}