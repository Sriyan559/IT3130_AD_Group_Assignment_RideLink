package com.ridelink.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public record Identity(String id, String role, String status) {
    public static Identity current() {
        var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        Identity identity = request == null ? null : (Identity) request.getAttribute(Identity.class.getName());
        if (identity == null) throw new AccessFailure(401, "Authentication required");
        return identity;
    }
    public void requireOwner(String ownerId, String requiredRole) {
        if (!id.equals(ownerId) || !role.equals(requiredRole)) throw new AccessFailure(403, "Access denied");
    }
}
