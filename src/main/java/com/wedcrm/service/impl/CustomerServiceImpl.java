package com.wedcrm.service.impl;

import com.wedcrm.entity.*;
import com.wedcrm.enums.AutomationTrigger;
import com.wedcrm.enums.CustomerStatus;
import com.wedcrm.repository.*;
import com.wedcrm.service.AutomationRuleService;
import com.wedcrm.service.CustomerService;
import com.wedcrm.service.NotificationService;
import com.wedcrm.specification.CustomerSpecification;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private AutomationRuleService automationService;

    @Autowired
    private NotificationService notificationService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ========== CRUD BÁSICO ==========

    @Override
    public CustomerResponseDTO createCustomer(CustomerRequestDTO request) {
        // 1. Valida CPF/CNPJ (implementação simplificada)
        if (request.cpfCnpj() != null && !isValidCpfCnpj(request.cpfCnpj())) {
            throw new IllegalArgumentException("CPF/CNPJ inválido");
        }

        // 2. Verifica duplicidade de e-mail
        if (customerRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado: " + request.email());
        }

        // 3. Verifica duplicidade de CPF/CNPJ
        if (request.cpfCnpj() != null && customerRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new IllegalArgumentException("CPF/CNPJ já cadastrado: " + request.cpfCnpj());
        }

        // 4. Busca vendedor responsável
        User assignedTo = null;
        if (request.assignedToId() != null) {
            assignedTo = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new RuntimeException("Vendedor não encontrado"));
        }

        // 5. Cria o cliente
        Customer customer = new Customer();
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setWhatsapp(request.whatsapp());
        customer.setCompany(request.company());
        customer.setPosition(request.position());
        customer.setCpfCnpj(request.cpfCnpj());
        customer.setStatus(request.status());
        customer.setSource(request.source());
        customer.setAssignedTo(assignedTo);
        customer.setNotes(request.notes());
        customer.setBirthday(request.birthday());
        customer.setScore(0);

        // 6. Adiciona endereço se existir
        if (request.address() != null) {
            Address address = new Address();
            address.setStreet(request.address().street());
            address.setNumber(request.address().number());
            address.setComplement(request.address().complement());
            address.setNeighborhood(request.address().neighborhood());
            address.setCity(request.address().city());
            address.setState(request.address().state());
            address.setZipCode(request.address().zipCode());
            address.setCountry(request.address().country());
            customer.setAddress(address);
        }

        // 7. Adiciona campos personalizados
        if (request.customFields() != null) {
            customer.setCustomFields(request.customFields());
        }

        Customer savedCustomer = customerRepository.save(customer);

        // 8. Adiciona tags
        if (request.tagIds() != null) {
            for (UUID tagId : request.tagIds()) {
                addTag(savedCustomer.getId(), tagId);
            }
        }

        // 9. Dispara automação CUSTOMER_CREATED
        automationService.processTrigger(AutomationTrigger.CUSTOMER_CREATED, savedCustomer);

        // 10. Notifica o vendedor responsável
        if (assignedTo != null) {
            notificationService.notifyCustomerAssigned(assignedTo, savedCustomer);
        }

        return toResponseDTO(savedCustomer);
    }

    @Override
    public CustomerResponseDTO updateCustomer(UUID id, CustomerRequestDTO request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        CustomerStatus oldStatus = customer.getStatus();

        // 1. Valida CPF/CNPJ
        if (request.cpfCnpj() != null && !isValidCpfCnpj(request.cpfCnpj())) {
            throw new IllegalArgumentException("CPF/CNPJ inválido");
        }

        // 2. Verifica duplicidade de e-mail (excluindo o próprio)
        if (!customer.getEmail().equals(request.email()) &&
                customerRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado: " + request.email());
        }

        // 3. Verifica duplicidade de CPF/CNPJ (excluindo o próprio)
        if (request.cpfCnpj() != null && !request.cpfCnpj().equals(customer.getCpfCnpj()) &&
                customerRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new IllegalArgumentException("CPF/CNPJ já cadastrado: " + request.cpfCnpj());
        }

        // 4. Busca novo vendedor se alterado
        if (request.assignedToId() != null &&
                (customer.getAssignedTo() == null || !customer.getAssignedTo().getId().equals(request.assignedToId()))) {
            User newAssignee = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new RuntimeException("Vendedor não encontrado"));
            customer.setAssignedTo(newAssignee);
            notificationService.notifyCustomerAssigned(newAssignee, customer);
        }

        // 5. Atualiza dados básicos
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setWhatsapp(request.whatsapp());
        customer.setCompany(request.company());
        customer.setPosition(request.position());
        customer.setCpfCnpj(request.cpfCnpj());
        customer.setStatus(request.status());
        customer.setSource(request.source());
        customer.setNotes(request.notes());
        customer.setBirthday(request.birthday());

        // 6. Atualiza endereço
        if (request.address() != null) {
            Address address = customer.getAddress();
            if (address == null) {
                address = new Address();
                customer.setAddress(address);
            }
            address.setStreet(request.address().street());
            address.setNumber(request.address().number());
            address.setComplement(request.address().complement());
            address.setNeighborhood(request.address().neighborhood());
            address.setCity(request.address().city());
            address.setState(request.address().state());
            address.setZipCode(request.address().zipCode());
            address.setCountry(request.address().country());
        }

        // 7. Atualiza campos personalizados
        if (request.customFields() != null) {
            customer.setCustomFields(request.customFields());
        }

        Customer savedCustomer = customerRepository.save(customer);

        // 8. Detecta mudança de status e dispara automação
        if (oldStatus != savedCustomer.getStatus()) {
            automationService.processTrigger(AutomationTrigger.CUSTOMER_STATUS_CHANGED, savedCustomer);
        }

        return toResponseDTO(savedCustomer);
    }

    @Override
    public CustomerResponseDTO getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return toResponseDTO(customer);
    }

    @Override
    public Page<CustomerSummaryDTO> listCustomers(CustomerFilterDTO filter, Pageable pageable) {
        Specification<Customer> spec = buildSpecification(filter);
        Page<Customer> customers = customerRepository.findAll(spec, pageable);
        return customers.map(this::toSummaryDTO);
    }

    @Override
    public void deleteCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Soft delete - apenas desativa
        customer.setActive(false);
        customerRepository.save(customer);
    }

    // ========== ASSIGNMENT E TAGS ==========

    @Override
    public CustomerResponseDTO assignCustomer(UUID customerId, UUID userId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        User newAssignee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Vendedor não encontrado"));

        customer.setAssignedTo(newAssignee);
        Customer savedCustomer = customerRepository.save(customer);

        // Notifica o novo responsável
        notificationService.notifyCustomerAssigned(newAssignee, savedCustomer);

        return toResponseDTO(savedCustomer);
    }

    @Override
    public CustomerResponseDTO addTag(UUID customerId, UUID tagId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag não encontrada"));

        customer.addTag(tag);
        Customer savedCustomer = customerRepository.save(customer);

        return toResponseDTO(savedCustomer);
    }

    @Override
    public CustomerResponseDTO removeTag(UUID customerId, UUID tagId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag não encontrada"));

        customer.removeTag(tag);
        Customer savedCustomer = customerRepository.save(customer);

        return toResponseDTO(savedCustomer);
    }

    // ========== LEAD SCORE ==========

    @Override
    public CustomerResponseDTO recalculateScore(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        customer.recalculateScore();
        Customer savedCustomer = customerRepository.save(customer);

        return toResponseDTO(savedCustomer);
    }

    // ========== INATIVIDADE ==========

    @Override
    public List<CustomerSummaryDTO> findInactiveCustomers(int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        List<Customer> inactiveCustomers = customerRepository.findInactiveCustomers(since);
        return inactiveCustomers.stream()
                .filter(Customer::isActive)
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ========== IMPORTAÇÃO E EXPORTAÇÃO ==========

    @Override
    public ImportResultDTO importCustomers(MultipartFile csvFile) {
        ImportResultDTO result = new ImportResultDTO();

        try (Reader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

            for (CSVRecord record : parser) {
                try {
                    CustomerRequestDTO request = parseCustomerFromCSV(record);
                    createCustomer(request);
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(record.getRecordNumber(), e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo CSV", e);
        }

        return result;
    }

    @Override
    public byte[] exportCustomers(CustomerFilterDTO filter) {
        Specification<Customer> spec = buildSpecification(filter);
        List<Customer> customers = customerRepository.findAll(spec);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(outputStream),
                     CSVFormat.DEFAULT.withHeader("ID", "Nome", "E-mail", "Telefone", "WhatsApp",
                             "Empresa", "Status", "Origem", "Vendedor", "Score", "Último Contato", "Data Criação"))) {

            for (Customer customer : customers) {
                printer.printRecord(
                        customer.getId(),
                        customer.getFullName(),
                        customer.getEmail(),
                        customer.getPhone(),
                        customer.getWhatsapp(),
                        customer.getCompany(),
                        customer.getStatus().getDescription(),
                        customer.getSource() != null ? customer.getSource().getDescription() : "",
                        customer.getAssignedTo() != null ? customer.getAssignedTo().getName() : "",
                        customer.getScore(),
                        customer.getLastContactDate() != null ? customer.getLastContactDate().toString() : "",
                        customer.getCreatedAt().toString()
                );
            }

            printer.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao exportar clientes", e);
        }
    }

    // ========== TIMELINE ==========

    @Override
    public List<TimelineEventDTO> getCustomerTimeline(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        List<TimelineEventDTO> timeline = new ArrayList<>();

        // Adiciona interações
        List<Interaction> interactions = interactionRepository.findByCustomerOrderByCreatedAtDesc(customer);
        for (Interaction interaction : interactions) {
            timeline.add(TimelineEventDTO.builder()
                    .date(interaction.getCreatedAt())
                    .type("INTERACTION")
                    .title(interaction.getType().getDescription())
                    .description(interaction.getContentSummary(100))
                    .icon(interaction.getTypeIcon())
                    .build());
        }

        // Adiciona atividades
        List<Activity> activities = activityRepository.findByCustomerOrderByDueDateDesc(customer);
        for (Activity activity : activities) {
            timeline.add(TimelineEventDTO.builder()
                    .date(activity.getCreatedAt())
                    .type("ACTIVITY")
                    .title(activity.getTitle())
                    .description(activity.getDescription())
                    .icon(activity.getTypeIcon())
                    .status(activity.getStatus().getDescription())
                    .build());
        }

        // Ordena por data (mais recente primeiro)
        timeline.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        return timeline;
    }

    // ========== MÉTODOS PRIVADOS ==========

    private Specification<Customer> buildSpecification(CustomerFilterDTO filter) {
        List<Specification<Customer>> specs = new ArrayList<>();

        specs.add(CustomerSpecification.filterByStatus(filter.status()));
        specs.add(CustomerSpecification.filterByAssignedTo(filter.assignedToId()));
        specs.add(CustomerSpecification.filterByTag(filter.tagId()));
        specs.add(CustomerSpecification.filterByMinScore(filter.minScore()));
        specs.add(CustomerSpecification.filterByMaxScore(filter.maxScore()));
        specs.add(CustomerSpecification.filterByLastContactBefore(filter.inactiveSince()));
        specs.add(CustomerSpecification.filterBySearchTerm(filter.searchTerm()));
        specs.add(CustomerSpecification.filterByActive(true));

        return CustomerSpecification.combineSpecifications(specs);
    }

    private CustomerResponseDTO toResponseDTO(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .whatsapp(customer.getWhatsapp())
                .company(customer.getCompany())
                .position(customer.getPosition())
                .cpfCnpj(customer.getCpfCnpj())
                .status(customer.getStatus())
                .statusDescription(customer.getStatus().getDescription())
                .source(customer.getSource())
                .assignedToId(customer.getAssignedTo() != null ? customer.getAssignedTo().getId() : null)
                .assignedToName(customer.getAssignedTo() != null ? customer.getAssignedTo().getName() : null)
                .tags(customer.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .address(customer.getAddress())
                .notes(customer.getNotes())
                .birthday(customer.getBirthday())
                .age(customer.getAge())
                .score(customer.getScore())
                .lastContactDate(customer.getLastContactDate())
                .opportunitiesCount(customer.getOpportunities().size())
                .activitiesCount(customer.getActivities().size())
                .interactionsCount(customer.getInteractions().size())
                .createdAt(customer.getCreatedAt())
                .createdBy(customer.getCreatedBy())
                .build();
    }

    private CustomerSummaryDTO toSummaryDTO(Customer customer) {
        return CustomerSummaryDTO.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .company(customer.getCompany())
                .status(customer.getStatus())
                .statusDescription(customer.getStatus().getDescription())
                .assignedToName(customer.getAssignedTo() != null ? customer.getAssignedTo().getName() : null)
                .score(customer.getScore())
                .lastContactDate(customer.getLastContactDate())
                .tags(customer.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .build();
    }

    private CustomerRequestDTO parseCustomerFromCSV(CSVRecord record) {
        return CustomerRequestDTO.builder()
                .firstName(record.get("nome"))
                .lastName(record.get("sobrenome"))
                .email(record.get("email"))
                .phone(record.get("telefone"))
                .company(record.get("empresa"))
                .status(CustomerStatus.valueOf(record.get("status")))
                .build();
    }

    private boolean isValidCpfCnpj(String documento) {
        // Implementação simplificada - em produção usar biblioteca específica
        if (documento == null) return true;
        String numeros = documento.replaceAll("[^0-9]", "");
        return numeros.length() == 11 || numeros.length() == 14;
    }
}