package controlador;

import modelo.EmpleadoAdministrativo;
import modelo.EmpleadoBase;
import modelo.EmpleadoComercial;
import modelo.RepositorioEmpleado;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * El "cerebro" del sistema: recibe lo que el usuario escribe en la ventana,
 * lo valida y decide qué hacer con los datos.
 */
public class EmpleadoControlador {

    // Array: lista FIJA de tipos de empleado
    public static final String[] TIPOS_EMPLEADO = {"Operativo", "Administrativo", "Comercial"};

    private final RepositorioEmpleado repositorio;
    private final ArrayList<String> historial;

    public EmpleadoControlador() {
        repositorio = new RepositorioEmpleado();
        historial = new ArrayList<>();
    }

    // Revisa carácter por carácter que el texto sea un número positivo válido
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

    // Devuelve un mensaje de error, o null si todo está correcto
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

    // Fábrica de empleados: decide qué clase instanciar según el tipo
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

    // OPERACIONES CRUD
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

    // Polimorfismo en acción: cada empleado calcula SU propio salario total
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