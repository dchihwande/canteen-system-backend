package co.zw.bancabc.canteen_system_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LdapService {

    private final LdapTemplate ldapTemplate;

    @Value("${ad.domain}")
    private String domain;

    /** Look up a user's email in AD via sAMAccountName or CN. Returns null if not found. */
    public String getUserEmail(String username) {
        try {
            LdapQuery query = LdapQueryBuilder.query()
                    .where("objectClass").is("person")
                    .and("sAMAccountName").is(username);

            List<String> emails = ldapTemplate.search(query, (AttributesMapper<String>) attrs -> {
                try {
                    return attrs.get("mail") != null ? attrs.get("mail").get().toString() : null;
                } catch (NamingException e) {
                    return null;
                }
            });
            if (emails.isEmpty() || emails.get(0) == null) {
                // Fallback: construct UPN-based email from domain
                return username + "@" + domain;
            }
            return emails.get(0);
        } catch (Exception e) {
            log.warn("Failed to fetch email from AD for {}: {}. Falling back to UPN.", username, e.getMessage());
            return username + "@" + domain;
        }
    }

    /** Get the user's full distinguished name from AD. Returns null if not found. */
    public String getUserDn(String username) {
        try {
            LdapQuery query = LdapQueryBuilder.query()
                    .where("objectClass").is("person")
                    .and("sAMAccountName").is(username);

            List<String> dns = ldapTemplate.search(query, (AttributesMapper<String>) attrs -> {
                try {
                    if (attrs.get("distinguishedName") != null) {
                        return attrs.get("distinguishedName").get().toString();
                    }
                    return null;
                } catch (NamingException e) {
                    return null;
                }
            });
            return dns.isEmpty() ? null : dns.get(0);
        } catch (Exception e) {
            log.debug("Failed to fetch DN from AD for {}: {}", username, e.getMessage());
            return null;
        }
    }
}