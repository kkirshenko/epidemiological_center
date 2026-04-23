package com.sanepidcenter.controller;

import com.sanepidcenter.dto.ProfileDto;
import com.sanepidcenter.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing user profiles (admin only).
 */
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public String listUsers(Model model) {
        List<ProfileDto> users = profileService.getAllProfiles();
        model.addAttribute("users", users);
        return "users/list";
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable UUID id, Model model) {
        ProfileDto user = profileService.getProfileById(id);
        if (user == null) {
            return "redirect:/users";
        }
        model.addAttribute("user", user);
        return "users/form";
    }

    @PostMapping("/{id}/update")
    public String updateUser(@PathVariable UUID id,
                             @RequestParam String fullName,
                             @RequestParam String phone,
                             @RequestParam String position,
                             @RequestParam String role,
                             @RequestParam Boolean isActive,
                             RedirectAttributes redirectAttributes) {
        ProfileDto dto = new ProfileDto();
        dto.setFullName(fullName);
        dto.setPhone(phone);
        dto.setPosition(position);
        dto.setRole(role);
        dto.setIsActive(isActive);
        
        profileService.updateProfile(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Пользователь обновлен");
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        profileService.deleteProfile(id);
        redirectAttributes.addFlashAttribute("successMessage", "Пользователь удален");
        return "redirect:/users";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        profileService.toggleProfileStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Статус пользователя изменен");
        return "redirect:/users";
    }
}
