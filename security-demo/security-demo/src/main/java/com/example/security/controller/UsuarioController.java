package com.example.security.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.dto.UsuarioDTO;
import com.example.security.modelo.Usuario;
import com.example.security.servicios.UserService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = userService.findAll();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Crea un nuevo usuario
     */
    @PostMapping("/crear")
    public ResponseEntity<Usuario> createUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        try {
            Usuario nuevoUsuario = userService.save(usuarioDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Elimina un usuario por su ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
        try {
            userService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Usuario no encontrado con ID: " + id);
        }
    }

    /**
     * Obtiene un usuario por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuarioById(@PathVariable Long id) {
        try {
            Usuario usuario = userService.findById(id);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Usuario no encontrado con ID: " + id);
        }
    }

    /**
     * Obtener el usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUsuarioAutenticado(Principal principal) {
        try {
            String nombreUsuario = principal.getName();
            Usuario usuario = userService.findByUsername(nombreUsuario);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Usuario no encontrado");
        }
    }
    
    /**
     * ✅ ACTUALIZAR USUARIO COMPLETO (con username, roles y password opcional)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuarioPorId(
            @PathVariable Long id,
            @RequestBody UsuarioDTO usuarioDTO) {
        
        try {
            // Buscar el usuario existente
            Usuario usuarioExistente = userService.findById(id);
            
            // Actualizar campos básicos
            usuarioExistente.setUsername(usuarioDTO.getUsername());
            usuarioExistente.setNombre(usuarioDTO.getNombre());
            usuarioExistente.setApellido(usuarioDTO.getApellido());
            usuarioExistente.setEmail(usuarioDTO.getEmail());
            
            // ✅ Solo actualizar password si se proporcionó uno nuevo
            if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().trim().isEmpty()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
            }
            
            // ✅ Actualizar roles si se proporcionaron
            if (usuarioDTO.getRoles() != null && !usuarioDTO.getRoles().isEmpty()) {
                Set<String> roles = new HashSet<>(usuarioDTO.getRoles());
                usuarioExistente.setRoles(roles);
            }
            
            // Guardar cambios (sin re-encriptar password)
            Usuario usuarioActualizado = userService.saveExisting(usuarioExistente);
            
            return ResponseEntity.ok(usuarioActualizado);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Usuario no encontrado con ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar usuario: " + e.getMessage());
        }
    }
}