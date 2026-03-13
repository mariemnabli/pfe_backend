package com.example.telecom.service;

import com.example.telecom.dto.ServiceDTO;
import com.example.telecom.entity.Services;
import com.example.telecom.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;

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

    public List<ServiceDTO> getAll() {
        return serviceRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void supprimer(Long id) {
        serviceRepository.deleteById(id);
    }

    private ServiceDTO toDTO(Services s) {
        return ServiceDTO.builder().id(s.getId()).nomService(s.getNomService()).description(s.getDescription()).build();
    }
}