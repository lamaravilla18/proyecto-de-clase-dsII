package com.example.security.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.security.modelo.Usuario;
import com.example.security.repositorio.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    System.out.println("Intentando autenticar usuario: " + username);
    
    Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> {
                System.out.println("Usuario no encontrado en la base de datos.");
                return new UsernameNotFoundException("Usuario no encontrado: " + username);
            });
    
    // Imprimir información detallada para depuración
    System.out.println("Usuario encontrado: " + usuario);
    System.out.println("Password en BD: " + usuario.getPassword());
    
    // Convertir roles a authorities de forma más explícita
    List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
            .map(role -> {
                System.out.println("Procesando rol: " + role);
                return new SimpleGrantedAuthority(role);
            })
            .collect(Collectors.toList());
    
    System.out.println("Authorities generados: " + authorities);
    
    return new User(
            usuario.getUsername(),
            usuario.getPassword(),
            authorities
    );
    }
}