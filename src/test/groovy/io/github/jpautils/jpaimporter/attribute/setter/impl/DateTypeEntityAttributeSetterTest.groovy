package io.github.jpautils.jpaimporter.attribute.setter.impl

import spock.lang.Specification

import java.time.ZoneOffset

class DateTypeEntityAttributeSetterTest extends Specification {

    DateTypeEntityAttributeSetter dateTypeEntityAttributeSetter = new DateTypeEntityAttributeSetter();

    def 'should parse date'() {
        when:
        Object result = dateTypeEntityAttributeSetter.parseDate(dateString, returnType)
        final String resultAsString

        if (result.class == java.util.Date.class) {
            Date resultAsDate = (Date) result
            resultAsString = resultAsDate.toInstant().atOffset(ZoneOffset.UTC).toString()
        } else if (result.class == java.util.GregorianCalendar.class) {
            Calendar resultAsCalendar = (Calendar) result
            resultAsString = resultAsCalendar.toInstant().atOffset(ZoneOffset.UTC).toString()
        } else {
            resultAsString = result.toString();
        }

        then:
        result.class == resultClass
        resultAsString == resultValue

        where:
        dateString                      | returnType                    | resultClass                   | resultValue
        '2024-01-01'                    | 'java.sql.Date'               | java.sql.Date                 | '2024-01-01'
        '2024-01-01'                    | 'java.sql.Timestamp'          | java.sql.Timestamp            | '2024-01-01 00:00:00.0'
        '2024-01-01'                    | 'java.time.LocalDate'         | java.time.LocalDate           | '2024-01-01'
        '2024-01-01'                    | 'java.time.LocalDateTime'     | java.time.LocalDateTime       | '2024-01-01T00:00'
        '2024-01-01T00:00:01'           | 'java.time.LocalDateTime'     | java.time.LocalDateTime       | '2024-01-01T00:00:01'
        '2024-01-01T00:00:01'           | 'java.time.LocalDateTime'     | java.time.LocalDateTime       | '2024-01-01T00:00:01'
        '2024-01-01 00:00:01'           | 'java.time.LocalDateTime'     | java.time.LocalDateTime       | '2024-01-01T00:00:01'
        '2024-01-01T00:00:01.999'       | 'java.time.LocalDateTime'     | java.time.LocalDateTime       | '2024-01-01T00:00:01.999'
        '2024-01-01 00:00:01.999'       | 'java.time.LocalDateTime'     | java.time.LocalDateTime       | '2024-01-01T00:00:01.999'
        '2024-01-01T00:00:01.999'       | 'java.util.Date'              | java.util.Date                | '2024-01-01T00:00:01.999Z'
        '2024-01-01T00:00:01.999'       | 'java.util.Calendar'          | java.util.GregorianCalendar   | '2024-01-01T00:00:01.999Z'
        '2024-01-01T00:00:01.999'       | 'java.sql.Date'               | java.sql.Date                 | '2024-01-01'
        '2024-01-01T00:00:01.999'       | 'java.sql.Timestamp'          | java.sql.Timestamp            | '2024-01-01 02:00:01.999'
        '2024-01-01T00:00:01.999'       | 'java.time.LocalDate'         | java.time.LocalDate           | '2024-01-01'
        '2024-01-01T00:00:01.999'       | 'java.time.OffsetDateTime'    | java.time.OffsetDateTime      | '2024-01-01T00:00:01.999Z'
        '2024-01-01T23:59:59Z'          | 'java.time.OffsetDateTime'    | java.time.OffsetDateTime      | '2024-01-01T23:59:59Z'
        '2024-01-01 23:59:59Z'          | 'java.time.OffsetDateTime'    | java.time.OffsetDateTime      | '2024-01-01T23:59:59Z'
        '2024-01-01T23:59:59.999Z'      | 'java.time.OffsetDateTime'    | java.time.OffsetDateTime      | '2024-01-01T23:59:59.999Z'
        '2024-01-01 23:59:59.999Z'      | 'java.time.OffsetDateTime'    | java.time.OffsetDateTime      | '2024-01-01T23:59:59.999Z'
        '2024-01-01T23:59:59-02:00'     | 'java.time.OffsetDateTime'    | java.time.OffsetDateTime      | '2024-01-01T23:59:59-02:00'
        '2024-01-01 23:59:59-02:00'     | 'java.time.OffsetDateTime'    | java.time.OffsetDateTime      | '2024-01-01T23:59:59-02:00'
        '2024-01-01T23:59:59.999+02:00' | 'java.time.OffsetDateTime'    | java.time.OffsetDateTime      | '2024-01-01T23:59:59.999+02:00'
        '2024-01-01 23:59:59.999+02:00' | 'java.time.OffsetDateTime'    | java.time.OffsetDateTime      | '2024-01-01T23:59:59.999+02:00'
        '2024-01-01 23:59:59.999+02:00' | 'java.util.Date'              | java.util.Date                | '2024-01-01T21:59:59.999Z'
        '2024-01-01 23:59:59.999+02:00' | 'java.util.Calendar'          | java.util.GregorianCalendar   | '2024-01-01T21:59:59.999Z'
        '2024-01-01 23:59:59.999+02:00' | 'java.sql.Date'               | java.sql.Date                 | '2024-01-01'
        '2024-01-01 23:59:59.999+02:00' | 'java.sql.Timestamp'          | java.sql.Timestamp            | '2024-01-01 23:59:59.999'
        '2024-01-01 23:59:59.999+02:00' | 'java.time.LocalDate'         | java.time.LocalDate           | '2024-01-01'
        '2024-01-01 23:59:59.999+02:00' | 'java.time.LocalDateTime'     | java.time.LocalDateTime       | '2024-01-01T23:59:59.999'
        'T23:59:59'                     | 'java.time.LocalTime'         | java.time.LocalTime           | '23:59:59'
        'T23:59:59.999'                 | 'java.time.LocalTime'         | java.time.LocalTime           | '23:59:59.999'
        '23:59:59'                      | 'java.time.LocalTime'         | java.time.LocalTime           | '23:59:59'
        '23:59:59.999'                  | 'java.time.LocalTime'         | java.time.LocalTime           | '23:59:59.999'
        '23:59:59.999'                  | 'java.sql.Time'               | java.sql.Time                 | '23:59:59'
        '23:59:59.999'                  | 'java.time.OffsetTime'        | java.time.OffsetTime          | '23:59:59.999Z'
        'T23:59:59-01:00'               | 'java.time.OffsetTime'        | java.time.OffsetTime          | '23:59:59-01:00'
        'T23:59:59.999-01:00'           | 'java.time.OffsetTime'        | java.time.OffsetTime          | '23:59:59.999-01:00'
        '23:59:59+01:00'                | 'java.time.OffsetTime'        | java.time.OffsetTime          | '23:59:59+01:00'
        '23:59:59.999+00:00'            | 'java.time.OffsetTime'        | java.time.OffsetTime          | '23:59:59.999Z'
        '23:59:59.999+01:00'            | 'java.sql.Time'               | java.sql.Time                 | '23:59:59'
        '23:59:59.999+01:00'            | 'java.time.LocalTime'         | java.time.LocalTime           | '23:59:59.999'
    }
}
