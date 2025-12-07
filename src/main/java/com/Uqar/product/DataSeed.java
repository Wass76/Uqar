package com.Uqar.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.Uqar.language.Language;
import com.Uqar.language.LanguageRepo;
import com.Uqar.product.entity.Category;
import com.Uqar.product.entity.CategoryTranslation;
import com.Uqar.product.entity.Form;
import com.Uqar.product.entity.FormTranslation;
import com.Uqar.product.entity.Manufacturer;
import com.Uqar.product.entity.ManufacturerTranslation;
import com.Uqar.product.entity.MasterProduct;
import com.Uqar.product.entity.Type;
import com.Uqar.product.entity.TypeTranslation;
import com.Uqar.product.repo.CategoryRepo;
import com.Uqar.product.repo.CategoryTranslationRepo;
import com.Uqar.product.repo.FormRepo;
import com.Uqar.product.repo.FormTranslationRepo;
import com.Uqar.product.repo.ManufacturerRepo;
import com.Uqar.product.repo.ManufacturerTranslationRepo;
import com.Uqar.product.repo.MasterProductRepo;
import com.Uqar.product.repo.TypeRepo;
import com.Uqar.product.repo.TypeTranslationRepo;
import com.Uqar.user.Enum.PharmacyType;
import com.Uqar.user.entity.Customer;
import com.Uqar.user.entity.Pharmacy;
import com.Uqar.user.repository.CustomerRepo;
import com.Uqar.user.repository.PharmacyRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeed {

    private static final Logger logger = LoggerFactory.getLogger(DataSeed.class);

    private final LanguageRepo languageRepo;
    private final CategoryRepo categoryRepo;
    private final CategoryTranslationRepo categoryTranslationRepo;
    private final FormRepo formRepo;
    private final FormTranslationRepo formTranslationRepo;
    private final TypeRepo typeRepo;
    private final TypeTranslationRepo typeTranslationRepo;
    private final ManufacturerRepo manufacturerRepo;
    private final ManufacturerTranslationRepo manufacturerTranslationRepo;
    private final MasterProductRepo masterProductRepo;
    private final CustomerRepo customerRepository;
    private final PharmacyRepository pharmacyRepository;

    private final Random random = new Random();

    @PostConstruct
    public void seedAll() {
        try {
            logger.info("🌱 Starting data seeding process...");
            logger.info("🌱 بدء عملية تعبئة البيانات...");

            // Check if database is ready
            if (!isDatabaseReady()) {
                logger.warn("Database is not ready for seeding. Skipping data seeding.");
                logger.warn("قاعدة البيانات غير جاهزة للتعبئة. سيتم تخطي تعبئة البيانات.");
                return;
            }

            seedLanguages();
            seedCategories();
            seedForms();
            seedTypes();
            seedManufacturers();
            seedPharmacy(); // Must be before customers since customers depend on pharmacy
            seedCustomers();
//            seedPharmaceuticalProducts(); // New: Seed pharmaceutical products

            logger.info("🎉 Data seeding completed successfully!");
            logger.info("🎉 تم إكمال تعبئة البيانات بنجاح!");
        } catch (Exception e) {
            logger.error("❌ Error during data seeding: {}", e.getMessage(), e);
            logger.error("❌ خطأ أثناء تعبئة البيانات: {}", e.getMessage());
            logger.warn("Application will continue without seeded data. You may need to seed data manually later.");
            logger.warn("سيستمر التطبيق بدون البيانات المعبأة. قد تحتاج لتعبئة البيانات يدوياً لاحقاً.");
        }
    }

    private boolean isDatabaseReady() {
        try {
            // Try to access a simple repository method to check if tables exist
            languageRepo.count();
            return true;
        } catch (Exception e) {
            logger.warn("Database tables are not ready yet: {}", e.getMessage());
            logger.warn("جداول قاعدة البيانات ليست جاهزة بعد: {}", e.getMessage());
            return false;
        }
    }

    private void seedLanguages() {
        try {
            if (languageRepo.count() == 0) {
                List<Language> languages = List.of(
                        new Language("ar", "Arabic"),
                        new Language("en", "English")
                );
                languageRepo.saveAll(languages);
                logger.info("✅ Languages seeded");
                logger.info("✅ تم تعبئة اللغات");
            } else {
                logger.info("Languages already exist, skipping seeding");
                logger.info("اللغات موجودة مسبقاً، سيتم تخطي التعبئة");
            }
        } catch (Exception e) {
            logger.error("Error seeding languages: {}", e.getMessage());
            logger.error("خطأ في تعبئة اللغات: {}", e.getMessage());
        }
    }

    private void seedCategories() {
        try {
            if (categoryRepo.count() == 0) {
                Language ar = languageRepo.findByCode("ar").orElseThrow();

                // Enhanced categories for pharmaceutical system
                Category cat1 = new Category();
                cat1.setName("Painkillers");
                cat1 = categoryRepo.save(cat1);

                Category cat2 = new Category();
                cat2.setName("Antibiotics");
                cat2 = categoryRepo.save(cat2);

                Category cat3 = new Category();
                cat3.setName("Sterilizers");
                cat3 = categoryRepo.save(cat3);

                Category cat4 = new Category();
                cat4.setName("Cardiovascular");
                cat4 = categoryRepo.save(cat4);

                Category cat5 = new Category();
                cat5.setName("Gastrointestinal");
                cat5 = categoryRepo.save(cat5);

                Category cat6 = new Category();
                cat6.setName("Respiratory");
                cat6 = categoryRepo.save(cat6);

                Category cat7 = new Category();
                cat7.setName("Vitamins & Supplements");
                cat7 = categoryRepo.save(cat7);

                List<CategoryTranslation> translations = List.of(
                        new CategoryTranslation("مسكنات", cat1, ar),
                        new CategoryTranslation("مضادات حيوية", cat2, ar),
                        new CategoryTranslation("معقمات", cat3, ar),
                        new CategoryTranslation("أدوية القلب والأوعية الدموية", cat4, ar),
                        new CategoryTranslation("أدوية الجهاز الهضمي", cat5, ar),
                        new CategoryTranslation("أدوية الجهاز التنفسي", cat6, ar),
                        new CategoryTranslation("فيتامينات ومكملات غذائية", cat7, ar)
                );
                categoryTranslationRepo.saveAll(translations);
                logger.info("✅ Categories seeded");
                logger.info("✅ تم تعبئة الفئات");
            } else {
                logger.info("Categories already exist, skipping seeding");
                logger.info("الفئات موجودة مسبقاً، سيتم تخطي التعبئة");
            }
        } catch (Exception e) {
            logger.error("Error seeding categories: {}", e.getMessage());
            logger.error("خطأ في تعبئة الفئات: {}", e.getMessage());
        }
    }

    private void seedForms() {
        try {
            if (formRepo.count() == 0) {
                Language ar = languageRepo.findByCode("ar").orElseThrow();

                // Enhanced forms for pharmaceutical system
                Form form1 = new Form();
                form1.setName("Tablets");
                form1 = formRepo.save(form1);

                Form form2 = new Form();
                form2.setName("Coated Tablets");
                form2 = formRepo.save(form2);

                Form form3 = new Form();
                form3.setName("Capsules");
                form3 = formRepo.save(form3);

                Form form4 = new Form();
                form4.setName("Syrup");
                form4 = formRepo.save(form4);

                Form form5 = new Form();
                form5.setName("Injection");
                form5 = formRepo.save(form5);

                Form form6 = new Form();
                form6.setName("Ointment");
                form6 = formRepo.save(form6);

                Form form7 = new Form();
                form7.setName("Solution");
                form7 = formRepo.save(form7);

                Form form8 = new Form();
                form8.setName("Drops");
                form8 = formRepo.save(form8);

                Form form9 = new Form();
                form9.setName("Spray");
                form9 = formRepo.save(form9);

                Form form10 = new Form();
                form10.setName("Extended Release Capsules");
                form10 = formRepo.save(form10);

                List<FormTranslation> translations = List.of(
                        new FormTranslation("أقراص", form1, ar),
                        new FormTranslation("أقراص ملبسة", form2, ar),
                        new FormTranslation("محافظ", form3, ar),
                        new FormTranslation("شراب", form4, ar),
                        new FormTranslation("حقن", form5, ar),
                        new FormTranslation("مرهم", form6, ar),
                        new FormTranslation("محلول", form7, ar),
                        new FormTranslation("قطرة", form8, ar),
                        new FormTranslation("بخاخ", form9, ar),
                        new FormTranslation("محافظ مديدة التحرر", form10, ar)
                );
                formTranslationRepo.saveAll(translations);
                logger.info("✅ Forms seeded");
                logger.info("✅ تم تعبئة الأشكال الصيدلانية");
            } else {
                logger.info("Forms already exist, skipping seeding");
                logger.info("الأشكال الصيدلانية موجودة مسبقاً، سيتم تخطي التعبئة");
            }
        } catch (Exception e) {
            logger.error("Error seeding forms: {}", e.getMessage());
            logger.error("خطأ في تعبئة الأشكال الصيدلانية: {}", e.getMessage());
        }
    }

    private void seedTypes() {
        try {
            if (typeRepo.count() == 0) {
                Language ar = languageRepo.findByCode("ar").orElseThrow();

                Type type1 = new Type();
                type1.setName("Medicine");
                type1 = typeRepo.save(type1);

                Type type2 = new Type();
                type2.setName("Cosmetic");
                type2 = typeRepo.save(type2);

                Type type3 = new Type();
                type3.setName("Medical Supplies");
                type3 = typeRepo.save(type3);

                Type type4 = new Type();
                type4.setName("Supplements");
                type4 = typeRepo.save(type4);

                List<TypeTranslation> translations = List.of(
                        new TypeTranslation("دواء", type1, ar),
                        new TypeTranslation("مستحضر تجميل", type2, ar),
                        new TypeTranslation("مستلزمات طبية", type3, ar),
                        new TypeTranslation("مكملات غذائية", type4, ar)
                );
                typeTranslationRepo.saveAll(translations);
                logger.info("✅ Types seeded");
                logger.info("✅ تم تعبئة الأنواع");
            } else {
                logger.info("Types already exist, skipping seeding");
                logger.info("الأنواع موجودة مسبقاً، سيتم تخطي التعبئة");
            }
        } catch (Exception e) {
            logger.error("Error seeding types: {}", e.getMessage());
            logger.error("خطأ في تعبئة الأنواع: {}", e.getMessage());
        }
    }

    private void seedManufacturers() {
        try {
            if (manufacturerRepo.count() == 0) {
                Language ar = languageRepo.findByCode("ar").orElseThrow();

                // Enhanced manufacturers for pharmaceutical system
                Manufacturer m1 = new Manufacturer();
                m1.setName("Uqar Pharma");
                m1 = manufacturerRepo.save(m1);

                Manufacturer m2 = new Manufacturer();
                m2.setName("Ultra Medica");
                m2 = manufacturerRepo.save(m2);

                Manufacturer m3 = new Manufacturer();
                m3.setName("Avenzor");
                m3 = manufacturerRepo.save(m3);

                Manufacturer m4 = new Manufacturer();
                m4.setName("Bahari Pharmaceutical");
                m4 = manufacturerRepo.save(m4);

                Manufacturer m5 = new Manufacturer();
                m5.setName("Domna Pharmaceutical");
                m5 = manufacturerRepo.save(m5);

                Manufacturer m6 = new Manufacturer();
                m6.setName("Barakat Pharmaceutical");
                m6 = manufacturerRepo.save(m6);

                Manufacturer m7 = new Manufacturer();
                m7.setName("Al-Razi Pharmaceutical");
                m7 = manufacturerRepo.save(m7);

                Manufacturer m8 = new Manufacturer();
                m8.setName("Default Manufacturer");
                m8 = manufacturerRepo.save(m8);

                List<ManufacturerTranslation> translations = List.of(
                        new ManufacturerTranslation("ترياق فارما", m1, ar),
                        new ManufacturerTranslation("ألترا ميديكا", m2, ar),
                        new ManufacturerTranslation("ابن زهر", m3, ar),
                        new ManufacturerTranslation("بحري للصناعة الدوائية", m4, ar),
                        new ManufacturerTranslation("دومنا للصناعة الدوائية", m5, ar),
                        new ManufacturerTranslation("بركات للصناعة الدوائية", m6, ar),
                        new ManufacturerTranslation("الرازي للصناعة الدوائية", m7, ar),
                        new ManufacturerTranslation("مصنع افتراضي", m8, ar)
                );
                manufacturerTranslationRepo.saveAll(translations);
                logger.info("✅ Manufacturers seeded");
                logger.info("✅ تم تعبئة المصنعين");
            } else {
                logger.info("Manufacturers already exist, skipping seeding");
                logger.info("المصنعون موجودون مسبقاً، سيتم تخطي التعبئة");
            }
        } catch (Exception e) {
            logger.error("Error seeding manufacturers: {}", e.getMessage());
            logger.error("خطأ في تعبئة المصنعين: {}", e.getMessage());
        }
    }

//    /**
//     * Seed pharmaceutical products with sample data
//     * تعبئة المنتجات الصيدلانية ببيانات نموذجية
//     */
//    private void seedPharmaceuticalProducts() {
//        try {
//            if (masterProductRepo.count() == 0) {
//                logger.info("🏥 Seeding pharmaceutical products...");
//                logger.info("🏥 تعبئة المنتجات الصيدلانية...");
//
//                // Get forms and manufacturers
//                List<Form> forms = formRepo.findAll();
//                List<Manufacturer> manufacturers = manufacturerRepo.findAll();
//
//                if (forms.isEmpty() || manufacturers.isEmpty()) {
//                    logger.warn("Forms or Manufacturers not found. Cannot seed products.");
//                    logger.warn("الأشكال الصيدلانية أو المصنعون غير موجودون. لا يمكن تعبئة المنتجات.");
//                    return;
//                }
//
//                // Sample pharmaceutical products
//                List<MasterProduct> products = List.of(
//                        createProduct("Panadol", "Paracetamol", "500mg", "20 Tablets",
//                                BigDecimal.valueOf(150.00), BigDecimal.valueOf(200.00),
//                                forms.get(0), manufacturers.get(0)),
//
//                        createProduct("Augmentin", "Amoxicillin + Clavulanic Acid", "625mg", "14 Tablets",
//                                BigDecimal.valueOf(850.00), BigDecimal.valueOf(1000.00),
//                                forms.get(1), manufacturers.get(1)),
//
//                        createProduct("Omeprazole", "Omeprazole Magnesium", "20mg", "14 Capsules",
//                                BigDecimal.valueOf(255.00), BigDecimal.valueOf(300.00),
//                                forms.get(2), manufacturers.get(2)),
//
//                        createProduct("Ventolin Syrup", "Salbutamol", "2mg/5ml", "100ml Syrup",
//                                BigDecimal.valueOf(425.00), BigDecimal.valueOf(500.00),
//                                forms.get(3), manufacturers.get(3)),
//
//                        createProduct("Insulin", "Human Insulin", "100 IU/ml", "10ml Vial",
//                                BigDecimal.valueOf(1700.00), BigDecimal.valueOf(2000.00),
//                                forms.get(4), manufacturers.get(4)),
//
//                        createProduct("Betadine", "Povidone Iodine", "10%", "30ml Solution",
//                                BigDecimal.valueOf(170.00), BigDecimal.valueOf(200.00),
//                                forms.get(6), manufacturers.get(5)),
//
//                        createProduct("Aspirin", "Acetylsalicylic Acid", "100mg", "30 Tablets",
//                                BigDecimal.valueOf(85.00), BigDecimal.valueOf(100.00),
//                                forms.get(0), manufacturers.get(6)),
//
//                        createProduct("Vitamin C", "Ascorbic Acid", "1000mg", "20 Effervescent Tablets",
//                                BigDecimal.valueOf(255.00), BigDecimal.valueOf(300.00),
//                                forms.get(0), manufacturers.get(0)),
//
//                        createProduct("Cough Syrup", "Dextromethorphan", "15mg/5ml", "120ml Syrup",
//                                BigDecimal.valueOf(340.00), BigDecimal.valueOf(400.00),
//                                forms.get(3), manufacturers.get(1)),
//
//                        createProduct("Eye Drops", "Chloramphenicol", "0.5%", "10ml Drops",
//                                BigDecimal.valueOf(425.00), BigDecimal.valueOf(500.00),
//                                forms.get(7), manufacturers.get(2))
//                );
//
//                masterProductRepo.saveAll(products);
//                logger.info("✅ {} pharmaceutical products seeded", products.size());
//                logger.info("✅ تم تعبئة {} منتج صيدلاني", products.size());
//            } else {
//                logger.info("Pharmaceutical products already exist, skipping seeding");
//                logger.info("المنتجات الصيدلانية موجودة مسبقاً، سيتم تخطي التعبئة");
//            }
//        } catch (Exception e) {
//            logger.error("Error seeding pharmaceutical products: {}", e.getMessage());
//            logger.error("خطأ في تعبئة المنتجات الصيدلانية: {}", e.getMessage());
//        }
//    }

//    /**
//     * Create a pharmaceutical product
//     * إنشاء منتج صيدلاني
//     */
//    private MasterProduct createProduct(String tradeName, String scientificName, String concentration,
//                                        String size, BigDecimal purchasePrice, BigDecimal sellingPrice,
//                                        Form form, Manufacturer manufacturer) {
//        MasterProduct product = new MasterProduct();
//        product.setTradeName(tradeName);
//        product.setScientificName(scientificName);
//        product.setConcentration(concentration);
//        product.setSize(size);
//        product.setRefPurchasePrice(purchasePrice);
//        product.setRefSellingPrice(sellingPrice);
//        product.setNotes("دواء من إنتاج " + manufacturer.getName() + " - " + form.getName());
//        product.setTax(BigDecimal.valueOf(15.0)); // 15% tax
//        product.setBarcode(generateRandomBarcode());
//        product.setForm(form);
//        product.setManufacturer(manufacturer);
//        return product;
//    }

    /**
     * Generate a random 13-digit barcode
     * توليد باركود عشوائي من 13 رقم
     */
    private String generateRandomBarcode() {
        StringBuilder barcode = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            barcode.append(random.nextInt(10));
        }
        return barcode.toString();
    }

    private void seedCustomers() {
        try {
            // Get the pharmacy first
            Pharmacy pharmacy = pharmacyRepository.findAll().get(0);

            // Always ensure cash customer exists
            ensureCashCustomerExists(pharmacy);
            if (customerRepository.count() == 0) {
                // No customers exist, create all including cash customer
                List<Customer> customers = List.of(
                        createCashCustomer(pharmacy),  // Cash Customer for direct sales
                        createCustomer("أحمد محمد", "0991111111", "دمشق - المزة", pharmacy),
                        createCustomer("فاطمة علي", "0992222222", "دمشق - باب شرقي", pharmacy),
                        createCustomer("محمد حسن", "0993333333", "دمشق - أبو رمانة", pharmacy),
                        createCustomer("عائشة أحمد", "0994444444", "دمشق - القابون", pharmacy),
                        createCustomer("علي محمود", "0995555555", "دمشق - الميدان", pharmacy)
                );
                customerRepository.saveAll(customers);
                logger.info("✅ All customers seeded including cash customer");
                logger.info("✅ تم تعبئة جميع العملاء بما في ذلك العميل النقدي");
            }
        } catch (Exception e) {
            logger.error("Error seeding customers: {}", e.getMessage());
            logger.error("خطأ في تعبئة العملاء: {}", e.getMessage());
        }
    }

    private void ensureCashCustomerExists(Pharmacy pharmacy) {
        try {
            // Check if cash customer already exists
            if (!customerRepository.findByNameAndPharmacyId("cash customer", pharmacy.getId()).isPresent()) {
                Customer cashCustomer = createCashCustomer(pharmacy);
                customerRepository.save(cashCustomer);
                logger.info("✅ Cash customer created for pharmacy: {}", pharmacy.getName());
                logger.info("✅ تم إنشاء العميل النقدي للصيدلية: {}", pharmacy.getName());
            } else {
                logger.info("Cash customer already exists for pharmacy: {}", pharmacy.getName());
                logger.info("العميل النقدي موجود مسبقاً للصيدلية: {}", pharmacy.getName());
            }
        } catch (Exception e) {
            logger.error("Error ensuring cash customer exists: {}", e.getMessage());
            logger.error("خطأ في التأكد من وجود العميل النقدي: {}", e.getMessage());
        }
    }

    private Customer createCashCustomer(Pharmacy pharmacy) {
        Customer cashCustomer = new Customer();
        cashCustomer.setName("cash customer");
        cashCustomer.setPhoneNumber(null);  // لا يوجد رقم هاتف
        cashCustomer.setAddress(null);      // لا يوجد عنوان
        cashCustomer.setPharmacy(pharmacy);
        return cashCustomer;
    }

    private Customer createCustomer(String name, String phoneNumber, String address, Pharmacy pharmacy) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhoneNumber(phoneNumber);
        customer.setAddress(address);
        customer.setPharmacy(pharmacy);
        return customer;
    }

    private void seedPharmacy() {
        try {
            if (pharmacyRepository.count() == 0) {
                Pharmacy pharmacy = new Pharmacy();
                pharmacy.setName("صيدلية ترياق");
                pharmacy.setLicenseNumber("PH-001-2024");
                pharmacy.setAddress("دمشق - المزة");
                pharmacy.setEmail("info@Uqar-pharmacy.com");
                pharmacy.setPhoneNumber("011-1234567");
                pharmacy.setOpeningHours("8:00 AM - 10:00 PM");
                pharmacy.setType(PharmacyType.MAIN);
                pharmacy = pharmacyRepository.save(pharmacy);
                logger.info("✅ Pharmacy seeded");
                logger.info("✅ تم تعبئة الصيدلية");
            } else {
                logger.info("Pharmacy already exist, skipping seeding");
                logger.info("الصيدلية موجودة مسبقاً، سيتم تخطي التعبئة");
            }
        } catch (Exception e) {
            logger.error("Error seeding pharmacy: {}", e.getMessage());
            logger.error("خطأ في تعبئة الصيدلية: {}", e.getMessage());
        }
    }
}

