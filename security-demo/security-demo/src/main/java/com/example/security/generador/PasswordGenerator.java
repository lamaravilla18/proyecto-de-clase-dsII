package com.example.security.generador;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generar contraseña para admin
        String adminPassword = "admin";
        String encodedAdminPassword = encoder.encode(adminPassword);
        System.out.println("Contraseña encriptada para admin: " + encodedAdminPassword);
        
        // Generar contraseña para usuario
        String userPassword = "user";
        String encodedUserPassword = encoder.encode(userPassword);
        System.out.println("Contraseña encriptada para user: " + encodedUserPassword);
    }
}