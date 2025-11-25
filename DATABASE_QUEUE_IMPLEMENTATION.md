# Database Queue Implementation

## نظرة عامة

تم استبدال RabbitMQ بنظام Database Queue يستخدم PostgreSQL مباشرة. هذا الحل أبسط ولا يحتاج خدمات إضافية.

## كيف يعمل النظام

### 1. إرسال الإشعارات
- عند استدعاء `NotificationService.sendNotification()`
- يتم حفظ الإشعار في جدول `notification` بحالة `PENDING`
- لا حاجة لخدمة خارجية

### 2. معالجة الإشعارات
- `NotificationQueueProcessor` يعمل كل 5 ثوانٍ (Scheduled Task)
- يبحث عن الإشعارات بحالة `PENDING`
- يعالج 10 إشعارات في كل مرة (BATCH_SIZE)
- يرسل الإشعارات عبر FCM
- يحدث الحالة إلى `SENT` أو `FAILED`

### 3. آلية إعادة المحاولة
- كل إشعار له `retry_count`
- الحد الأقصى للمحاولات: 3
- إذا فشل 3 مرات، يصبح `FAILED`

## الملفات المعدلة

### تم حذفها:
- `NotificationConsumer.java` (RabbitMQ consumer)
- `NotificationDeadLetterListener.java` (RabbitMQ DLQ)
- `NotificationQueueConfig.java` (RabbitMQ config)

### تم تعديلها:
- `NotificationProducer.java` - الآن يحفظ في Database مباشرة
- `NotificationRepository.java` - أضيف method للبحث عن PENDING notifications
- `application.yml` - تم حذف إعدادات RabbitMQ
- `pom.xml` - تم حذف `spring-boot-starter-amqp`

### تم إنشاؤها:
- `NotificationQueueProcessor.java` - Scheduled Task لمعالجة الإشعارات

## المميزات

✅ **لا يحتاج خدمات إضافية** - يستخدم PostgreSQL الموجود  
✅ **بسيط وسهل الصيانة** - كل شيء في قاعدة البيانات  
✅ **Persistence** - الإشعارات محفوظة دائماً  
✅ **إعادة المحاولة** - آلية retry مدمجة  
✅ **لا يحتاج Erlang أو RabbitMQ** - تثبيت أسهل

## الإعدادات

لا حاجة لإعدادات إضافية! النظام يعمل تلقائياً.

### تخصيص الإعدادات (اختياري):

في `NotificationQueueProcessor.java`:
```java
private static final int MAX_RETRY_COUNT = 3; // عدد المحاولات
private static final int BATCH_SIZE = 10; // عدد الإشعارات في كل batch
```

في `@Scheduled`:
```java
@Scheduled(fixedDelay = 5000) // كل 5 ثوانٍ
```

## الاختبار

1. أرسل إشعار عبر API:
```bash
POST /api/notifications/send
{
  "userId": 1,
  "title": "Test",
  "body": "Test notification",
  "notificationType": "SALE_CREATED"
}
```

2. تحقق من جدول `notification`:
```sql
SELECT * FROM notification WHERE status = 'PENDING' ORDER BY created_at;
```

3. بعد 5 ثوانٍ، يجب أن تتغير الحالة إلى `SENT` أو `FAILED`

## ملاحظات

- النظام يعمل بشكل متزامن (كل 5 ثوانٍ)
- إذا كان هناك الكثير من الإشعارات، قد يستغرق وقتاً أطول
- يمكن تقليل `fixedDelay` لمعالجة أسرع (لكن يزيد الضغط على DB)
- يمكن زيادة `BATCH_SIZE` لمعالجة أكثر في كل مرة

