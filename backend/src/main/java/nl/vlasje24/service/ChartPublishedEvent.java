package nl.vlasje24.service;

import java.time.LocalDate;

record ChartPublishedEvent(Integer weekId, LocalDate chartDate) {
}
