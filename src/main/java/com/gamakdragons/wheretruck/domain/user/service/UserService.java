package com.gamakdragons.wheretruck.domain.user.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.domain.user.dto.Role;
import com.gamakdragons.wheretruck.domain.user.entity.User;

public interface UserService {
    
    User getById(String id);

    IndexUpdateResultDto saveUser(User user);
    DeleteResultDto deleteUser(String id);

    IndexUpdateResultDto changeNickName(String userId, String nickName);
    IndexUpdateResultDto changeRole(String userId, Role role);
}
