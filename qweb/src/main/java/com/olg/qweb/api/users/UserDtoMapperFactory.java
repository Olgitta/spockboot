package com.olg.qweb.api.users;

import com.olg.core.annotations.ApiVersion;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserDtoMapperFactory {

    private final Map<String, IUserDtoMapper> versions = new HashMap<>();

    public UserDtoMapperFactory(List<IUserDtoMapper> mappers){
        System.out.println("================UserDtoMapperFactory ctor");
        for (IUserDtoMapper mapper : mappers) {
            System.out.println("===============UserDtoMapperFactory forloop");

            ApiVersion versionAnnotation = mapper.getClass().getAnnotation(ApiVersion.class);
            if (versionAnnotation != null) {
                versions.put(versionAnnotation.value(), mapper);
            }
        }
    }

    public IUserDtoMapper getMapper(String version) throws Exception {
        if(version == null || versions.get(version) == null) {
            throw new Exception("Not supported mapper version");
        }

        return versions.get(version);
    }
}
