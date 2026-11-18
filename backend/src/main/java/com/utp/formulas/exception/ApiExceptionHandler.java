package com.utp.formulas.exception;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> negocio(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("mensaje", ex.getReason() == null ? "Operación rechazada" : ex.getReason()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validar(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("mensaje", "Revisa los campos del formulario", "campos", campos));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> duplicado(DataIntegrityViolationException ex) {
        return ResponseEntity.status(409).body(Map.of("mensaje", "El documento o usuario ya existe, o el registro tiene relaciones que deben conservarse"));
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> prohibido(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(Map.of("mensaje", "Tu rol no puede realizar esta acción"));
    }
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "La solicitud contiene un formato incorrecto"));
    }
}
