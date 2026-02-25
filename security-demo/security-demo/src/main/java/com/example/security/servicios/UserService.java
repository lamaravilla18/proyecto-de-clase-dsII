package com.example.security.servicios;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ AGREGAR IMPORT

import com.example.security.dto.UsuarioDTO;
import com.example.security.modelo.Usuario;
import com.example.security.repositorio.UsuarioRepository;

@Service
public class UserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ============================================================
    // MÉTODOS DE CONSULTA
    // ============================================================

    /**
     * Devuelve todos los usuarios
     */
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca un usuario por su ID
     */
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Encontrar usuario por nombre de usuario
     */
    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    /**
     * Verificar si existe un usuario por username
     */
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    // ============================================================
    // MÉTODOS DE CREACIÓN
    // ============================================================

    /**
     * Guarda un NUEVO usuario (usado por el administrador)
     * Este método ENCRIPTA la contraseña y asigna roles
     */
    @Transactional
    public Usuario save(UsuarioDTO usuarioDTO) {
        Usuario usuario = new Usuario();
        usuario.setUsername(usuarioDTO.getUsername());
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword())); // ✅ Encriptar
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setUltimoAcceso(LocalDateTime.now());

        // Asignar roles desde el DTO
        Set<String> roles = new HashSet<>(usuarioDTO.getRoles());
        usuario.setRoles(roles);

        return usuarioRepository.save(usuario);
    }

    /**
     * Método específico para el REGISTRO PÚBLICO de usuarios
     * Siempre asigna ROLE_USER independientemente de lo que venga en el formulario
     */
    @Transactional
    public Usuario registrarUsuarioPublico(UsuarioDTO usuarioDTO) throws Exception {
        // Verificar si ya existe el username
        if (existsByUsername(usuarioDTO.getUsername())) {
            throw new Exception("El nombre de usuario ya está en uso");
        }

        // Verificar si ya existe el email
        if (usuarioRepository.findByEmail(usuarioDTO.getEmail()) != null) {
            throw new Exception("El correo electrónico ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(usuarioDTO.getUsername());
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setUltimoAcceso(LocalDateTime.now());

        // Asignar SOLO el rol ROLE_USER
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        usuario.setRoles(roles);

        return usuarioRepository.save(usuario);
    }

    // ============================================================
    // MÉTODOS DE ACTUALIZACIÓN
    // ============================================================

    /**
     * ✅ Método para guardar un usuario EXISTENTE (sin re-encriptar password)
     * Usar este método cuando la contraseña YA viene encriptada
     */
    @Transactional
    public Usuario saveExisting(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /**
     * Actualizar usuario por ID (método legacy - mantener por compatibilidad)
     */
    @Transactional
    public void updateUsuarioById(Long id, String nombre, String apellido, String email, String password) {
        Usuario usuario = findById(id);
        updateUsuario(usuario, nombre, apellido, email, password);
    }

    /**
     * Actualizar campos específicos de un usuario
     */
    @Transactional
    public void updateUsuario(Usuario usuario, String nombre, String apellido, String email, String password) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            usuario.setNombre(nombre);
        }
        if (apellido != null && !apellido.trim().isEmpty()) {
            usuario.setApellido(apellido);
        }
        if (email != null && !email.trim().isEmpty()) {
            usuario.setEmail(email);
        }
        if (password != null && !password.trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }
        usuarioRepository.save(usuario);
    }

    /**
     * ✅ Guardar usuario (con encriptación de contraseña)
     * Usar este método cuando se necesite encriptar la contraseña
     */
    @Transactional
    public void saveUser(Usuario usuario) {
        // Encriptar la contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        // Guardar el usuario
        usuarioRepository.save(usuario);
    }

    // ============================================================
    // MÉTODOS DE ELIMINACIÓN
    // ============================================================

    /**
     * Elimina un usuario por su ID
     */
    @Transactional
    public void delete(Long id) {
        Usuario usuario = findById(id);
        usuarioRepository.delete(usuario);
    }
}