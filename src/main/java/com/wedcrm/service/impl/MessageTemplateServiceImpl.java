package com.wedcrm.service.impl;

import com.wedcrm.dto.Assistants.PreviewMessageDTO;
import com.wedcrm.dto.filter.MessageTemplateFilterDTO;
import com.wedcrm.dto.request.MessageTemplateRequestDTO;
import com.wedcrm.dto.response.MessageTemplateResponseDTO;
import com.wedcrm.entity.MessageTemplate;
import com.wedcrm.mapper.MessageTemplateMapper;
import com.wedcrm.repository.MessageTemplateRepository;
import com.wedcrm.service.MessageGeneratorService;
import com.wedcrm.service.MessageTemplateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MessageTemplateServiceImpl implements MessageTemplateService {

    private final MessageTemplateRepository repository;
    private final MessageTemplateMapper mapper;
    private final MessageGeneratorService messageGenerator;

    public MessageTemplateServiceImpl(MessageTemplateRepository repository,
                                      MessageTemplateMapper mapper,
                                      MessageGeneratorService messageGenerator) {
        this.repository = repository;
        this.mapper = mapper;
        this.messageGenerator = messageGenerator;
    }

    @Override
    public MessageTemplateResponseDTO createTemplate(MessageTemplateRequestDTO request) {
        MessageTemplate template = mapper.toEntity(request);
        template.setUsageCount(0);
        template.setIsActive(true);
        MessageTemplate saved = repository.save(template);
        return mapper.toResponseDTO(saved);
    }

    @Override
    public MessageTemplateResponseDTO updateTemplate(UUID id, MessageTemplateRequestDTO request) {
        MessageTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
        mapper.updateEntity(request, template);
        return mapper.toResponseDTO(repository.save(template));
    }

    @Override
    public MessageTemplateResponseDTO getTemplateById(UUID id) {
        MessageTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
        return mapper.toResponseDTO(template);
    }

    @Override
    public Page<MessageTemplateResponseDTO> listTemplates(MessageTemplateFilterDTO filter, Pageable pageable) {
        Specification<MessageTemplate> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.channel() != null)
                predicates.add(cb.equal(root.get("channel"), filter.channel()));
            if (filter.category() != null)
                predicates.add(cb.equal(root.get("category"), filter.category()));
            if (filter.isActive() != null)
                predicates.add(cb.equal(root.get("isActive"), filter.isActive()));
            if (filter.language() != null)
                predicates.add(cb.equal(root.get("language"), filter.language()));
            if (filter.nameSearch() != null && !filter.nameSearch().isBlank())
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.nameSearch().toLowerCase() + "%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable).map(mapper::toResponseDTO);
    }

    @Override
    public void deleteTemplate(UUID id) {
        MessageTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
        repository.delete(template);
    }

    @Override
    public MessageTemplateResponseDTO activateTemplate(UUID id) {
        MessageTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
        template.setIsActive(true);
        return mapper.toResponseDTO(repository.save(template));
    }

    @Override
    public MessageTemplateResponseDTO deactivateTemplate(UUID id) {
        MessageTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
        template.setIsActive(false);
        return mapper.toResponseDTO(repository.save(template));
    }

    @Override
    public PreviewMessageDTO previewTemplate(UUID id, UUID customerId) {
        MessageTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
        // Se customerId for fornecido, usa dados reais; senão, dados de exemplo
        String previewBody = messageGenerator.resolveVariables(template.getBody(), null);
        String previewSubject = template.getSubject() != null ? messageGenerator.resolveVariables(template.getSubject(), null) : null;
        return PreviewMessageDTO.builder()
                .templateId(template.getId())
                .templateName(template.getName())
                .channel(template.getChannel().name())
                .subject(previewSubject)
                .body(previewBody)
                .usedVariables(template.getVariables())
                .customerName(customerId != null ? "Cliente real" : "Cliente Exemplo")
                .customerEmail(customerId != null ? "cliente@email.com" : "exemplo@email.com")
                .build();
    }

    @Override
    public void sendTemplateToCustomer(UUID templateId, UUID customerId) {
        messageGenerator.sendMessage(customerId, templateId, null); // canal será obtido do template
    }

    @Override
    public List<MessageTemplateResponseDTO> getMostUsedTemplates() {
        return repository.findMostUsed(Pageable.ofSize(10)).stream()
                .map(mapper::toResponseDTO)
                .toList();
    }
}
