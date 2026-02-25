package com.example.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import com.example.security.dto.UsuarioDTO;
import com.example.security.servicios.UserService;

@Controller
public class AuthController {


    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/")
    public String indexPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));

            boolean isUser = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_USER"));

            if (isAdmin) {
                return "redirect:/admin";
            } else if (isUser) {
                return "redirect:/user";
            }
        }

        return "redirect:/home";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/user")
    public String user() {
        return "user";
    }

    /**
     * Muestra el formulario de registro público
     */
    @GetMapping("/registrar")
    public String mostrarFormularioRegistro(Model model) {
        // Agregar un objeto UsuarioDTO vacío al modelo
        model.addAttribute("userForm", new UsuarioDTO());
        return "registrar";  // Debe coincidir con el nombre de tu template HTML
    }
    
    /**
     * Procesa el formulario de registro público
     * Este método siempre asignará ROLE_USER a los nuevos usuarios
     */
    @PostMapping("/registrar")
    public String procesarRegistro(@ModelAttribute("userForm") UsuarioDTO usuarioDTO, 
                                  RedirectAttributes redirectAttributes) {
        try {
            // Llamar al método específico para registro público que asigna ROLE_USER
            userService.registrarUsuarioPublico(usuarioDTO);
            
            // Mensaje de éxito
            redirectAttributes.addFlashAttribute("successMessage", "Usuario registrado exitosamente");
            return "redirect:/registrar?success";
            
        } catch (Exception e) {
            // Mensaje de error
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/registrar?error";
        }
    }

}
