package com.example.telecom.service;

import com.example.telecom.dto.ServiceDTO;
import com.example.telecom.dto.PaginatedResponse;
import com.example.telecom.entity.Services;
import com.example.telecom.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceTelecom {

    private final ServiceRepository serviceRepository;

    public ServiceDTO creer(ServiceDTO dto) {
        Services s = Services.builder().nomService(dto.getNomService()).description(dto.getDescription()).build();
        return toDTO(serviceRepository.save(s));
    }

    @Transactional
    public List<ServiceDTO> creerLots(List<ServiceDTO> dtos) {
        return dtos.stream()
                .map(this::creer)
                .collect(Collectors.toList());
    }

    public ServiceDTO modifier(Long id, ServiceDTO dto) {
        Services s = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service introuvable : " + id));
        s.setNomService(dto.getNomService());
        s.setDescription(dto.getDescription());
        return toDTO(serviceRepository.save(s));
    }

    public ServiceDTO getById(Long id) {
        return toDTO(serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service introuvable : " + id)));
    }

    public PaginatedResponse<ServiceDTO> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<ServiceDTO> result = serviceRepository.findAll(pageable).map(this::toDTO);
        return buildPaginatedResponse(result);
    }

    public void supprimer(Long id) {
        serviceRepository.deleteById(id);
    }

    private ServiceDTO toDTO(Services s) {
        return ServiceDTO.builder().id(s.getId()).nomService(s.getNomService()).description(s.getDescription()).build();
    }

    private PaginatedResponse<ServiceDTO> buildPaginatedResponse(Page<ServiceDTO> page) {
        return PaginatedResponse.<ServiceDTO>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
