package nl.vlasje24.service;

import nl.vlasje24.domain.AccountTokenPurpose;

record AccountMailEvent(
        AccountTokenPurpose purpose,
        String recipient,
        String username,
        String rawToken
) {
}
