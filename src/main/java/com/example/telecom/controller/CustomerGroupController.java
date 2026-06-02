package com.example.telecom.controller;

import com.example.telecom.dto.CustomerGroupDTO;
import com.example.telecom.entity.CustomerGroupMember;
import com.example.telecom.service.CustomerGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerGroupController {

    private final CustomerGroupService customerGroupService;

    @PostMapping
    @PreAuthorize("hasAnyRole('VENTE','DSI')")
    public ResponseEntity<CustomerGroupDTO> creer(@RequestBody CustomerGroupDTO dto) {
        return ResponseEntity.ok(customerGroupService.creer(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENTE','DSI')")
    public ResponseEntity<CustomerGroupDTO> modifier(@PathVariable Long id, @RequestBody CustomerGroupDTO dto) {
        return ResponseEntity.ok(customerGroupService.modifier(id, dto));
    }

    @PostMapping("/{groupId}/members/{customerId}")
    @PreAuthorize("hasAnyRole('VENTE','DSI')")
    public ResponseEntity<CustomerGroupDTO> ajouterClient(
            @PathVariable Long groupId,
            @PathVariable Long customerId,
            @RequestParam(required = false) CustomerGroupMember.MemberRole memberRole,
            @RequestParam(defaultValue = "false") boolean primaryMember) {
        return ResponseEntity.ok(customerGroupService.ajouterClient(groupId, customerId, memberRole, primaryMember));
    }

    @DeleteMapping("/{groupId}/members/{customerId}")
    @PreAuthorize("hasAnyRole('VENTE','DSI')")
    public ResponseEntity<CustomerGroupDTO> retirerClient(@PathVariable Long groupId, @PathVariable Long customerId) {
        return ResponseEntity.ok(customerGroupService.retirerClient(groupId, customerId));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('VENTE','DSI','EXPLOIT')")   // EXPLOIT ajouté pour ExploitPromotion
    public ResponseEntity<List<CustomerGroupDTO.MemberDTO>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(customerGroupService.getMembers(id));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENTE','DSI')")
    public ResponseEntity<CustomerGroupDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerGroupService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VENTE','DSI')")
    public ResponseEntity<List<CustomerGroupDTO>> getAll() {
        return ResponseEntity.ok(customerGroupService.getAll());
    }
}
