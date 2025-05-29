package com.olg.qweb.api.users;

import com.olg.core.annotations.ApiVersion;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserServiceFactory {

    private final Map<String, IUserService> versions = new HashMap<>();

    public UserServiceFactory(List<IUserService> services) {
        for (IUserService service : services) {
            ApiVersion versionAnnotation = service.getClass().getAnnotation(ApiVersion.class);
            if (versionAnnotation != null) {
                versions.put(versionAnnotation.value(), service);
            }
        }
    }

    public IUserService getService(String version) throws Exception {
        if(version == null || versions.get(version) == null) {
            throw new Exception("Not supported service version");
        }

        return versions.get(version);
    }

}

