package com.sanepidcenter.controller;

import com.sanepidcenter.model.Inspection;
import com.sanepidcenter.repository.InspectionTypeRepository;
import com.sanepidcenter.repository.ProfileRepository;
import com.sanepidcenter.service.InspectionService;
import com.sanepidcenter.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;
    private final OrganizationService organizationService;
    private final InspectionTypeRepository inspectionTypeRepository;
    private final ProfileRepository profileRepository;

    @GetMapping
    public String listInspections(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            Model model) {
        List<Inspection> inspections;
        if (query != null && !query.isEmpty()) {
            inspections = inspectionService.searchAllFields(query);
        } else {
            inspections = inspectionService.getAllInspections();
        }
        
        if (sortBy != null && !sortBy.isEmpty()) {
            inspections = inspectionService.sortInspections(inspections, sortBy, sortDir);
        }
        
        model.addAttribute("inspections", inspections);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("sortBy", sortBy != null ? sortBy : "");
        model.addAttribute("sortDir", sortDir != null ? sortDir : "asc");
        return "inspections/list";
    }

    @GetMapping("/{id}")
    public String viewInspection(@PathVariable UUID id, Model model) {
        return inspectionService.getInspectionById(id)
                .map(inspection -> {
                    model.addAttribute("inspection", inspection);
                    return "inspections/view";
                })
                .orElse("redirect:/inspections");
    }

    @GetMapping("/new")
    public String newInspectionForm(Model model) {
        model.addAttribute("inspection", new Inspection());
        enrichFormModel(model);
        return "inspections/form";
    }

    @PostMapping
    public String createInspection(@ModelAttribute Inspection inspection) {
        try {
            inspectionService.createInspection(inspection);
            return "redirect:/inspections";
        } catch (RuntimeException e) {
            return "redirect:/inspections/new?error=save_failed";
        }
    }

    @GetMapping("/{id}/edit")
    public String editInspectionForm(@PathVariable UUID id, Model model) {
        return inspectionService.getInspectionById(id)
                .map(inspection -> {
                    if (inspection.getScheduledDate() == null && inspection.getStartDate() != null) {
                        inspection.setScheduledDate(inspection.getStartDate());
                    }
                    if (inspection.getScheduledDate() == null) {
                        inspection.setScheduledDate(java.time.LocalDate.now());
                    }
                    model.addAttribute("inspection", inspection);
                    enrichFormModel(model);
                    return "inspections/form";
                })
                .orElse("redirect:/inspections");
    }

    @PostMapping("/{id}")
    public String updateInspection(@PathVariable UUID id, @ModelAttribute Inspection inspection) {
        try {
            inspectionService.updateInspection(id, inspection);
            return "redirect:/inspections";
        } catch (RuntimeException e) {
            return "redirect:/inspections/" + id + "/edit?error=save_failed";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteInspection(@PathVariable UUID id) {
        inspectionService.deleteInspection(id);
        return "redirect:/inspections";
    }

    @GetMapping("/planned")
    public String listPlannedInspections(Model model) {
        List<Inspection> inspections = inspectionService.getPlannedInspections();
        model.addAttribute("inspections", inspections);
        return "inspections/list";
    }

    private void enrichFormModel(Model model) {
        model.addAttribute("organizations", organizationService.getAllOrganizations());
        model.addAttribute("inspectionTypes", inspectionTypeRepository.findAll());
        model.addAttribute("inspectors", profileRepository.findAll().stream()
                .filter(profile -> "ROLE_INSPECTOR".equals(profile.getRole()))
                .toList());
    }
}
