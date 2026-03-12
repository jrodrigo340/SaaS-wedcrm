package com.wedcrm.service.impl;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final AutomationService automationService;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            TagRepository tagRepository,
            UserRepository userRepository,
            AutomationService automationService) {
        this.customerRepository = customerRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.automationService = automationService;
    }

    @Override
    public Customer createCustomer(CustomerRequest request) {

        // valida duplicidade
        customerRepository.findByEmailIgnoreCase(request.getEmail())
                .ifPresent(c -> {
                    throw new RuntimeException("Email já cadastrado");
                });

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());

        Customer saved = customerRepository.save(customer);

        automationService.triggerCustomerCreated(saved);

        return saved;
    }

}
