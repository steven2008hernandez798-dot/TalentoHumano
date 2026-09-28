package vista;

import controlador.EmpleadoControlador;
import vista.VentanaEmpleado;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EmpleadoControlador controlador = new EmpleadoControlador();
            VentanaEmpleado ventana = new VentanaEmpleado(controlador);
            ventana.setVisible(true);
        });
    }
}
