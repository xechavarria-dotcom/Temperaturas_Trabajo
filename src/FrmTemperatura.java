import datechooser.beans.DateChooserCombo;
import entidades.TemperaturaRegistro;
import Servicios.TemperaturaServicio;

import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

public class FrmTemperatura extends JFrame {

    private DateChooserCombo dccDesde, dccHasta, dccConsulta;
    private JPanel pnlEstadisticas;
    private JTextArea txtResultados;
    private JTabbedPane tabs;
    private TemperaturaServicio servicio;

    public FrmTemperatura() {
        setTitle("Temperaturas por Ciudad");
        setSize(750, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Cargar datos una sola vez
        servicio = new TemperaturaServicio();
        servicio.cargarDesdeCSV("src/Datos/Temperaturas.csv");

        JToolBar barra = new JToolBar();

        JButton btnPromedios = new JButton(new ImageIcon(getClass().getResource("/iconos/Grafica.png")));
        btnPromedios.setToolTipText("Ver promedios por ciudad");
        btnPromedios.addActionListener(this::mostrarPromedios);
        barra.add(btnPromedios);

        JButton btnConsulta = new JButton(new ImageIcon(getClass().getResource("/iconos/Datos.png")));
        btnConsulta.setToolTipText("Consultar ciudad más y menos calurosa");
        btnConsulta.addActionListener(this::consultarExtremos);
        barra.add(btnConsulta);

        JPanel pnlContenedor = new JPanel();
        pnlContenedor.setLayout(new BoxLayout(pnlContenedor, BoxLayout.Y_AXIS));

        JPanel pnlFiltros = new JPanel();
        pnlFiltros.setPreferredSize(new Dimension(pnlFiltros.getWidth(), 50));
        pnlFiltros.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlFiltros.setLayout(null);

        JLabel lblDesde = new JLabel("Desde:");
        lblDesde.setBounds(10, 10, 50, 25);
        pnlFiltros.add(lblDesde);

        dccDesde = new DateChooserCombo();
        dccDesde.setBounds(60, 10, 120, 25);
        pnlFiltros.add(dccDesde);

        JLabel lblHasta = new JLabel("Hasta:");
        lblHasta.setBounds(190, 10, 50, 25);
        pnlFiltros.add(lblHasta);

        dccHasta = new DateChooserCombo();
        dccHasta.setBounds(240, 10, 120, 25);
        pnlFiltros.add(dccHasta);

        JLabel lblConsulta = new JLabel("Consulta:");
        lblConsulta.setBounds(370, 10, 70, 25);
        pnlFiltros.add(lblConsulta);

        dccConsulta = new DateChooserCombo();
        dccConsulta.setBounds(440, 10, 120, 25);
        pnlFiltros.add(dccConsulta);

        // Configuración del JTextArea y centrado
        txtResultados = new JTextArea(10, 50);
        txtResultados.setEditable(false);
        txtResultados.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtResultados.setAlignmentY(Component.CENTER_ALIGNMENT);
        txtResultados.setLineWrap(true);
        txtResultados.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtResultados);

        // Contenedor con FlowLayout centrado para los resultados
        JPanel pnlResultadosContenedor = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlResultadosContenedor.add(scroll);

        pnlEstadisticas = new JPanel(new BorderLayout());
        pnlEstadisticas.add(pnlResultadosContenedor, BorderLayout.CENTER);

        // Crear el JTabbedPane y agregar la pestaña de la gráfica en la posición 0
        tabs = new JTabbedPane();
        tabs.addTab("Gráfica", null); // Pestaña de gráfica en la posición 0
        tabs.addTab("Resultados", pnlEstadisticas); // Pestaña de resultados

        pnlContenedor.add(pnlFiltros);
        pnlContenedor.add(tabs);

        getContentPane().add(barra, BorderLayout.NORTH);
        getContentPane().add(pnlContenedor, BorderLayout.CENTER);
    }

    private void mostrarPromedios(ActionEvent e) {
        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Obtener los promedios por ciudad
        Map<String, Double> promedios = servicio.calcularPromedioPorCiudad(desde, hasta);

        // Crear la gráfica de barras
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        promedios.forEach((ciudad, temperatura) -> {
            dataset.addValue(temperatura, "Promedio", ciudad);
        });

        JFreeChart chart = ChartFactory.createBarChart(
                "Promedio de Temperaturas por Ciudad", // Título del gráfico
                "Ciudad", // Eje X
                "Temperatura (°C)", // Eje Y
                dataset, // Datos
                PlotOrientation.VERTICAL, // Orientación de la gráfica
                true, // Mostrar leyenda
                true, // Mostrar tooltips
                false // Mostrar URLs
        );

        // Crear un panel para mostrar la gráfica
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400)); // Tamaño del panel

        // Limpiar el panel antes de agregar la nueva gráfica
        JPanel pnlGrafica = new JPanel();
        pnlGrafica.setLayout(new BorderLayout());
        pnlGrafica.add(chartPanel, BorderLayout.CENTER);

        // Mostrar la gráfica en la primera pestaña
        tabs.setComponentAt(0, pnlGrafica);
        tabs.setSelectedIndex(0); // Cambia a la pestaña de gráfica
    }

    private void consultarExtremos(ActionEvent e) {
        LocalDate consulta = dccConsulta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Optional<TemperaturaRegistro> masCalurosa = servicio.obtenerCiudadMasCalurosa(consulta);
        Optional<TemperaturaRegistro> masFria = servicio.obtenerCiudadMasFria(consulta);

        StringBuilder sb = new StringBuilder("Consulta de temperaturas para: ").append(consulta).append("\n\n");
        sb.append("Ciudad más calurosa: ")
                .append(masCalurosa.map(TemperaturaRegistro::toString).orElse("Sin datos")).append("\n");
        sb.append("Ciudad más fría: ")
                .append(masFria.map(TemperaturaRegistro::toString).orElse("Sin datos")).append("\n");

        // Actualiza el texto en el área de resultados
        txtResultados.setText(sb.toString());

        // Revalidar y repintar el panel de estadísticas para asegurar que se actualiza visualmente
        pnlEstadisticas.revalidate();
        pnlEstadisticas.repaint();

        // Cambia a la pestaña de resultados
        tabs.setSelectedIndex(1);
    }
}
