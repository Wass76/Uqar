# خطة التعديلات لاستخدام Message Queue (RabbitMQ)

## 1. إضافة Dependencies

### في `pom.xml`:
```xml
<!-- RabbitMQ -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

---

## 2. إضافة Configuration

### في `application.yml`:
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    # Retry configuration
    listener:
      simple:
        retry:
          enabled: true
          initial-interval: 1000
          max-attempts: 3
          multiplier: 2
        # Acknowledgment mode
        acknowledge-mode: manual
        # Prefetch count (how many messages to fetch at once)
        prefetch: 10
```

---

## 3. إنشاء RabbitMQ Configuration

### ملف جديد: `NotificationQueueConfig.java`
```java
@Configuration
public class NotificationQueueConfig {
    
    // Queue names
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_DLQ = "notification.dlq"; // Dead Letter Queue
    
    // Exchange names
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_DLX = "notification.dlx"; // Dead Letter Exchange
    
    // Routing keys
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
            .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
            .withArgument("x-dead-letter-routing-key", NOTIFICATION_ROUTING_KEY)
            .build();
    }
    
    @Bean
    public Queue notificationDLQ() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }
    
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }
    
    @Bean
    public DirectExchange notificationDLX() {
        return new DirectExchange(NOTIFICATION_DLX);
    }
    
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
            .bind(notificationQueue())
            .to(notificationExchange())
            .with(NOTIFICATION_ROUTING_KEY);
    }
    
    @Bean
    public Binding notificationDLQBinding() {
        return BindingBuilder
            .bind(notificationDLQ())
            .to(notificationDLX())
            .with(NOTIFICATION_ROUTING_KEY);
    }
}
```

---

## 4. إنشاء Producer (يضع الرسائل في Queue)

### ملف جديد: `NotificationProducer.java`
```java
@Service
public class NotificationProducer {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public void sendNotificationToQueue(NotificationRequest request) {
        // حفظ الإشعار في DB أولاً
        Notification notification = saveNotificationToDB(request);
        
        // إرسال الرسالة إلى Queue
        rabbitTemplate.convertAndSend(
            NotificationQueueConfig.NOTIFICATION_EXCHANGE,
            NotificationQueueConfig.NOTIFICATION_ROUTING_KEY,
            notification.getId() // إرسال ID فقط لتقليل حجم الرسالة
        );
        
        logger.info("Notification {} sent to queue", notification.getId());
    }
    
    private Notification saveNotificationToDB(NotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Notification notification = Notification.builder()
            .user(user)
            .title(request.getTitle())
            .body(request.getBody())
            .notificationType(request.getNotificationType())
            .data(request.getData())
            .status("PENDING")
            .build();
        
        return notificationRepository.save(notification);
    }
}
```

---

## 5. إنشاء Consumer (يقرأ من Queue ويرسل)

### ملف جديد: `NotificationConsumer.java`
```java
@Component
public class NotificationConsumer {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private DeviceTokenRepository deviceTokenRepository;
    
    @Autowired
    private FirebaseMessagingService firebaseMessagingService;
    
    @RabbitListener(queues = NotificationQueueConfig.NOTIFICATION_QUEUE)
    public void handleNotification(Long notificationId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
            
            // إرسال الإشعار عبر FCM
            sendFCMNotification(notification);
            
            // Acknowledgment - تأكيد نجاح المعالجة
            channel.basicAck(tag, false);
            
            logger.info("Notification {} processed successfully", notificationId);
            
        } catch (Exception e) {
            logger.error("Error processing notification {}: {}", notificationId, e.getMessage(), e);
            
            // Negative Acknowledgment - رفض الرسالة وإرسالها إلى DLQ
            try {
                channel.basicNack(tag, false, false);
            } catch (IOException ioException) {
                logger.error("Error sending NACK: {}", ioException.getMessage());
            }
        }
    }
    
    private void sendFCMNotification(Notification notification) {
        List<DeviceToken> deviceTokens = deviceTokenRepository
            .findByUserIdAndIsActiveTrue(notification.getUser().getId());
        
        if (deviceTokens.isEmpty()) {
            notification.setStatus("FAILED");
            notificationRepository.save(notification);
            throw new RuntimeException("No active device tokens");
        }
        
        FCMNotificationDTO fcmNotification = FCMNotificationDTO.builder()
            .title(notification.getTitle())
            .body(notification.getBody())
            .data(notification.getData())
            .build();
        
        boolean sent = false;
        for (DeviceToken deviceToken : deviceTokens) {
            String result = firebaseMessagingService.sendNotificationToDevice(
                deviceToken.getDeviceToken(), 
                fcmNotification
            );
            if (result != null) {
                sent = true;
            }
        }
        
        if (sent) {
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
        } else {
            notification.setStatus("FAILED");
            throw new RuntimeException("Failed to send FCM notification");
        }
        
        notificationRepository.save(notification);
    }
}
```

