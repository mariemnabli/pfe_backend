package com.example.telecom.service;

import com.example.telecom.dto.CustomerGroupDTO;
import com.example.telecom.entity.Client;
import com.example.telecom.entity.CustomerGroup;
import com.example.telecom.entity.CustomerGroupMember;
import com.example.telecom.repository.ClientRepository;
import com.example.telecom.repository.CustomerGroupMemberRepository;
import com.example.telecom.repository.CustomerGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerGroupService {

    private final CustomerGroupRepository customerGroupRepository;
    private final CustomerGroupMemberRepository customerGroupMemberRepository;
    private final ClientRepository clientRepository;

    public CustomerGroupDTO creer(CustomerGroupDTO dto) {
        CustomerGroup group = CustomerGroup.builder()
                .groupCode(genererGroupCode())
                .name(dto.getName())
                .groupType(dto.getGroupType())
                .status(dto.getStatus() != null ? dto.getStatus() : CustomerGroup.GroupStatus.ACTIVE)
                .build();

        return toDTO(customerGroupRepository.save(group), true);
    }

    public CustomerGroupDTO modifier(Long id, CustomerGroupDTO dto) {
        CustomerGroup group = getGroup(id);
        group.setName(dto.getName());
        if (dto.getGroupType() != null) {
            group.setGroupType(dto.getGroupType());
        }
        if (dto.getStatus() != null) {
            group.setStatus(dto.getStatus());
        }
        return toDTO(customerGroupRepository.save(group), true);
    }

    @Transactional
    public CustomerGroupDTO ajouterClient(Long groupId, Long customerId, CustomerGroupMember.MemberRole memberRole, boolean primaryMember) {
        CustomerGroup group = getGroup(groupId);
        Client client = clientRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + customerId));

        customerGroupMemberRepository.findFirstByCustomerIdAndStatus(customerId, CustomerGroupMember.MembershipStatus.ACTIVE)
                .ifPresent(existing -> {
                    if (!existing.getCustomerGroup().getId().equals(groupId)) {
                        throw new RuntimeException("Le client appartient deja a un autre groupe actif");
                    }
                    throw new RuntimeException("Le client appartient deja a ce groupe");
                });

        if (primaryMember) {
            clearPrimaryMember(groupId);
        }

        CustomerGroupMember member = CustomerGroupMember.builder()
                .customerGroup(group)
                .customer(client)
                .memberRole(memberRole != null ? memberRole : CustomerGroupMember.MemberRole.USER)
                .joinedAt(LocalDate.now())
                .primaryMember(primaryMember)
                .status(CustomerGroupMember.MembershipStatus.ACTIVE)
                .build();

        customerGroupMemberRepository.save(member);
        return getById(groupId);
    }

    @Transactional
    public CustomerGroupDTO retirerClient(Long groupId, Long customerId) {
        CustomerGroupMember member = customerGroupMemberRepository.findFirstByCustomerIdAndStatus(customerId, CustomerGroupMember.MembershipStatus.ACTIVE)
                .filter(existing -> existing.getCustomerGroup().getId().equals(groupId))
                .orElseThrow(() -> new RuntimeException("Le client n'appartient pas a ce groupe"));

        member.setStatus(CustomerGroupMember.MembershipStatus.INACTIVE);
        member.setLeftAt(LocalDate.now());
        member.setPrimaryMember(false);
        customerGroupMemberRepository.save(member);

        return getById(groupId);
    }

    public CustomerGroupDTO getById(Long id) {
        return toDTO(getGroup(id), true);
    }

    public List<CustomerGroupDTO> getAll() {
        return customerGroupRepository.findAll().stream()
                .map(group -> toDTO(group, false))
                .collect(Collectors.toList());
    }

    public CustomerGroup getActiveGroupForCustomer(Long customerId) {
        return customerGroupMemberRepository.findFirstByCustomerIdAndStatus(customerId, CustomerGroupMember.MembershipStatus.ACTIVE)
                .map(CustomerGroupMember::getCustomerGroup)
                .orElse(null);
    }

    private CustomerGroup getGroup(Long id) {
        return customerGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable : " + id));
    }

    private String genererGroupCode() {
        long prochain = customerGroupRepository.findMaxId().orElse(0L) + 1;
        return "GRP-" + String.format("%05d", prochain);
    }

    private void clearPrimaryMember(Long groupId) {
        customerGroupMemberRepository.findByCustomerGroupId(groupId).stream()
                .filter(CustomerGroupMember::isActive)
                .filter(CustomerGroupMember::isPrimaryMember)
                .forEach(member -> member.setPrimaryMember(false));
    }

    private CustomerGroupDTO toDTO(CustomerGroup group, boolean includeMembers) {
        List<CustomerGroupDTO.MemberDTO> members = includeMembers
                ? customerGroupMemberRepository.findByCustomerGroupId(group.getId()).stream()
                  .map(this::toMemberDTO)
                  .collect(Collectors.toList())
                : null;

        long activeCount = customerGroupMemberRepository
                .countByCustomerGroupIdAndStatus(group.getId(), CustomerGroupMember.MembershipStatus.ACTIVE);

        return CustomerGroupDTO.builder()
                .id(group.getId())
                .groupCode(group.getGroupCode())
                .name(group.getName())
                .groupType(group.getGroupType())
                .status(group.getStatus())
                .members(members)
                .memberCount(activeCount)
                .build();
    }

    private CustomerGroupDTO.MemberDTO toMemberDTO(CustomerGroupMember member) {
        Client customer = member.getCustomer();
        return CustomerGroupDTO.MemberDTO.builder()
                .membershipId(member.getId())
                .customerId(customer.getId())
                .customerName(customer.getNom() + " " + customer.getPrenom())
                .email(customer.getEmail())
                .memberRole(member.getMemberRole())
                .joinedAt(member.getJoinedAt())
                .leftAt(member.getLeftAt())
                .primaryMember(member.isPrimaryMember())
                .status(member.getStatus())
                .build();
    }
}
