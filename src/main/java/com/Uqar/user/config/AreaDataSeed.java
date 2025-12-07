package com.Uqar.user.config;

import com.Uqar.user.entity.Area;
import com.Uqar.user.entity.AreaTranslation;
import com.Uqar.user.repository.AreaRepository;
import com.Uqar.user.repository.AreaTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AreaDataSeed implements CommandLineRunner {

    private final AreaRepository areaRepository;
    private final AreaTranslationRepository areaTranslationRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (areaRepository.count() == 0) {
            log.info("Seeding Damascus areas data...");
            seedDamascusAreas();
            log.info("Damascus areas data seeded successfully!");
        } else {
            log.info("Areas data already exists, skipping seed.");
        }
    }

    private void seedDamascusAreas() {
        List<AreaData> areasData = Arrays.asList(
                new AreaData("Old City", "البلدة القديمة", "Historic Damascus - the ancient walled city"),
                new AreaData("Midan", "الميدان", "Traditional commercial area in Damascus"),
                new AreaData("Sarouja", "ساروجة", "Central Damascus area"),
                new AreaData("Qanawat", "قنوات", "Central Damascus area"),
                new AreaData("Shaghour", "الشاغور", "Traditional neighborhood in Damascus"),
                new AreaData("Bab Tuma", "باب توما", "Christian quarter in Damascus"),
                new AreaData("Bab Sharqi", "باب شرقي", "Eastern gate area of Damascus"),
                new AreaData("Rukn al-Din", "ركن الدين", "Northern area of Damascus"),
                new AreaData("Kafr Sousa", "كفر سوسة", "Modern residential area in Damascus"),
                new AreaData("Mazzeh", "المزة", "Western area of Damascus"),
                new AreaData("Dummar", "دمر", "Northern residential area of Damascus"),
                new AreaData("Barzeh", "البرزة", "Northern area of Damascus"),
                new AreaData("Qaboun", "القابون", "Eastern area of Damascus"),
                new AreaData("Jobar", "جوبر", "Eastern area of Damascus"),
                new AreaData("Douma", "دوما", "Eastern suburb of Damascus"),
                new AreaData("Harasta", "حرستا", "Eastern suburb of Damascus"),
                new AreaData("Nashabiya", "النشابية", "Eastern area of Damascus"),
                new AreaData("Yarmouk", "اليرموك", "Palestinian refugee camp area"),
                new AreaData("Sayyida Zainab", "السيدة زينب", "Southern area of Damascus"),
                new AreaData("Hajar al-Aswad", "حجر الأسود", "Southern area of Damascus")
        );

        for (AreaData areaData : areasData) {
            // Create Area entity
            Area area = Area.builder()
                    .name(areaData.name)
                    .description(areaData.description)
                    .arabicName(areaData.arabicName)
                    .isActive(true)
                    .build();

            Area savedArea = areaRepository.save(area);

            // Create Arabic translation
            AreaTranslation arabicTranslation = AreaTranslation.builder()
                    .area(savedArea)
                    .language("ar")
                    .translatedName(areaData.arabicName)
                    .translatedDescription(areaData.description)
                    .build();

            areaTranslationRepository.save(arabicTranslation);

            // Create English translation (same as base name)
            AreaTranslation englishTranslation = AreaTranslation.builder()
                    .area(savedArea)
                    .language("en")
                    .translatedName(areaData.name)
                    .translatedDescription(areaData.description)
                    .build();

            areaTranslationRepository.save(englishTranslation);
        }
    }

    private static class AreaData {
        final String name;
        final String arabicName;
        final String description;

        AreaData(String name, String arabicName, String description) {
            this.name = name;
            this.arabicName = arabicName;
            this.description = description;
        }
    }
}