---

## 6. تعديل NotificationService

### في `NotificationService.java`:
```java
@Service
@Transactional
public class NotificationService extends BaseSecurityService {
    
    @Autowired
    private NotificationProducer notificationProducer; // بدلاً من FirebaseMessagingService مباشرة
    
    // إزالة sendNotification القديم واستبداله بـ:
    public NotificationResponse sendNotification(NotificationRequest request) {
        // فقط وضع الرسالة في Queue
        notificationProducer.sendNotificationToQueue(request);
        
        // إرجاع response (يمكن البحث عن الإشعار من DB)
        Notification notification = notificationRepository
            .findByUserAndTitleAndBody(
                userRepository.findById(request.getUserId()).orElse(null),
                request.getTitle(),
                request.getBody()
            )
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
        return notificationMapper.toResponse(notification);
    }
    
    // إزالة retryFailedNotification - لأن RabbitMQ يتولى Retry تلقائياً
}
```

---

## 7. تعديل NotificationSchedulerService

### في `NotificationSchedulerService.java`:
```java
// إزالة retryFailedNotifications() method بالكامل
// لأن RabbitMQ يتولى Retry تلقائياً

// باقي الـ jobs تبقى كما هي (checkLowStock, checkExpiringProducts, etc.)
// لكن بدلاً من notificationService.sendNotification() مباشرة:
// نستخدم notificationProducer.sendNotificationToQueue()
```

---

## 8. Dead Letter Queue Handler (اختياري)

### ملف جديد: `NotificationDLQHandler.java`
```java
@Component
public class NotificationDLQHandler {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @RabbitListener(queues = NotificationQueueConfig.NOTIFICATION_DLQ)
    public void handleFailedNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElse(null);
        
        if (notification != null) {
            notification.setStatus("FAILED");
            notification.setRetryCount(3); // وصل للحد الأقصى
            notificationRepository.save(notification);
            
            logger.error("Notification {} moved to DLQ after max retries", notificationId);
        }
    }
}
```

---

## 9. إزالة Retry Mechanism القديم

### في `NotificationSchedulerService.java`:
- حذف `retryFailedNotifications()` method
- حذف `retryFailedNotification()` من `NotificationService`

---

## 10. التثبيت

### تثبيت RabbitMQ:
```bash
# Windows (using Docker)
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# أو تثبيت مباشر من: https://www.rabbitmq.com/download.html
```

### الوصول إلى Management UI:
- URL: http://localhost:15672
- Username: guest
- Password: guest

---

## الفوائد بعد التعديل:

✅ **Guaranteed Delivery**: RabbitMQ يضمن وصول الرسائل  
✅ **Automatic Retry**: Retry تلقائي مع exponential backoff  
✅ **Dead Letter Queue**: الرسائل الفاشلة تذهب إلى DLQ  
✅ **Load Balancing**: يمكن تشغيل عدة Consumers  
✅ **Persistent Messages**: الرسائل محفوظة حتى لو توقف التطبيق  
✅ **Acknowledgment**: تأكيد نجاح المعالجة  

---

## ملاحظات مهمة:

1. **يجب تثبيت RabbitMQ Server** قبل تشغيل التطبيق
2. **يمكن استخدام Docker** لتشغيل RabbitMQ بسهولة
3. **Dead Letter Queue** يحفظ الرسائل الفاشلة للتحليل لاحقاً
4. **يمكن إضافة Monitoring** باستخدام RabbitMQ Management UI

