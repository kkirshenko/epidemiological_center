package com.sanepidcenter.controller;

import com.sanepidcenter.model.Organization;
import com.sanepidcenter.repository.OrganizationTypeRepository;
import com.sanepidcenter.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final OrganizationTypeRepository organizationTypeRepository;

    @GetMapping
    public String listOrganizations(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            Model model) {
        List<Organization> organizations;
        if (query != null && !query.isEmpty()) {
            organizations = organizationService.searchAllFields(query);
        } else {
            organizations = organizationService.getAllOrganizations();
        }
        
        if (sortBy != null && !sortBy.isEmpty()) {
            organizations = organizationService.sortOrganizations(organizations, sortBy, sortDir);
        }
        
        model.addAttribute("organizations", organizations);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("sortBy", sortBy != null ? sortBy : "");
        model.addAttribute("sortDir", sortDir != null ? sortDir : "asc");
        return "organizations/list";
    }

    @GetMapping("/{id}")
    public String viewOrganization(@PathVariable UUID id, Model model) {
        return organizationService.getOrganizationById(id)
                .map(organization -> {
                    model.addAttribute("organization", organization);
                    return "organizations/view";
                })
                .orElse("redirect:/organizations");
    }

    @GetMapping("/new")
    public String newOrganizationForm(Model model) {
        model.addAttribute("organization", new Organization());
        model.addAttribute("organizationTypes", organizationTypeRepository.findAll());
        return "organizations/form";
    }

    @PostMapping
    public String createOrganization(@ModelAttribute Organization organization) {
        try {
            organizationService.createOrganization(organization);
            return "redirect:/organizations";
        } catch (RuntimeException e) {
            return "redirect:/organizations/new?error=save_failed";
        }
    }

    @GetMapping("/{id}/edit")
    public String editOrganizationForm(@PathVariable UUID id, Model model) {
        return organizationService.getOrganizationById(id)
                .map(organization -> {
                    model.addAttribute("organization", organization);
                    model.addAttribute("organizationTypes", organizationTypeRepository.findAll());
                    model.addAttribute("orgId", id);
                    return "organizations/form";
                })
                .orElse("redirect:/organizations");
    }

    @PostMapping("/{id}")
    public String updateOrganization(@PathVariable UUID id, @ModelAttribute Organization organization) {
        try {
            organizationService.updateOrganization(id, organization);
            return "redirect:/organizations";
        } catch (RuntimeException e) {
            return "redirect:/organizations/" + id + "/edit?error=save_failed";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return "redirect:/organizations";
    }

    @GetMapping("/search")
    public String searchOrganizations(@RequestParam String query, Model model) {
        return "redirect:/organizations?query=" + query;
    }
}
