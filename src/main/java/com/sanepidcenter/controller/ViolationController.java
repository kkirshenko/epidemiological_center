package com.sanepidcenter.controller;

import com.sanepidcenter.service.InspectionService;
import com.sanepidcenter.service.ViolationService;
import com.sanepidcenter.repository.InspectionRepository;
import com.sanepidcenter.repository.ViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/violations")
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService violationService;
    private final InspectionService inspectionService;
    private final InspectionRepository inspectionRepository;
    private final ViolationRepository violationRepository;

    @GetMapping
    public String listViolations(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            Model model) {
        List<com.sanepidcenter.model.Violation> violations;
        if (query != null && !query.isEmpty()) {
            violations = violationService.searchAllFields(query);
        } else {
            violations = violationService.getAllViolations();
        }
        
        if (sortBy != null && !sortBy.isEmpty()) {
            violations = violationService.sortViolations(violations, sortBy, sortDir);
        }
        
        model.addAttribute("violations", violations);
        model.addAttribute("inspections", inspectionService.getAllInspections());
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("sortBy", sortBy != null ? sortBy : "");
        model.addAttribute("sortDir", sortDir != null ? sortDir : "asc");
        return "violations/list";
    }

    @GetMapping("/{id}")
    public String viewViolation(@PathVariable UUID id, Model model) {
        return violationService.getViolationById(id)
                .map(violation -> {
                    Long resolutionDays = null;
                    if (Boolean.TRUE.equals(violation.getResolved())
                            && violation.getCreatedAt() != null
                            && violation.getCorrectionDeadline() != null) {
                        resolutionDays = ChronoUnit.DAYS.between(
                                violation.getCreatedAt().toLocalDate(),
                                violation.getCorrectionDeadline());
                    }
                    model.addAttribute("violation", violation);
                    model.addAttribute("resolutionDays", resolutionDays);
                    return "violations/view";
                })
                .orElse("redirect:/violations");
    }

    @GetMapping("/new")
    public String newViolationForm(Model model) {
        model.addAttribute("violation", new com.sanepidcenter.model.Violation());
        model.addAttribute("inspections", inspectionService.getAllInspections());
        return "violations/form";
    }

    @PostMapping
    public String createViolation(
            @RequestParam UUID inspectionId,
            @RequestParam String description,
            @RequestParam String severity,
            RedirectAttributes redirectAttributes) {
        try {
            com.sanepidcenter.model.Violation violation = new com.sanepidcenter.model.Violation();
            violation.setInspection(inspectionRepository.findById(inspectionId)
                    .orElseThrow(() -> new IllegalArgumentException("Inspection not found")));
            violation.setDescription(description);
            violation.setSeverity(severity);
            violation.setCode("N/A");
            violation.setArticleReference("Не указана");
            violation.setResolutionNotes("");
            violation.setCorrectionDeadline(null);
            violation.setResolved(false);
            violationRepository.save(violation);
            redirectAttributes.addFlashAttribute("successMessage", "Нарушение успешно добавлено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при добавлении нарушения: " + e.getMessage());
        }
        return "redirect:/violations";
    }

    @GetMapping("/{id}/edit")
    public String editViolationForm(@PathVariable UUID id, Model model) {
        return violationService.getViolationById(id)
                .map(violation -> {
                    model.addAttribute("violation", violation);
                    model.addAttribute("inspections", inspectionService.getAllInspections());
                    return "violations/form";
                })
                .orElse("redirect:/violations");
    }

    @PostMapping("/{id}/edit")
    public String updateViolation(
            @PathVariable UUID id,
            @RequestParam UUID inspectionId,
            @RequestParam String description,
            @RequestParam String severity,
            @RequestParam(required = false) Boolean resolved,
            RedirectAttributes redirectAttributes) {
        try {
            com.sanepidcenter.model.Violation violationDetails = new com.sanepidcenter.model.Violation();
            violationDetails.setInspection(inspectionRepository.findById(inspectionId)
                    .orElseThrow(() -> new IllegalArgumentException("Inspection not found")));
            violationDetails.setDescription(description);
            violationDetails.setSeverity(severity);
            violationDetails.setCode("N/A");
            violationDetails.setArticleReference("Не указана");
            violationDetails.setResolutionNotes("");
            violationDetails.setResolved(resolved != null && resolved);
            
            violationService.updateViolation(id, violationDetails);
            redirectAttributes.addFlashAttribute("successMessage", "Нарушение успешно обновлено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении нарушения: " + e.getMessage());
        }
        return "redirect:/violations";
    }

    @PostMapping("/{id}/delete")
    public String deleteViolation(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            violationService.deleteViolation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Нарушение успешно удалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении нарушения: " + e.getMessage());
        }
        return "redirect:/violations";
    }
}
