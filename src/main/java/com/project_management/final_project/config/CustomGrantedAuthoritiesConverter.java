package com.project_management.final_project.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add role
        String role = jwt.getClaimAsString("role");
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        // Add permissions
        List<Map<String, Object>> permissions = jwt.getClaim("permissions");
        if (permissions != null) {
            for (Map<String, Object> permission : permissions) {
                String module = (String) permission.get("module");
                if (Boolean.TRUE.equals(permission.get("canView"))) {
                    authorities.add(new SimpleGrantedAuthority(module + "_VIEW"));
                }
                if (Boolean.TRUE.equals(permission.get("canCreate"))) {
                    authorities.add(new SimpleGrantedAuthority(module + "_CREATE"));
                }
                if (Boolean.TRUE.equals(permission.get("canUpdate"))) {
                    authorities.add(new SimpleGrantedAuthority(module + "_UPDATE"));
                }
                if (Boolean.TRUE.equals(permission.get("canDelete"))) {
                    authorities.add(new SimpleGrantedAuthority(module + "_DELETE"));
                }
            }
        }

        return authorities;
    }
}
