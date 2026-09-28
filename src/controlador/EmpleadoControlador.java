package controlador;

import modelo.EmpleadoAdministrativo;
import modelo.EmpleadoBase;
import modelo.EmpleadoComercial;
import modelo.RepositorioEmpleado;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Controlador principal: realiza validaciones, maneja las reglas de negocio y las operaciones CRUD.
 */
public class EmpleadoControlador {

    public static final String[] TIPOS_EMPLEADO = {"Operativo", "Administrativo", "Comercial"};

    private final RepositorioEmpleado repositorio;
    private final ArrayList<String> historial;

    public EmpleadoControlador() {
        repositorio = new RepositorioEmpleado();
        historial = new ArrayList<>();
        cargarDatosDePrueba();
    }

    private void cargarDatosDePrueba() {
        String[] cedulas = {"1001", "1002", "1003", "1004", "1005"};
        String[] nombres = {"Ana Torres", "Luis Gómez", "Marta Ríos", "Pedro Cano", "Carlos Ruiz"};
        double[] salarios = {1800000, 2500000, 1750000, 3200000, 2000000};

        for (int i = 0; i < cedulas.length; i++) {
            EmpleadoBase empleado;
            if (i % 3 == 0) {
                empleado = new EmpleadoBase(cedulas[i], nombres[i], salarios[i]);
            } else if (i % 3 == 1) {
                empleado = new EmpleadoAdministrativo(cedulas[i], nombres[i], salarios[i], 300000);
            } else {
                empleado = new EmpleadoComercial(cedulas[i], nombres[i], salarios[i], 15);
            }
            repositorio.agregar(empleado);
        }
    }

    private boolean esNumeroValido(String texto) {
        if (texto.isEmpty() || texto.equals(".")) {
            return false;
        }
        int puntos = 0;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '.') {
                puntos++;
            } else if (!Character.isDigit(c)) {
                return false;
            }
        }
        return puntos <= 1;
    }

    private String validar(String cedula, String nombre, String salario, String tipo, String bonificacion) {
        if (cedula.isEmpty() || nombre.isEmpty()) {
            return "La cédula y el nombre son obligatorios.";
        }
        if (!esNumeroValido(salario)) {
            return "El salario debe ser un número positivo (sin puntos de miles).";
        }
        if (tipo.equals("Administrativo")) {
            if (!esNumeroValido(bonificacion)) {
                return "La bonificación debe ser un número positivo.";
            }
        } else if (tipo.equals("Comercial")) {
            if (!esNumeroValido(bonificacion)) {
                return "El porcentaje de comisión debe ser un número positivo.";
            }
            double comision = Double.parseDouble(bonificacion);
            if (comision > 50) {
                return "El porcentaje de comisión no puede ser mayor al 50%.";
            }
        }
        return null;
    }

    private EmpleadoBase construirEmpleado(String cedula, String nombre, String salario, String tipo, String bonificacion) {
        double salarioBase = Double.parseDouble(salario);
        if (tipo.equals("Administrativo")) {
            double bono = Double.parseDouble(bonificacion);
            return new EmpleadoAdministrativo(cedula, nombre, salarioBase, bono);
        } else if (tipo.equals("Comercial")) {
            double comision = Double.parseDouble(bonificacion);
            return new EmpleadoComercial(cedula, nombre, salarioBase, comision);
        }
        return new EmpleadoBase(cedula, nombre, salarioBase);
    }

    public String agregarEmpleado(String cedula, String nombre, String salario, String tipo, String bonificacion) {
        String error = validar(cedula, nombre, salario, tipo, bonificacion);
        if (error != null) {
            return error;
        }
        EmpleadoBase nuevo = construirEmpleado(cedula, nombre, salario, tipo, bonificacion);
        if (repositorio.agregar(nuevo)) {
            historial.add("AGREGADO: " + cedula + " - " + nombre);
            return "Empleado agregado correctamente.";
        }
        return "Ya existe un empleado con la cédula " + cedula + ".";
    }

    public EmpleadoBase buscarEmpleado(String cedula) {
        historial.add("BÚSQUEDA: " + cedula);
        return repositorio.buscar(cedula);
    }

    public String actualizarEmpleado(String cedula, String nombre, String salario, String tipo, String bonificacion) {
        String error = validar(cedula, nombre, salario, tipo, bonificacion);
        if (error != null) {
            return error;
        }
        EmpleadoBase actualizado = construirEmpleado(cedula, nombre, salario, tipo, bonificacion);
        if (repositorio.actualizar(actualizado)) {
            historial.add("ACTUALIZADO: " + cedula + " - " + nombre);
            return "Empleado actualizado correctamente.";
        }
        return "No existe ningún empleado con la cédula " + cedula + ".";
    }

    public String eliminarEmpleado(String cedula) {
        if (repositorio.eliminar(cedula)) {
            historial.add("ELIMINADO: " + cedula);
            return "Empleado eliminado correctamente.";
        }
        return "No existe ningún empleado con la cédula " + cedula + ".";
    }

    public ArrayList<EmpleadoBase> obtenerEmpleados() {
        ArrayList<EmpleadoBase> lista = repositorio.listarTodos();
        lista.sort(Comparator.comparing(EmpleadoBase::getNombre));
        return lista;
    }

    public double calcularTotalNomina() {
        double total = 0;
        for (EmpleadoBase empleado : repositorio.listarTodos()) {
            total += empleado.calcularSalarioTotal();
        }
        return total;
    }

    public ArrayList<String> obtenerHistorial() {
        return historial;
    }

    public HashMap<String, Integer> obtenerEstadisticas() {
        HashMap<String, Integer> conteo = new HashMap<>();
        for (String tipo : TIPOS_EMPLEADO) {
            conteo.put(tipo, 0);
        }
        for (EmpleadoBase emp : repositorio.listarTodos()) {
            String tipo = emp.getTipo();
            conteo.put(tipo, conteo.getOrDefault(tipo, 0) + 1);
        }
        return conteo;
    }
}
