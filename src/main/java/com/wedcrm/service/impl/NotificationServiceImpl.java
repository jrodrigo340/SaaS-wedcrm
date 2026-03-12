package com.wedcrm.service.impl;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PushProvider pushProvider;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            PushProvider pushProvider) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.pushProvider = pushProvider;
    }

}
