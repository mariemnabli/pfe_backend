package com.example.telecom.service;

import com.example.telecom.dto.ClientDTO;
import com.example.telecom.dto.ContratDTO;
import com.example.telecom.dto.DirectoryNumberDTO;
import com.example.telecom.dto.OffreDTO;
import com.example.telecom.dto.PlanTarifaireDTO;
import com.example.telecom.dto.ServiceDTO;
import com.example.telecom.entity.ContractHolderType;
import com.example.telecom.entity.ContractType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CsvImportService {

    public List<ClientDTO> parseClients(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = readCsv(file);
        List<ClientDTO> clients = new ArrayList<>();

        for (Map<String, String> row : rows) {
            ClientDTO dto = new ClientDTO();
            dto.setNom(required(row, "nom"));
            dto.setPrenom(required(row, "prenom"));
            dto.setTelephone(required(row, "telephone"));
            dto.setEmail(required(row, "email"));
            dto.setAdresse(required(row, "adresse"));
            dto.setVille(required(row, "ville"));
            dto.setDocumentType(parseInteger(required(row, "documenttype"), "documentType"));
            dto.setCinNumber(optional(row, "cinnumber"));
            dto.setPassportNumber(optional(row, "passportnumber"));
            dto.setCustomerGroupId(parseLong(optional(row, "customergroupid"), "customerGroupId"));
            clients.add(dto);
        }

        return clients;
    }

    public List<OffreDTO> parseOffres(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = readCsv(file);
        List<OffreDTO> offres = new ArrayList<>();

        for (Map<String, String> row : rows) {
            OffreDTO dto = new OffreDTO();
            dto.setNomOffre(required(row, "nomoffre"));
            dto.setTypeOffre(required(row, "typeoffre"));
            dto.setPlanTarifaireId(parseLong(optional(row, "plantarifaireid"), "planTarifaireId"));
            dto.setServiceIds(parseLongList(optional(row, "serviceids")));
            offres.add(dto);
        }

        return offres;
    }

    public List<ContratDTO> parseContrats(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = readCsv(file);
        List<ContratDTO> contrats = new ArrayList<>();

        for (Map<String, String> row : rows) {
            ContratDTO dto = new ContratDTO();
            dto.setContractType(parseContractType(optional(row, "contracttype")));
            dto.setHolderType(parseHolderType(optional(row, "holdertype")));
            dto.setDirectoryNumber(parseLong(optional(row, "directorynumber"), "directoryNumber"));
            dto.setClientId(parseLong(optional(row, "clientid"), "clientId"));
            dto.setCustomerGroupId(parseLong(optional(row, "customergroupid"), "customerGroupId"));
            dto.setOffreId(parseLong(required(row, "offreid"), "offreId"));
            contrats.add(dto);
        }

        return contrats;
    }

    public List<PlanTarifaireDTO> parsePlansTarifaires(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = readCsv(file);
        List<PlanTarifaireDTO> plans = new ArrayList<>();

        for (Map<String, String> row : rows) {
            PlanTarifaireDTO dto = new PlanTarifaireDTO();
            dto.setNom(required(row, "nom"));
            dto.setPrixMensuel(parseDouble(required(row, "prixmensuel"), "prixMensuel"));
            dto.setDescription(required(row, "description"));
            plans.add(dto);
        }

        return plans;
    }

    public List<DirectoryNumberDTO> parseDirectoryNumbers(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = readCsv(file);
        List<DirectoryNumberDTO> directoryNumbers = new ArrayList<>();

        for (Map<String, String> row : rows) {
            DirectoryNumberDTO dto = new DirectoryNumberDTO();
            dto.setNumero(parseLong(required(row, "numero"), "numero"));
            dto.setStatus(optional(row, "status"));
            dto.setDateActivation(parseDate(optional(row, "dateactivation"), "dateActivation"));
            dto.setDateDesactivation(parseDate(optional(row, "datedesactivation"), "dateDesactivation"));
            dto.setContratId(parseLong(optional(row, "contratid"), "contratId"));
            dto.setContractId(optional(row, "contractid"));
            directoryNumbers.add(dto);
        }

        return directoryNumbers;
    }

    public List<ServiceDTO> parseServices(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = readCsv(file);
        List<ServiceDTO> services = new ArrayList<>();

        for (Map<String, String> row : rows) {
            ServiceDTO dto = new ServiceDTO();
            dto.setNomService(required(row, "nomservice"));
            dto.setDescription(required(row, "description"));
            services.add(dto);
        }

        return services;
    }

    private List<Map<String, String>> readCsv(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Le fichier CSV est obligatoire");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new RuntimeException("Le fichier CSV est vide");
            }

            char delimiter = detectDelimiter(headerLine);
            List<String> headers = parseCsvLine(headerLine, delimiter).stream()
                    .map(this::normalizeHeader)
                    .toList();

            List<Map<String, String>> rows = new ArrayList<>();
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                List<String> values = parseCsvLine(line, delimiter);
                if (values.size() != headers.size()) {
                    throw new RuntimeException("Ligne CSV invalide " + lineNumber
                            + " : nombre de colonnes different du header");
                }

                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), values.get(i).trim());
                }
                rows.add(row);
            }

            return rows;
        }
    }

    private List<String> parseCsvLine(String line, char delimiter) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (ch == delimiter && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(ch);
        }

        if (inQuotes) {
            throw new RuntimeException("Ligne CSV invalide : guillemets non fermes");
        }

        values.add(current.toString());
        return values;
    }

    private char detectDelimiter(String headerLine) {
        long semicolons = headerLine.chars().filter(ch -> ch == ';').count();
        long commas = headerLine.chars().filter(ch -> ch == ',').count();
        return semicolons > commas ? ';' : ',';
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.trim().toLowerCase(Locale.ROOT);
    }

    private String required(Map<String, String> row, String key) {
        String value = optional(row, key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Colonne obligatoire manquante ou vide : " + key);
        }
        return value;
    }

    private String optional(Map<String, String> row, String key) {
        return row.getOrDefault(key, null);
    }

    private Integer parseInteger(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Valeur invalide pour " + fieldName + " : " + value);
        }
    }

    private Long parseLong(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Valeur invalide pour " + fieldName + " : " + value);
        }
    }

    private List<Long> parseLongList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return List.of(value.split("\\|")).stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(item -> parseLong(item, "serviceIds"))
                .collect(Collectors.toList());
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Date invalide pour " + fieldName + " : " + value
                    + ". Format attendu: yyyy-MM-dd");
        }
    }

    private Double parseDouble(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Valeur invalide pour " + fieldName + " : " + value);
        }
    }

    private ContractType parseContractType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ContractType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("contractType invalide : " + value
                    + ". Valeurs acceptees: INDIVIDUAL, ENTERPRISE");
        }
    }

    private ContractHolderType parseHolderType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ContractHolderType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("holderType invalide : " + value
                    + ". Valeurs acceptees: CUSTOMER, CUSTOMER_GROUP");
        }
    }
}
