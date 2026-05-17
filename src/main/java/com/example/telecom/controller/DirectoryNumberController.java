package com.example.telecom.controller;

import com.example.telecom.dto.BulkImportResponse;
import com.example.telecom.dto.DirectoryNumberDTO;
import com.example.telecom.dto.PaginatedResponse;
import com.example.telecom.entity.DirectoryNumber;
import com.example.telecom.service.CsvImportService;
import com.example.telecom.service.DirectoryNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/directory-numbers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DirectoryNumberController {

    private final DirectoryNumberService directoryNumberService;
    private final CsvImportService csvImportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT','DSI', 'METIER')")
    public ResponseEntity<DirectoryNumberDTO> creer(@RequestBody DirectoryNumberDTO dto) {
        return ResponseEntity.ok(directoryNumberService.creer(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT','DSI', 'METIER')")
    public ResponseEntity<PaginatedResponse<DirectoryNumberDTO>> getAll(
            @RequestParam(required = false) DirectoryNumber.DirectoryNumberStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(directoryNumberService.getAll(status, page, size));
    }

    @PostMapping("/upload-csv")
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT','DSI', 'METIER')")
    public ResponseEntity<BulkImportResponse<DirectoryNumberDTO>> uploadCsv(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        List<DirectoryNumberDTO> directoryNumbers = directoryNumberService.importer(
                csvImportService.parseDirectoryNumbers(file)
        );
        return ResponseEntity.ok(BulkImportResponse.<DirectoryNumberDTO>builder()
                .resourceType("directoryNumbers")
                .createdCount(directoryNumbers.size())
                .items(directoryNumbers)
                .build());
    }
}
