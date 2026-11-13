package ni.edu.uam.laboratorioorm.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.laboratorioorm.dao.EquipoDAO;
import ni.edu.uam.laboratorioorm.dao.TipoEquipoDAO;
import ni.edu.uam.laboratorioorm.model.Equipo;
import ni.edu.uam.laboratorioorm.model.TipoEquipo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LaboratorioController {

    private static final List<String> ESTADOS_PERMITIDOS = List.of(
            "Disponible",
            "En mantenimiento",
            "Fuera de servicio"
    );

    // Campos del formulario
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtMarca;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<TipoEquipo> cmbTipoEquipo;
    @FXML private ComboBox<String> cmbEstado;

    // Botones CRUD
    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;

    // Controles de búsqueda y filtro
    @FXML private TextField txtBuscarNombre;
    @FXML private ComboBox<TipoEquipo> cmbFiltroTipo;
    @FXML private Button btnBuscar;
    @FXML private Button btnMostrarTodos;

    // Tabla y columnas
    @FXML private TableView<Equipo> tblEquipos;
    @FXML private TableColumn<Equipo, String> colCodigo;
    @FXML private TableColumn<Equipo, String> colNombre;
    @FXML private TableColumn<Equipo, String> colMarca;
    @FXML private TableColumn<Equipo, String> colEstado;
    @FXML private TableColumn<Equipo, Integer> colCantidad;
    @FXML private TableColumn<Equipo, String> colTipoEquipo;

    private final EquipoDAO equipoDAO = new EquipoDAO();
    private final TipoEquipoDAO tipoEquipoDAO = new TipoEquipoDAO();

    private Long idSeleccionado = null;

    @FXML
    public void initialize() {
        configurarTabla();
        configurarCombos();

        tblEquipos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) {
                        seleccionarEquipo(actual);
                    } else {
                        idSeleccionado = null;
                        actualizarEstadoBotones();
                    }
                });

        actualizarEstadoBotones();
        cargarTiposEquipo();
        cargarEquipos();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        // Muestra el nombre legible del TipoEquipo relacionado
        colTipoEquipo.setCellValueFactory(cellData -> {
            TipoEquipo tipo = cellData.getValue().getTipoEquipo();
            String nombreTipo = (tipo != null) ? tipo.getNombre() : "Sin tipo";
            return new SimpleStringProperty(nombreTipo);
        });
    }

    private void configurarCombos() {
        cmbEstado.setItems(FXCollections.observableArrayList(ESTADOS_PERMITIDOS));
    }

    private void cargarTiposEquipo() {
        try {
            List<TipoEquipo> tipos = tipoEquipoDAO.listar();

            // ComboBox del formulario
            cmbTipoEquipo.setItems(FXCollections.observableArrayList(tipos));

            // ComboBox del filtro (incluye opción "Todos los tipos" con idTipo = null)
            List<TipoEquipo> opcionesFiltro = new ArrayList<>();
            TipoEquipo opcionTodos = new TipoEquipo();
            opcionTodos.setIdTipo(null);
            opcionTodos.setNombre("Todos los tipos");
            opcionesFiltro.add(opcionTodos);
            opcionesFiltro.addAll(tipos);

            cmbFiltroTipo.setItems(FXCollections.observableArrayList(opcionesFiltro));
            cmbFiltroTipo.getSelectionModel().selectFirst();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de conexión",
                    "No se pudieron cargar los tipos de equipo desde PostgreSQL.\nDetalle: " + e.getMessage());
        }
    }

    private void seleccionarEquipo(Equipo equipo) {
        idSeleccionado = equipo.getIdEquipo();

        txtCodigo.setText(equipo.getCodigo());
        txtNombre.setText(equipo.getNombre());
        txtMarca.setText(equipo.getMarca() != null ? equipo.getMarca() : "");
        txtCantidad.setText(String.valueOf(equipo.getCantidad()));
        cmbEstado.setValue(equipo.getEstado());

        TipoEquipo tipo = equipo.getTipoEquipo();
        if (tipo != null && tipo.getIdTipo() != null) {
            for (TipoEquipo item : cmbTipoEquipo.getItems()) {
                if (tipo.getIdTipo().equals(item.getIdTipo())) {
                    cmbTipoEquipo.getSelectionModel().select(item);
                    break;
                }
            }
        } else {
            cmbTipoEquipo.getSelectionModel().clearSelection();
        }

        actualizarEstadoBotones();
    }

    private void actualizarEstadoBotones() {
        boolean sinSeleccion = (idSeleccionado == null);
        btnActualizar.setDisable(sinSeleccion);
        btnEliminar.setDisable(sinSeleccion);
    }

    private Equipo construirYValidarEquipo(Long idActual) {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String marca = txtMarca.getText().trim();
        String cantidadTexto = txtCantidad.getText().trim();
        String estado = cmbEstado.getValue();
        TipoEquipo tipoSeleccionado = cmbTipoEquipo.getValue();

        if (codigo.isEmpty()) {
            throw new IllegalArgumentException("El código del equipo es obligatorio.");
        }
        if (codigo.length() > 50) {
            throw new IllegalArgumentException("El código no puede exceder los 50 caracteres.");
        }

        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre del equipo es obligatorio.");
        }
        if (nombre.length() > 150) {
            throw new IllegalArgumentException("El nombre no puede exceder los 150 caracteres.");
        }

        if (marca.length() > 100) {
            throw new IllegalArgumentException("La marca no puede exceder los 100 caracteres.");
        }

        if (cantidadTexto.isEmpty()) {
            throw new IllegalArgumentException("La cantidad es obligatoria.");
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadTexto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero válido.");
        }

        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa (debe ser >= 0).");
        }

        if (estado == null || !ESTADOS_PERMITIDOS.contains(estado)) {
            throw new IllegalArgumentException("Debe seleccionar un estado permitido (Disponible, En mantenimiento o Fuera de servicio).");
        }

        if (tipoSeleccionado == null || tipoSeleccionado.getIdTipo() == null) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de equipo.");
        }

        if (equipoDAO.existeCodigo(codigo, idActual)) {
            throw new IllegalArgumentException("Ya existe un equipo registrado con el código '" + codigo + "'.");
        }

        Equipo equipo = new Equipo();
        equipo.setCodigo(codigo);
        equipo.setNombre(nombre);
        equipo.setMarca(marca.isEmpty() ? null : marca);
        equipo.setEstado(estado);
        equipo.setCantidad(cantidad);
        equipo.setTipoEquipo(tipoSeleccionado);

        return equipo;
    }

    @FXML
    private void guardarEquipo() {
        try {
            if (idSeleccionado != null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Registro nuevo",
                        "Tiene un equipo seleccionado. Presione 'Limpiar' antes de registrar uno nuevo.");
                return;
            }

            Equipo nuevoEquipo = construirYValidarEquipo(null);
            equipoDAO.guardar(nuevoEquipo);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa",
                    "Equipo registrado correctamente.");
            limpiarFormulario();
            aplicarFiltros();

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación de datos", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar",
                    "No se pudo registrar el equipo en la base de datos.\nDetalle: " + obtenerMensajeCausa(e));
        }
    }

    @FXML
    private void actualizarEquipo() {
        try {
            if (idSeleccionado == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                        "Seleccione un equipo de la tabla para actualizar.");
                return;
            }

            Equipo equipoEditado = construirYValidarEquipo(idSeleccionado);
            equipoEditado.setIdEquipo(idSeleccionado);

            equipoDAO.actualizar(equipoEditado);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa",
                    "Equipo actualizado correctamente.");
            limpiarFormulario();
            aplicarFiltros();

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación de datos", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error al actualizar",
                    "No se pudo actualizar el equipo.\nDetalle: " + obtenerMensajeCausa(e));
        }
    }

    @FXML
    private void eliminarEquipo() {
        if (idSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                    "Seleccione un equipo de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("Eliminar equipo del inventario");
        confirmacion.setContentText("¿Está seguro de eliminar el equipo '" + txtNombre.getText().trim() + "' (" + txtCodigo.getText().trim() + ")?");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            try {
                boolean eliminado = equipoDAO.eliminar(idSeleccionado);
                if (!eliminado) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Registro no encontrado",
                            "El equipo ya no existe en la base de datos.");
                } else {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa",
                            "Equipo eliminado correctamente.");
                }
                limpiarFormulario();
                aplicarFiltros();
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Error al eliminar",
                        "No se pudo eliminar el equipo.\nDetalle: " + obtenerMensajeCausa(e));
            }
        }
    }

    @FXML
    private void aplicarFiltros() {
        try {
            String fragmentoNombre = txtBuscarNombre.getText();
            TipoEquipo tipoFiltro = cmbFiltroTipo.getValue();

            List<Equipo> resultados = equipoDAO.buscarPorNombreYTipo(fragmentoNombre, tipoFiltro);
            tblEquipos.setItems(FXCollections.observableArrayList(resultados));
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de consulta",
                    "No se pudo realizar la búsqueda de equipos.\nDetalle: " + obtenerMensajeCausa(e));
        }
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscarNombre.clear();
        if (!cmbFiltroTipo.getItems().isEmpty()) {
            cmbFiltroTipo.getSelectionModel().selectFirst();
        }
        cargarEquipos();
    }

    private void cargarEquipos() {
        try {
            List<Equipo> lista = equipoDAO.listar();
            tblEquipos.setItems(FXCollections.observableArrayList(lista));
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de consulta",
                    "No se pudieron cargar los equipos desde PostgreSQL.\nDetalle: " + obtenerMensajeCausa(e));
        }
    }

    @FXML
    private void limpiarFormulario() {
        tblEquipos.getSelectionModel().clearSelection();
        idSeleccionado = null;

        txtCodigo.clear();
        txtNombre.clear();
        txtMarca.clear();
        txtCantidad.clear();
        cmbTipoEquipo.getSelectionModel().clearSelection();
        cmbEstado.getSelectionModel().clearSelection();

        actualizarEstadoBotones();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private String obtenerMensajeCausa(Throwable throwable) {
        Throwable causa = throwable;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        return causa.getMessage() != null ? causa.getMessage() : throwable.toString();
    }
}