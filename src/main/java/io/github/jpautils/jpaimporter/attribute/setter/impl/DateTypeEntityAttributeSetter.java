package io.github.jpautils.jpaimporter.attribute.setter.impl;

import io.github.jpautils.jpaimporter.attribute.setter.EntityAttributeSetter;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeCharacteristicsDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeValueDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTypeEntityAttributeSetter implements EntityAttributeSetter {

    @Override
    public void setEntityAttributeValue(EntityAttributeCharacteristicsDto entityAttributeCharacteristics, EntityAttributeValueDto entityAttributeValue, Object entity) {
        if (!entityAttributeValue.getIsValueSet()) {
            return;
        }

        Method method = entityAttributeCharacteristics.getSetterMethod();
        Field field = entityAttributeCharacteristics.getAttributeField();

        String typeName = entityAttributeCharacteristics.getSetterType().getTypeName();
        Object valueToBeSet = parseDate(entityAttributeValue.getStringValue(), typeName);

        try {
            if (method != null) {
                method.invoke(entity, valueToBeSet);
            } else if (field != null) {
                field.set(entity, valueToBeSet);
            } else {
                throw new RuntimeException("Could not find a setter method/field for attribute: " + entityAttributeCharacteristics.getAttributeField().getName() + "and class: " + entityAttributeCharacteristics.getEntityClass());
            }
        } catch (Exception exception) {
            throw new RuntimeException("Could not set value for attribute [" + entityAttributeCharacteristics.getAttributeField().getName() + "] of class [" + entityAttributeCharacteristics.getEntityClass() + "]. Value: " + entityAttributeValue, exception);
        }
    }

    @Override
    public Predicate insertAttributeInCriteriaQuerySearch(EntityAttributeCharacteristicsDto entityAttributeCharacteristics, EntityAttributeValueDto entityAttributeValue, String fieldName, CriteriaBuilder criteriaBuilder, Root<?> root) {
        String typeName = entityAttributeCharacteristics.getSetterType().getTypeName();

        Object valueToBeSet = parseDate(entityAttributeValue.getStringValue(), typeName);
        return criteriaBuilder.equal(root.get(fieldName), valueToBeSet);
    }

    @Override
    public boolean matchesEntityAttribute(EntityAttributeCharacteristicsDto entityAttributeCharacteristics) {
        String typeName = entityAttributeCharacteristics.getSetterType().getTypeName();
        return typeName.equals("java.util.Date") ||
                typeName.equals("java.util.Calendar") ||
                typeName.equals("java.sql.Date") ||
                typeName.equals("java.sql.Time") ||
                typeName.equals("java.sql.Timestamp") ||
                typeName.equals("java.time.LocalDate") ||
                typeName.equals("java.time.LocalTime") ||
                typeName.equals("java.time.LocalDateTime") ||
                typeName.equals("java.time.OffsetTime") ||
                typeName.equals("java.time.OffsetDateTime");
    }

    @Override
    public BigDecimal getPriority() {
        return new BigDecimal(10);
    }

    public Object parseDate(String dateString, String returnType) {
        String dateOnly = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$";
        String dateTime = "^[0-9]{4}-[0-9]{2}-[0-9]{2}[\\sT]{1}[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]{1,3})?(Z|([\\-+][0-9]{2}:[0-9]{2}))?$";
        String timeOnly = "^T?[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]{1,3})?(Z|([\\-+][0-9]{2}:[0-9]{2}))?$";

        Pattern dateOnlyPattern = Pattern.compile(dateOnly);
        Matcher dateOnlyMatcher = dateOnlyPattern.matcher(dateString);

        if (dateOnlyMatcher.find()) {
            LocalDate localDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(dateString, LocalDate::from);

            switch (returnType) {
//                case "java.util.Date":
//                    return Date.from(localDate.atStartOfDay(ZoneOffset.UTC).toInstant());
//                case "java.util.Calendar":
//                    return GregorianCalendar.from(ZonedDateTime.ofInstant(localDate.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC));
                case "java.sql.Date":
                    return java.sql.Date.valueOf(localDate);
                case "java.sql.Timestamp":
                    return Timestamp.valueOf(localDate.atStartOfDay());
                case "java.time.LocalDate":
                    return localDate;
                case "java.time.LocalDateTime":
                    return localDate.atStartOfDay();
//                case "java.time.OffsetDateTime":
//                    return OffsetDateTime.of(localDate.atStartOfDay(), ZoneOffset.UTC);
                default:
                    throw new RuntimeException("Could not convert value: [" + dateString + "] which is a local date, to type: [" + returnType + "]");
            }
        }

        Pattern dateTimePattern = Pattern.compile(dateTime);
        Matcher dateTimeMatcher = dateTimePattern.matcher(dateString);

        if (dateTimeMatcher.find()) {
            final String canonicalizedDateString;
            if (dateString.contains(" ")) {
                canonicalizedDateString = dateString.replace(' ', 'T');
            } else {
                canonicalizedDateString = dateString;
            }

            TemporalAccessor temporalAccessor = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S][OOOO][O][z][XXXXX][XXXX]")
                    .parseBest(canonicalizedDateString, ZonedDateTime::from, LocalDateTime::from);

            if (temporalAccessor instanceof ZonedDateTime) {
                ZonedDateTime zonedDateTime = (ZonedDateTime)temporalAccessor;

                switch (returnType) {
                    case "java.util.Date":
                        return Date.from(zonedDateTime.toInstant());
                    case "java.util.Calendar":
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(Date.from(zonedDateTime.toInstant()));
                        return calendar;
                    case "java.sql.Date":
                        return java.sql.Date.valueOf(zonedDateTime.toLocalDate());
                    case "java.sql.Timestamp":
                        return Timestamp.from(zonedDateTime.toInstant());
                    case "java.time.LocalDate":
                        return zonedDateTime.toLocalDate();
                    case "java.time.LocalDateTime":
                        return zonedDateTime.toLocalDateTime();
                    case "java.time.OffsetDateTime":
                        return zonedDateTime.toOffsetDateTime();
                    default:
                        throw new RuntimeException("Could not convert value: [" + dateString + "] which is a zoned date time, to type: [" + returnType + "]");
                }
            }

            LocalDateTime localDateTime = (LocalDateTime) temporalAccessor;

            switch (returnType) {
                case "java.util.Date":
                    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
                case "java.util.Calendar":
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(Date.from(localDateTime.toInstant(ZoneOffset.UTC)));
                    return calendar;
                case "java.sql.Date":
                    return java.sql.Date.valueOf(localDateTime.toLocalDate());
                case "java.sql.Timestamp":
                    return Timestamp.from(localDateTime.toInstant(ZoneOffset.UTC));
                case "java.time.LocalDate":
                    return localDateTime.toLocalDate();
                case "java.time.LocalDateTime":
                    return localDateTime;
                case "java.time.OffsetDateTime":
                    return localDateTime.atOffset(ZoneOffset.UTC);
                default:
                    throw new RuntimeException("Could not convert value: [" + dateString + "] which is a local date time, to type: [" + returnType + "].");
            }
        }

        Pattern timeOnlyPattern = Pattern.compile(timeOnly);
        Matcher timeOnlyMatcher = timeOnlyPattern.matcher(dateString);

        if (timeOnlyMatcher.find()) {
            TemporalAccessor temporalAccessor = DateTimeFormatter.ofPattern("['T']HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S][OOOO][O][z][XXXXX][XXXX]")
                    .parseBest(dateString, OffsetTime::from, LocalTime::from);

            if (temporalAccessor instanceof LocalTime) {
                LocalTime localTime = (LocalTime) temporalAccessor;

                switch (returnType) {
                    case "java.sql.Time":
                        return Time.valueOf(localTime);
                    case "java.time.LocalTime":
                        return localTime;
                    case "java.time.OffsetTime":
                        return OffsetTime.of(localTime, ZoneOffset.UTC);
                    default:
                        throw new RuntimeException("Could not convert value: [" + dateString + "] which is a local time, to type: [" + returnType + "].");
                }
            }

            OffsetTime offsetTime = (OffsetTime)temporalAccessor;

            switch (returnType) {
                case "java.sql.Time":
                    return Time.valueOf(offsetTime.toLocalTime());
                case "java.time.LocalTime":
                    return offsetTime.toLocalTime();
                case "java.time.OffsetTime":
                    return offsetTime;
                default:
                    throw new RuntimeException("Could not convert value: [" + dateString + "] which is a offset time, to type: [" + returnType + "].");
            }
        }

        throw new RuntimeException("Could not convert value: [" + dateString + "] to a time object. Iti did not match date patterns.");
    }
}
